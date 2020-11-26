/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.partitioningBy;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class SetOpportunityDetector {

    private final Fisher fisher;
    private final List<SetOpportunityGenerator> setOpportunityGenerators;
    private final Map<Class<? extends AbstractSetAction>, Double> basicDetectionProbabilities;
    private final double searchBonus;
    private boolean hasSearched = false;

    public SetOpportunityDetector(
        final Fisher fisher,
        final Iterable<SetOpportunityGenerator> setOpportunityGenerators,
        final Map<Class<? extends AbstractSetAction>, Double> basicDetectionProbabilities,
        final double searchBonus
    ) {
        checkArgument(basicDetectionProbabilities.values().stream().allMatch(v -> v >= 0 && v <= 1));
        checkArgument(searchBonus >= 0 && searchBonus <= 1);
        this.fisher = fisher;
        this.setOpportunityGenerators = ImmutableList.copyOf(setOpportunityGenerators);
        this.basicDetectionProbabilities = ImmutableMap.copyOf(basicDetectionProbabilities);
        this.searchBonus = searchBonus;
    }

    @NotNull List<AbstractSetAction> possibleSetActions() {
        final Stream<AbstractSetAction> actions;
        if (fisher.getHold().getPercentageFilled() >= 1) {
            actions = Stream.of(); // no possible sets when hold is full
        } else {
            final FadManager fadManager = getFadManager(fisher);
            final Map<Boolean, List<Fad>> fadsOwnedOrNot = fadManager
                .getFadsHere()
                .collect(partitioningBy(fad -> fad.getOwner() == fadManager));
            actions = Stream.of(
                setsOnOwnFads(fadsOwnedOrNot.get(true)),
                opportunisticFadSets(fadsOwnedOrNot.get(false)),
                setsFromOpportunityGenerators()
            ).flatMap(identity());
        }
        hasSearched = false;
        return actions.collect(toImmutableList());
    }

    private Stream<FadSetAction> setsOnOwnFads(Iterable<Fad> ownFads) {
        return stream(ownFads).map(fad -> new FadSetAction(fisher, fad));
    }

    private Stream<OpportunisticFadSetAction> opportunisticFadSets(Iterable<Fad> otherFads) {
        final double p = getDetectionProbability(OpportunisticFadSetAction.class);
        return stream(otherFads)
            .map(fad -> new OpportunisticFadSetAction(fisher, fad))
            .filter(__ -> fisher.grabRandomizer().nextBoolean(p));
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<AbstractSetAction> setsFromOpportunityGenerators() {
        return setOpportunityGenerators.stream()
            .flatMap(g -> stream(g.get(fisher, fisher.getLocation())))
            .filter(action -> {
                final double p = getDetectionProbability(action.getClass());
                return fisher.grabRandomizer().nextBoolean(p);
            });
    }

    private double getDetectionProbability(Class<? extends AbstractSetAction> actionClass) {
        return basicDetectionProbabilities.get(actionClass) + (hasSearched ? searchBonus : 0);
    }

    public void notifyOfSearch() { hasSearched = true; }

}

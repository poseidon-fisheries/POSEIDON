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
import sim.util.Bag;
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
import static com.google.common.collect.Streams.stream;
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

    @NotNull
    List<AbstractSetAction> possibleSetActions() {
        final List<AbstractSetAction> actions;
        if (fisher.getHold().getPercentageFilled() >= 1) {
            actions = ImmutableList.of(); // no possible sets when hold is full
        } else {
            final FadManager fadManager = getFadManager(fisher);
            final Bag fadsHere = fadManager.getFadMap().fadsAt(fisher.getLocation());
            final double p = getDetectionProbability(OpportunisticFadSetAction.class);
            ImmutableList.Builder<AbstractSetAction> builder = ImmutableList.builder();
            // using the bag directly for speed, here
            for (int i = 0; i < fadsHere.numObjs; i++) {
                Fad fad = (Fad) fadsHere.objs[i];
                if (fad.getOwner() == fadManager)
                    builder.add(new FadSetAction(fisher, fad));
                else if (fisher.grabRandomizer().nextBoolean(p))
                    builder.add(new OpportunisticFadSetAction(fisher, fad));
            }
            setsFromOpportunityGenerators().forEach(builder::add);
            actions = builder.build();
        }
        hasSearched = false;
        return actions;
    }

    private double getDetectionProbability(Class<? extends AbstractSetAction> actionClass) {
        double p = basicDetectionProbabilities.get(actionClass) + (hasSearched ? searchBonus : 0);
        if (p > 1) p = 1; // even the search bonus can't push us above 1!
        return p;
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

    public void notifyOfSearch() {
        hasSearched = true;
    }

}

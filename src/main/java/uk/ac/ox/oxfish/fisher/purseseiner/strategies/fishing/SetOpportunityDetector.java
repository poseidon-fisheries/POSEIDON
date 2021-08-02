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
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import sim.util.Bag;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class SetOpportunityDetector {

    private final Fisher fisher;
    private final FadManager fadManager;
    private final MersenneTwisterFast rng;

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
        this.fadManager = getFadManager(fisher);
        this.rng = fisher.grabRandomizer();
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
            final ImmutableList.Builder<AbstractSetAction> builder = ImmutableList.builder();
            addFadSetOpportunities(builder);
            final SeaTile seaTile = fisher.getLocation();
            addOtherSetOpportunities(builder, seaTile.getBiology(), seaTile.getGridLocation(), fisher.grabState().getStep());
            actions = builder.build();
        }
        hasSearched = false;
        return actions;
    }

    private void addFadSetOpportunities(final ImmutableList.Builder<AbstractSetAction> builder) {
        final Bag fadsHere = fadManager.fadsAt(fisher.getLocation());
        final double p = getDetectionProbability(OpportunisticFadSetAction.class);
        // using the bag directly for speed, here
        for (int i = 0; i < fadsHere.numObjs; i++) {
            final BiomassFad fad = (BiomassFad) fadsHere.objs[i];
            if (fad.getOwner() == fadManager)
                builder.add(new FadSetAction(fisher, fad));
            else if (rng.nextBoolean(p))
                builder.add(new OpportunisticFadSetAction(fisher, fad));
        }
    }

    private void addOtherSetOpportunities(
        final ImmutableList.Builder<AbstractSetAction> builder,
        final LocalBiology biology,
        final Int2D gridLocation,
        final int step
    ) {
        for (final SetOpportunityGenerator generator : setOpportunityGenerators) {
            generator.get(fisher, biology, gridLocation, step)
                .filter(action -> rng.nextBoolean(getDetectionProbability(action.getClass())))
                .ifPresent(builder::add);
        }
    }

    private double getDetectionProbability(final Class<? extends AbstractSetAction> actionClass) {
        double p = basicDetectionProbabilities.get(actionClass) + (hasSearched ? searchBonus : 0);
        if (p > 1) p = 1; // even the search bonus can't push us above 1!
        return p;
    }

    public void notifyOfSearch() {
        hasSearched = true;
    }

}

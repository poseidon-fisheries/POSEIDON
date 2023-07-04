/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * Picks the cheapest insertion area, and then continues to pick that area until the last fad selected is below a
 * threshold value (which is just the `minimumValueFadSets` embedded in the option generator).
 * A caricature of what a fisher who read the marginal value theorem literature would do.
 */
public class MarginalValueFadPlanningModule
    extends DiscretizedOwnFadPlanningModule {


    /**
     * what was the last selection?
     */
    private int lastFadGroupChosen = -1;


    public MarginalValueFadPlanningModule(
        final MapDiscretization discretization,
        final double minimumValueOfFadBeforeBeingPickedUp
    ) {
        super(discretization, minimumValueOfFadBeforeBeingPickedUp);
    }

    public MarginalValueFadPlanningModule(final OwnFadSetDiscretizedActionGenerator optionsGenerator) {
        super(optionsGenerator);
    }

    @Override
    protected PlannedAction chooseFadSet(
        final Plan currentPlanSoFar, final Fisher fisher, final FishState model, final NauticalMap map,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        final List<Entry<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options =
            optionsGenerator.peekAllFads();

        //if there are no options, don't bother
        if (options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if (options.size() == 1) {
            if (options.get(0).getValue() > 0) {
                lastFadGroupChosen = options.get(0).getValue();
                return optionsGenerator.chooseFad(options.get(0).getValue());
            } else return null;
        }


        //if you can pick the same place you were fishing before, do so now
        if (lastFadGroupChosen >= 0) {
            final double valueIfFishingRemainsInThisArea = optionsGenerator.getValueOfThisOption(lastFadGroupChosen);

            if (Double.isFinite(valueIfFishingRemainsInThisArea)) {
                return optionsGenerator.chooseFad(lastFadGroupChosen);
            }

        }
        //need to pick a new spot.
        lastFadGroupChosen = -1;


        final int fadGroupChosen = GreedyInsertionFadPlanningModule.selectFadByCheapestInsertion(
            currentPlanSoFar, fisher, map, options,
            speedInKmPerHours, 0
        );

        //all fads are empty, don't bother setting on any!
        if (fadGroupChosen < 0 ||
            fadGroupChosen >= optionsGenerator.getNumberOfGroups())
            return null;
        lastFadGroupChosen = fadGroupChosen;
        return optionsGenerator.chooseFad(fadGroupChosen);
    }


    public int getLastFadGroupChosen() {
        return lastFadGroupChosen;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        super.start(model, fisher);
        lastFadGroupChosen = -1;
    }

    @Override
    public void prepareForReplanning(final FishState state, final Fisher fisher) {
        super.prepareForReplanning(state, fisher);
        lastFadGroupChosen = -1;

    }
}

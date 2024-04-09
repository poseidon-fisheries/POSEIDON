/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * picks the FAD (map discretized) that maximizes immediate gain (i.e. revenue - cost of getting there and back).
 * <p>
 * In order to make the planning more "long term", you can pick greedily by computing the revenue of multiple fads from
 * that centroid. That way you pick somewhere you are likely going to pick more fads
 */
public class GreedyInsertionFadPlanningModule extends DiscretizedOwnFadPlanningModule {

    private final int additionalFadInspected;

    public GreedyInsertionFadPlanningModule(
        final MapDiscretization discretization,
        final double minimumValueOfFadBeforeBeingPickedUp,
        final int additionalFadInspected
    ) {
        super(discretization, minimumValueOfFadBeforeBeingPickedUp);
        this.additionalFadInspected = additionalFadInspected;
    }

    public GreedyInsertionFadPlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator,
        final int additionalFadInspected
    ) {
        super(optionsGenerator);
        optionsGenerator.setFilterOutCurrentlyInvalidFads(true);
        this.additionalFadInspected = additionalFadInspected;
    }

    @Override
    protected PlannedAction chooseFadSet(
        final Plan currentPlanSoFar,
        final Fisher fisher,
        final FishState model,
        final NauticalMap map,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        final List<Entry<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options =
            optionsGenerator.peekAllFads();

        // if there are no options, don't bother
        if (options == null || options.isEmpty())
            return null;
        // if there is only one option, also don't bother
        if (options.size() == 1) {
            if (options.get(0).getValue() > 0)
                return optionsGenerator.chooseFad(options.get(0).getValue());
            else return null;
        }

        final int fadGroupChosen = selectFadByCheapestInsertion(currentPlanSoFar, fisher, map, options,
            speedInKmPerHours, additionalFadInspected
        );

        // all fads are empty, don't bother setting on any!
        if (fadGroupChosen < 0 ||
            fadGroupChosen >= optionsGenerator.getNumberOfGroups())
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);

    }

    public static int selectFadByCheapestInsertion(
        final Plan currentPlanSoFar,
        final Fisher fisher,
        final NauticalMap map,
        final List<Entry<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options,
        final double boatSpeed,
        final int numberOfAdditionalFadToCount
    ) {
        double maxProfitsSoFar = 0;
        int fadGroupChosen = -1;

        for (final Entry<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer> option : options) {
            // get the fads in centroid
            final PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad> fads = option.getKey();
            // if there are none, don't bother
            if (fads.isEmpty() || fads == null)
                continue;
            // check the cost to go to the "best" fad
            final Iterator<OwnFadSetDiscretizedActionGenerator.ValuedFad> iterator = fads.iterator();
            final OwnFadSetDiscretizedActionGenerator.ValuedFad bestFad = iterator.next();
            final PlannedAction.FadSet potentialAction = new PlannedAction.FadSet(bestFad.getKey());
            final double additionalHoursTravelled = DrawThenCheapestInsertionPlanner
                .cheapestInsert(
                    currentPlanSoFar,
                    potentialAction,
                    Integer.MAX_VALUE, // don't want to censor time now
                    boatSpeed,
                    map,
                    false
                )
                // the `cheapestInsert` method now returns an empty Optional if it can't
                // add the action, but it used to return Double.NaN in those cases.
                // We're just mimicking the old behaviour here; not sure if it matters.
                .orElse(Double.NaN);
            final double costHere = fisher.getExpectedAdditionalCosts(
                additionalHoursTravelled,
                potentialAction.hoursItTake(),
                additionalHoursTravelled / boatSpeed
            );

            // compute the revenues for harvesting the first X fads
            // (the first we will actually do, the others are there to trick us into setting in more promising areas)
            double revenuesHere = bestFad.getValue();
            int additionalFadsInspected = 0;
            while (additionalFadsInspected < numberOfAdditionalFadToCount && iterator.hasNext()) {
                revenuesHere += iterator.next().getValue();
                additionalFadsInspected++;
            }

            final double profitHere = revenuesHere - costHere;
            if (profitHere > maxProfitsSoFar) {
                maxProfitsSoFar = profitHere;
                fadGroupChosen = option.getValue();
            }

        }
        return fadGroupChosen;
    }

}

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
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map.Entry;

import static uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator.ValuedFad;

/**
 * pick the best fads in each quadrant through OwnFadSetDiscretizedActionGenerator and then apply an idea from Feillet (2005): pick the action
 * that has the highest value/distance_from_path_centroid
 * <p>
 * <p>
 * Feillet, Dominique, Pierre Dejax, and Michel Gendreau. "Traveling Salesman Problems with Profits." Transportation
 * Science 39, no. 2 (2005): 188–205.
 */
public class DiscretizedOwnFadCentroidPlanningModule
    extends DiscretizedOwnFadPlanningModule {

    //best fad is chosen as max $/(hr^penalty)
    private final double distancePenalty;

    public DiscretizedOwnFadCentroidPlanningModule(
        final MapDiscretization discretization,
        final double minimumValueOfFadBeforeBeingPickedUp,
        final double distancePenalty
    ) {
        super(discretization, minimumValueOfFadBeforeBeingPickedUp);
        this.distancePenalty = distancePenalty;
    }

    public DiscretizedOwnFadCentroidPlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator,
        final double distancePenalty
    ) {
        super(optionsGenerator);
        this.distancePenalty = distancePenalty;
    }

    protected PlannedAction chooseFadSet(
        final Plan currentPlanSoFar,
        final Fisher fisher,
        final FishState model,
        final NauticalMap map,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {


        final List<Entry<ValuedFad, Integer>> options =
            optionsGenerator.generateBestFadOpportunities();

        //if there are no options, don't bother
        if (options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if (options.size() == 1) {
            if (options.get(0).getValue() > 0)
                return optionsGenerator.chooseFad(options.get(0).getValue());
            else return null;
        }

        //find which seatile the centroid belongs to (the centroid is the middle of a path so a convex operation
        //it should always land on some tile or another)
        final SeaTile centroid = map.getSeaTile(
            (int) currentPlanSoFar.getGridXCentroid(),
            (int) currentPlanSoFar.getGridYCentroid()
        );

        //pick the best value as a distance to centroid
        double discountedValue = Double.MIN_VALUE;
        Integer fadGroupChosen = null;
        for (final Entry<ValuedFad, Integer> option : options) {
            final double hoursSpentTravellingToThere =
                map.distance(centroid, option.getKey().getKey().getLocation()) / speedInKmPerHours;
            assert option.getKey().getValue() >= 0;
            final double currentDiscountValue = option.getKey().getValue() /
                Math.pow(hoursSpentTravellingToThere + 1, distancePenalty);
            if (currentDiscountValue >= discountedValue) {
                fadGroupChosen = option.getValue();
                discountedValue = currentDiscountValue;
            }
        }

        //all fads are empty, don't bother setting on any!
        if (fadGroupChosen == null || fadGroupChosen < 0 || fadGroupChosen >= optionsGenerator.getNumberOfGroups())
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);

    }


}

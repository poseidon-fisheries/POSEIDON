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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.SetLimits;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;
import java.util.Optional;

/**
 * pick the best fads in each quadrant through OwnFadSetDiscretizedActionGenerator and then apply an idea from Feillet (2005): pick the action
 * that has the highest value/distance_from_path_centroid
 *
 *
 * Feillet, Dominique, Pierre Dejax, and Michel Gendreau. “Traveling Salesman Problems with Profits.” Transportation
 * Science 39, no. 2 (2005): 188–205.
 */
public class DiscretizedOwnFadCentroidPlanningModule
        extends DiscretizedOwnFadPlanningModule {

    //best fad is chosen as max $/(hr^penalty)
    private final double distancePenalty;

    public DiscretizedOwnFadCentroidPlanningModule(MapDiscretization discretization,
                                                   double minimumValueOfFadBeforeBeingPickedUp, double distancePenalty) {
        super(discretization, minimumValueOfFadBeforeBeingPickedUp);
        this.distancePenalty = distancePenalty;
    }

    public DiscretizedOwnFadCentroidPlanningModule(OwnFadSetDiscretizedActionGenerator optionsGenerator, double distancePenalty) {
        super(optionsGenerator);
        this.distancePenalty = distancePenalty;
    }

    protected PlannedAction chooseFadSet(Plan currentPlanSoFar,
                                         Fisher fisher,
                                         FishState model,
                                         NauticalMap map,
                                         OwnFadSetDiscretizedActionGenerator optionsGenerator
                                                  ) {


        List<Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer>> options =
                optionsGenerator.generateBestFadOpportunities();

        //if there are no options, don't bother
        if(options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if(options.size()==1)
        {
            if(options.get(0).getSecond()>0)
                return optionsGenerator.chooseFad(options.get(0).getSecond());
            else return null;
        }

        //find which seatile the centroid belongs to (the centroid is the middle of a path so a convex operation
        //it should always land on some tile or another)
        SeaTile centroid = map.getSeaTile((int) currentPlanSoFar.getGridXCentroid(),
                                          (int) currentPlanSoFar.getGridYCentroid());

        //pick the best value as a distance to centroid
        double discountedValue = Double.MIN_VALUE;
        Integer fadGroupChosen = null;
        for (Pair<OwnFadSetDiscretizedActionGenerator.ValuedFad, Integer> option : options) {
            double hoursSpentTravellingToThere =
                    map.distance(centroid,option.getFirst().getFirst().getLocation()) / speedInKmPerHours;
            assert option.getFirst().getSecond()>=0;
            double currentDiscountValue = option.getFirst().getSecond() /
                    Math.pow(hoursSpentTravellingToThere+1,distancePenalty);
            if(currentDiscountValue>= discountedValue)
            {
                fadGroupChosen = option.getSecond();
                discountedValue = currentDiscountValue;
            }
        }

        //all fads are empty, don't bother setting on any!
        if(fadGroupChosen==null || fadGroupChosen<0 || fadGroupChosen >= optionsGenerator.getNumberOfGroups() )
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);

    }




}

/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * standard diffuser for biomass
 */
public class SmoothMovementRule implements BiomassMovementRule {


    /**
     * how much of the differential do you want to move
     */
    private final double differentialPercentageToMove;

    /**
     * maximum percentage of biomass that can leave one place for another
     */
    private final double percentageLimitOnDailyMovement;


    public SmoothMovementRule(double differentialPercentageToMove, double percentageLimitOnDailyMovement) {
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
    }

    @Override
    public void move(
            Species species, SeaTile here, double biomassHere, SeaTile there, double biomassThere, double delta,
            double carryingCapacityHere, double carryingCapacityThere,
            BiomassLocalBiology biologyHere, BiomassLocalBiology biologyThere)
    {
        //shouldn't call this uselessly
        assert carryingCapacityHere >FishStateUtilities.EPSILON;
        assert  carryingCapacityThere >FishStateUtilities.EPSILON;
        if(delta<=0)
            return;

        double differential = Math.min(delta, (carryingCapacityThere-biomassThere));
        differential = FishStateUtilities.round(differential);
        if(differential>0)
        {
            double movement = Math.min(differentialPercentageToMove * differential,
                                       percentageLimitOnDailyMovement * biomassHere);
            assert movement >= 0 : movement + " --- " + differential + " ------ " + here.getBiomass(
                    species) + " ------ " + FishStateUtilities.round(movement);
            assert here.getBiomass(species) >= movement;

            biologyHere.getCurrentBiomass()[species.getIndex()] -=movement;
            biologyThere.getCurrentBiomass()[species.getIndex()] +=movement;

        }

    }
}

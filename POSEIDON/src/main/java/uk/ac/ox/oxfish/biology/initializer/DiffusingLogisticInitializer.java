/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassDiffuserContainer;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * The logistic local biologies now daily share their biomass with their poorer neighbors
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticInitializer extends IndependentLogisticInitializer {

    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;


    public DiffusingLogisticInitializer(
        final DoubleParameter carryingCapacity,
        final DoubleParameter minInitialCapacity, final DoubleParameter maxInitialCapacity,
        final double percentageLimitOnDailyMovement,
        final double differentialPercentageToMove,
        final LogisticGrowerInitializer grower
    ) {
        super(carryingCapacity, minInitialCapacity, maxInitialCapacity, grower);
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
    }

    /**
     * Call the independent logistic initializer but add a steppable to call to smooth fish around
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random  mersenne randomizer
     * @param model
     */
    @Override
    public void processMap(
        final GlobalBiology biology, final NauticalMap map, final MersenneTwisterFast random, final FishState model
    ) {
        super.processMap(biology, map, random, model);

        @SuppressWarnings("deprecation") final BiomassDiffuserContainer diffuser =
            new BiomassDiffuserContainer(map, random, biology,
                differentialPercentageToMove,
                percentageLimitOnDailyMovement
            );
        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);


    }


}

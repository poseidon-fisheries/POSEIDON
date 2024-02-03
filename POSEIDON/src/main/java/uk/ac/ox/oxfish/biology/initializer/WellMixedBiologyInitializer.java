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

import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

/**
 * A facade for a TwoSpeciesBoxInitializer where there is a single box covering everything and where
 * species live together
 * Created by carrknight on 10/8/15.
 */
public class WellMixedBiologyInitializer extends TwoSpeciesBoxInitializer {

    public WellMixedBiologyInitializer(
        DoubleParameter firstSpeciesCapacity,
        DoubleParameter ratioFirstToSecondSpecies, double percentageLimitOnDailyMovement,
        double differentialPercentageToMove,
        LogisticGrowerInitializer grower
    ) {
        super(-1, -1,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            true,
            firstSpeciesCapacity,
            ratioFirstToSecondSpecies,
            percentageLimitOnDailyMovement,
            differentialPercentageToMove,
            grower
        );
    }


}

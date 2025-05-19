/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

public class RecruitmentFixedMultiplierByJackKnifeMaturityTest {

    @Test
    public void ssbToRecruitsWorkCorrectly() {


        final Map<Integer, DoubleParameter> rps = new HashMap<>();
        rps.put(10, new FixedDoubleParameter(2));
        rps.put(30, new FixedDoubleParameter(4));

        final RecruitmentFixedMultiplierByJackKnifeMaturity recruitment =
            new RecruitmentFixedMultiplierByJackKnifeMaturity(
                15, rps, new MersenneTwisterFast()
            );

        final Species species = new Species(
            "Test",
            new FromListMeristics(
                new double[]{1, 2, 3},
                new double[]{10d, 20d, 30d},
                1
            )
        );

        //no recruitment until day 10!
        for (int day = 0; day < 10; day++) {
            final double recruit = recruitment.recruit(
                species,
                species.getMeristics(),
                new StructuredAbundance(new double[]{100, 100, 100}),
                day,
                365
            );
            Assertions.assertEquals(0, recruit, .001d);
        }
        //on day 10 there should a be a recruitment pulse;
        // we are talking about 100 + 100 mature fish, 200kg+300kg ssb
        // rps 2
        Assertions.assertEquals(1000, recruitment.recruit(
            species,
            species.getMeristics(),
            new StructuredAbundance(new double[]{100, 100, 100}),
            10,
            365
        ), .001d);


    }
}

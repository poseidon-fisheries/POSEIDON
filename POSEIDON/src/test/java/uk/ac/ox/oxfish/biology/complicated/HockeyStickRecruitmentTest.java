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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;

public class HockeyStickRecruitmentTest {


    @Test
    public void nohinge() {


        //SSB0 --> 500; hinge at 20%; R0 is 1000
        HockeyStickRecruitment recruitment = new HockeyStickRecruitment(
            true,
            .20,
            1000,
            10,
            500
        );

        //all fish weighs 1, but only bin 1 and 2 are mature
        Species species = new Species(
            "lame",
            new GrowthBinByList(1, new double[]{5, 20, 100}, new double[]{1, 1, 1})
        );

        //250 SSB now
        StructuredAbundance abundance = new StructuredAbundance(new double[]{10000, 100, 150});

        //full recruitment!
        Assertions.assertEquals(
            recruitment.computeYearlyRecruitment(species, species.getMeristics(), abundance),
            1000,
            .0001
        );


    }

    @Test
    public void hinge() {


        //SSB0 --> 500; hinge at 20%; R0 is 1000
        HockeyStickRecruitment recruitment = new HockeyStickRecruitment(
            true,
            .20,
            1000,
            10,
            500
        );

        //all fish weighs 1, but only bin 1 and 2 are mature
        Species species = new Species(
            "lame",
            new GrowthBinByList(1, new double[]{5, 20, 100}, new double[]{1, 1, 1})
        );

        //50 SSB now
        StructuredAbundance abundance = new StructuredAbundance(new double[]{10000, 25, 25});

        //half recruitment! (10% depletion)
        Assertions.assertEquals(
            recruitment.computeYearlyRecruitment(species, species.getMeristics(), abundance),
            500,
            .0001
        );


    }
}

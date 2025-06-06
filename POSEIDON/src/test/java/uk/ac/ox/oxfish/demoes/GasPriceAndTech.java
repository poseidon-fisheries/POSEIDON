/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.demoes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static uk.ac.ox.oxfish.experiments.BestGearWins.efficiencyImitation;

public class GasPriceAndTech {

    @Test
    public void expensiveOilPushesEfficientGear() throws Exception {

        double averageTech = 0;
        for (int i = 0; i < 3; i++) {
            averageTech += efficiencyImitation(.5, 20, "Independent Logistic", System.currentTimeMillis()).getLatest();
            System.out.println(averageTech);
        }

        Assertions.assertTrue(averageTech / 3d < 5);


    }

    @Test
    public void freeGasNoPointImprovingGear() throws Exception {
        double averageTech = 0;
        for (int i = 0; i < 3; i++)
            averageTech += efficiencyImitation(0, 20, "Independent Logistic", System.currentTimeMillis()).getLatest();


        Assertions.assertTrue(averageTech / 3d > 5);


    }
}

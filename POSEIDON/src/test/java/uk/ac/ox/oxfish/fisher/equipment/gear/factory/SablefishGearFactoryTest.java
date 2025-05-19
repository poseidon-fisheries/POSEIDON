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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Created by carrknight on 3/21/17.
 */
public class SablefishGearFactoryTest {


    @Test
    public void correct() throws Exception {


        MeristicsInput sablefish = new MeristicsInput(59, 30, 0.5, 25.8, 56.2, 0.419, 3.6724E-06, 3.250904,
            0.065, 0.5, 25.8, 64, 0.335, 3.4487E-06, 3.26681,
            0.08, 58, -0.13, 1, 0, 40741397,
            0.6, false
        );

        Species species = new Species("Sablefish", sablefish);

        SablefishGearFactory factory = new SablefishGearFactory(.1,
            45.5128, 3.12457, 0.910947,
            100
        );
        FishState st = new FishState();
        HomogeneousAbundanceGear gear = factory.apply(st);

        //filtered and rounded if there are 10000 males in this cell, all aged 1, only 9 will actually be caught
        double[][] abundance = new double[2][sablefish.getMaxAge() + 1];
        abundance[FishStateUtilities.MALE][1] = 10000;
        Assertions.assertEquals(gear.filter(species, abundance)[FishStateUtilities.MALE][1], 9, .001);


    }
}

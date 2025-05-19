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

package uk.ac.ox.oxfish.utility.bandit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by carrknight on 12/1/16.
 */
public class BanditSwitchTest {


    @Test
    public void simpleSwitch() throws Exception {

        final BanditSwitch banditSwitch = new BanditSwitch(
            5,
            integer -> integer % 2 == 0
        );

        Assertions.assertEquals(banditSwitch.getGroup(0), 0);
        Assertions.assertEquals(banditSwitch.getGroup(1), 2);
        Assertions.assertEquals(banditSwitch.getGroup(2), 4);
        Assertions.assertEquals((int) banditSwitch.getArm(0), 0);
        Assertions.assertEquals((int) banditSwitch.getArm(2), 1);
        Assertions.assertEquals((int) banditSwitch.getArm(4), 2);

        Assertions.assertNull(banditSwitch.getArm(1));

    }
}

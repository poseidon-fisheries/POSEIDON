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

package uk.ac.ox.oxfish.utility.bandit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by carrknight on 12/1/16.
 */
public class BanditSwitchTest {


    @Test
    public void simpleSwitch() throws Exception {

        BanditSwitch banditSwitch = new BanditSwitch(5,
                                                     integer -> integer % 2 == 0);

        assertEquals(banditSwitch.getGroup(0),0);
        assertEquals(banditSwitch.getGroup(1),2);
        assertEquals(banditSwitch.getGroup(2),4);
        assertEquals((int)banditSwitch.getArm(0),0);
        assertEquals((int)banditSwitch.getArm(2),1);
        assertEquals((int)banditSwitch.getArm(4),2);

        assertNull(banditSwitch.getArm(1));

    }
}
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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class GrowthBinByListTest {


    @Test
    public void lenghtAtAge() {

        //if no length at age provided then the meristic assumes that bins are one year apart

        GrowthBinByList list = new GrowthBinByList(1, new double[]{1, 2, 3},
            new double[]{1, 2, 3}
        );
        assertEquals(list.getLengthAtAge(1, 1), 2.0, .0001);

        list = new GrowthBinByList(1, new double[]{1, 2, 3},
            new double[]{1, 2, 3},
            new double[]{0, 10}
        );
        assertEquals(list.getLengthAtAge(1, 1), 10.0, .0001);

    }
}
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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 3/21/17.
 */
public class ArrayFilterTest {


    @Test
    public void arrayFilter() throws Exception {

        ArrayFilter filter = new ArrayFilter(new double[]{.5, .3}, new double[]{.1, .2});
        int[][] filtered = filter.filter(new int[]{100, 100}, new int[]{1000, 1000}, mock(Species.class));


        assertEquals(filtered[FishStateUtilities.FEMALE][0],100);
        assertEquals(filtered[FishStateUtilities.FEMALE][1],200);
        assertEquals(filtered[FishStateUtilities.MALE][0],50);
        assertEquals(filtered[FishStateUtilities.MALE][1],30);


    }
}
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

package uk.ac.ox.oxfish.model.regs;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/27/16.
 */
public class TemporaryProtectedAreaTest {


    @Test
    public void canFishHere() throws Exception {

        SeaTile tile = mock(SeaTile.class);
        FishState state = mock(FishState.class);
        when(tile.isProtected()).thenReturn(true);

        when(state.getDayOfTheYear()).thenReturn(100);

        TemporaryProtectedArea reg1 = new TemporaryProtectedArea(10,300);
        TemporaryProtectedArea reg2 = new TemporaryProtectedArea(10,30);
        TemporaryProtectedArea reg3 = new TemporaryProtectedArea(150,300);

        assertFalse(reg1.canFishHere(mock(Fisher.class),
                                    tile,
                                    state));
        assertTrue(reg2.canFishHere(mock(Fisher.class),
                                    tile,
                                    state));
        assertTrue(reg3.canFishHere(mock(Fisher.class),
                                    tile,
                                    state));




    }
}
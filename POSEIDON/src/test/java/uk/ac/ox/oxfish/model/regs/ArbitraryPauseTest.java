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

package uk.ac.ox.oxfish.model.regs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbitraryPauseTest {


    @Test
    public void pause() {

        ArbitraryPause pause = new ArbitraryPause(10, 110, new Anarchy());

        Fisher fisher = mock(Fisher.class);
        when(fisher.isAtPortAndDocked()).thenReturn(true);

        FishState model = mock(FishState.class);
        when(model.getDayOfTheYear(anyInt())).thenReturn(5);
        Assertions.assertTrue(pause.allowedAtSea(fisher, model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(15);
        Assertions.assertTrue(!pause.allowedAtSea(fisher, model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(105);
        Assertions.assertTrue(!pause.allowedAtSea(fisher, model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(205);
        Assertions.assertTrue(pause.allowedAtSea(fisher, model));


        //if the fisher is not at port, the regulation doesn't matter
        when(fisher.isAtPortAndDocked()).thenReturn(false);

        when(model.getDayOfTheYear(anyInt())).thenReturn(5);
        Assertions.assertTrue(pause.allowedAtSea(fisher, model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(15);
        Assertions.assertTrue(pause.allowedAtSea(fisher, model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(105);
        Assertions.assertTrue(pause.allowedAtSea(fisher, model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(205);
        Assertions.assertTrue(pause.allowedAtSea(fisher, model));


    }
}

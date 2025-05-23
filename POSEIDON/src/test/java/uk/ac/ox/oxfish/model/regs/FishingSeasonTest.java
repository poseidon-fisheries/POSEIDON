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

package uk.ac.ox.oxfish.model.regs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishingSeasonTest {


    @Test
    public void fishHere() throws Exception {

        //
        FishingSeason season = new FishingSeason(true, 100);
        Fisher fisher = mock(Fisher.class);
        SeaTile tile = mock(SeaTile.class);
        FishState model = mock(FishState.class);

        //right season, not protected===> can fish
        when(model.getDayOfTheYear(anyInt())).thenReturn(50);
        when(tile.isProtected()).thenReturn(false);
        Assertions.assertTrue(season.canFishHere(fisher, tile, model));

        //off season ===> can't fish
        when(model.getDayOfTheYear(anyInt())).thenReturn(150);
        when(tile.isProtected()).thenReturn(false);
        Assertions.assertTrue(season.canFishHere(fisher, tile, model));// not an MPA problem
        Assertions.assertFalse(season.allowedAtSea(fisher, model));

        //on season, protected ===> can't fish
        when(model.getDayOfTheYear(anyInt())).thenReturn(50);
        when(tile.isProtected()).thenReturn(true);
        Assertions.assertFalse(season.canFishHere(fisher, tile, model));
        //turnOff caring about mpas
        season = new FishingSeason(false, 100);
        Assertions.assertTrue(season.canFishHere(fisher, tile, model));

    }
}

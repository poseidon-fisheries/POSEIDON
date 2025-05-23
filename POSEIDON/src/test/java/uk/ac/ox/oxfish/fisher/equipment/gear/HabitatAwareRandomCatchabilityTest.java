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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.Mockito.*;


public class HabitatAwareRandomCatchabilityTest {


    @Test
    public void correct() throws Exception {


        HabitatAwareRandomCatchability gear = new HabitatAwareRandomCatchability(
            new double[]{.1},
            new double[]{0},
            new double[]{.2},
            new double[]{0},
            1
        );


        SeaTile tile = mock(SeaTile.class);
        Species species = new Species("0");
        GlobalBiology biology = new GlobalBiology(species);
        when(tile.getBiomass(species)).thenReturn(100d);
        when(tile.getRockyPercentage()).thenReturn(1d);


        Fisher fisher = mock(Fisher.class);
        when(fisher.grabRandomizer()).thenReturn(new MersenneTwisterFast());
        Catch fishCaught = gear.fish(fisher, tile, tile, 1, biology);
        //gear does not kill off the fish
        verify(tile, never()).reactToThisAmountOfBiomassBeingFished(any(), any(), any());

        Assertions.assertEquals(20, fishCaught.getWeightCaught(species), .01);
        when(tile.getRockyPercentage()).thenReturn(0d);
        Assertions.assertEquals(10, gear.fish(fisher, tile, tile, 1, biology).getWeightCaught(species), .01);


    }

    @Test
    public void expectationsDoNotKillFish() throws Exception {


        HabitatAwareRandomCatchability gear = new HabitatAwareRandomCatchability(
            new double[]{.1},
            new double[]{0},
            new double[]{.2},
            new double[]{0},
            1
        );


        SeaTile tile = mock(SeaTile.class);
        Species species = new Species("0");
        GlobalBiology biology = new GlobalBiology(species);
        when(tile.getBiomass(species)).thenReturn(100d);
        when(tile.getRockyPercentage()).thenReturn(1d);


        Fisher fisher = mock(Fisher.class);
        when(fisher.grabRandomizer()).thenReturn(new MersenneTwisterFast());
        double[] fishCaught = gear.expectedHourlyCatch(fisher, tile, 1, biology);
        verify(tile, never()).reactToThisAmountOfBiomassBeingFished(any(), any(), any());
        Assertions.assertEquals(20, fishCaught[0], .01);
    }

}

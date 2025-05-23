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
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class RandomCatchabilityTrawlFactoryTest {

    @Test
    public void catchability() throws Exception {

        Fisher mock = mock(Fisher.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(mock.grabRandomizer()).thenReturn(random);

        RandomCatchabilityTrawl thrawl = new RandomCatchabilityTrawl(
            new double[]{.5},
            new double[]{.1},
            100
        );

        Boat boat = mock(Boat.class);
        SeaTile tile = mock(SeaTile.class);
        double fuelConsumed = thrawl.getFuelConsumptionPerHourOfFishing(
            mock,
            boat,
            tile
        );

        Assertions.assertEquals(fuelConsumed, 100d, .0001d);


        when(tile.getBiomass(any())).thenReturn(1000d);
        when(random.nextGaussian()).thenReturn(0d); //no deviation

        GlobalBiology biology = new GlobalBiology(new Species("test"));
        Assertions.assertEquals(500, thrawl.fish(mock, tile, tile, 1, biology).getWeightCaught(0), .0001d);
        verify(tile, never()).reactToThisAmountOfBiomassBeingFished(any(),
            any(),
            any()); //gear alone doesn't kill off fish
        when(random.nextGaussian()).thenReturn(2d); //no deviation
        Assertions.assertEquals(700, thrawl.fish(mock, tile, tile, 1, biology).getWeightCaught(0), .0001d);

    }

    @Test
    public void expectationKillsNoOne() throws Exception {

        Fisher mock = mock(Fisher.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(mock.grabRandomizer()).thenReturn(random);

        RandomCatchabilityTrawl thrawl = new RandomCatchabilityTrawl(
            new double[]{.5},
            new double[]{.1},
            100
        );

        Boat boat = mock(Boat.class);
        SeaTile tile = mock(SeaTile.class);
        double fuelConsumed = thrawl.getFuelConsumptionPerHourOfFishing(
            mock,
            boat,
            tile
        );

        Assertions.assertEquals(fuelConsumed, 100d, .0001d);


        when(tile.getBiomass(any())).thenReturn(1000d);
        when(random.nextGaussian()).thenReturn(0d); //no deviation

        GlobalBiology biology = new GlobalBiology(new Species("test"));
        Assertions.assertEquals(500, thrawl.expectedHourlyCatch(mock, tile, 1, biology)[0], .0001d);
        when(random.nextGaussian()).thenReturn(2d); //no deviation
        Assertions.assertEquals(700, thrawl.expectedHourlyCatch(mock, tile, 1, biology)[0], .0001d);
        verify(tile, never()).reactToThisAmountOfBiomassBeingFished(any(),
            any(),
            any()); //gear alone doesn't kill off fish

    }
}

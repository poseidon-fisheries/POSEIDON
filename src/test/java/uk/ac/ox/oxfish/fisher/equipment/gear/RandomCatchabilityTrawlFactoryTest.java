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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class RandomCatchabilityTrawlFactoryTest {

    @Test
    public void catchability() throws Exception {

        Fisher mock = mock(Fisher.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(mock.grabRandomizer()).thenReturn(random);

        RandomCatchabilityTrawl thrawl = new RandomCatchabilityTrawl(new double[]{.5},
                                                                       new double[]{.1},
                                                                       100);

        Boat boat = mock(Boat.class);
        SeaTile tile = mock(SeaTile.class);
        double fuelConsumed = thrawl.getFuelConsumptionPerHourOfFishing(mock,
                                                                        boat,
                                                                        tile);

        assertEquals(fuelConsumed, 100d, .0001d);




        when(tile.getBiomass(any())).thenReturn(1000d);
        when(random.nextGaussian()).thenReturn(0d); //no deviation

        GlobalBiology biology = new GlobalBiology(new Species("test"));
        assertEquals(500, thrawl.fish(mock, tile,tile , 1, biology).getWeightCaught(0), .0001d);
        verify(tile,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any()); //gear alone doesn't kill off fish
        when(random.nextGaussian()).thenReturn(2d); //no deviation
        assertEquals(700, thrawl.fish(mock, tile,tile , 1, biology).getWeightCaught(0), .0001d);

    }

    @Test
    public void expectationKillsNoOne() throws Exception {

        Fisher mock = mock(Fisher.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        when(mock.grabRandomizer()).thenReturn(random);

        RandomCatchabilityTrawl thrawl = new RandomCatchabilityTrawl(new double[]{.5},
                                                                     new double[]{.1},
                                                                     100);

        Boat boat = mock(Boat.class);
        SeaTile tile = mock(SeaTile.class);
        double fuelConsumed = thrawl.getFuelConsumptionPerHourOfFishing(mock,
                                                                        boat,
                                                                        tile);

        assertEquals(fuelConsumed, 100d, .0001d);




        when(tile.getBiomass(any())).thenReturn(1000d);
        when(random.nextGaussian()).thenReturn(0d); //no deviation

        GlobalBiology biology = new GlobalBiology(new Species("test"));
        assertEquals(500, thrawl.expectedHourlyCatch(mock, tile, 1, biology)[0], .0001d);
        when(random.nextGaussian()).thenReturn(2d); //no deviation
        assertEquals(700, thrawl.expectedHourlyCatch(mock, tile, 1, biology)[0], .0001d);
        verify(tile,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any()); //gear alone doesn't kill off fish

    }
}
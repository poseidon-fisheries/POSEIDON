package uk.ac.ox.oxfish.fisher.equipment.gear;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
        when(boat.expectedFuelConsumption(100)).thenReturn(123d);
        SeaTile tile = mock(SeaTile.class);
        double fuelConsumed = thrawl.getFuelConsumptionPerHourOfFishing(mock,
                                                                        boat,
                                                                        tile);

        assertEquals(fuelConsumed, 123d, .0001d);




        when(tile.getBiomass(any())).thenReturn(1000d);
        when(random.nextGaussian()).thenReturn(0d); //no deviation

        GlobalBiology biology = new GlobalBiology(new Specie("test"));
        assertEquals(500, thrawl.fish(mock, tile, 1, biology).getPoundsCaught(0), .0001d);
        when(random.nextGaussian()).thenReturn(2d); //no deviation
        assertEquals(700, thrawl.fish(mock, tile, 1, biology).getPoundsCaught(0), .0001d);

    }
}
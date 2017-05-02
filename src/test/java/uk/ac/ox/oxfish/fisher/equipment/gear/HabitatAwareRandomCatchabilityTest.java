package uk.ac.ox.oxfish.fisher.equipment.gear;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.mockito.Mockito.*;


public class HabitatAwareRandomCatchabilityTest
{


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
        Catch fishCaught = gear.fish(fisher, tile, 1, biology);
        //gear does not kill off the fish
        verify(tile,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any());

        Assert.assertEquals(20, fishCaught.getWeightCaught(species), .01);
        when(tile.getRockyPercentage()).thenReturn(0d);
        Assert.assertEquals(10, gear.fish(fisher, tile, 1, biology).getWeightCaught(species), .01);







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
        verify(tile, never()).reactToThisAmountOfBiomassBeingFished(any(), any(),any());
        Assert.assertEquals(20, fishCaught[0], .01);
    }

}
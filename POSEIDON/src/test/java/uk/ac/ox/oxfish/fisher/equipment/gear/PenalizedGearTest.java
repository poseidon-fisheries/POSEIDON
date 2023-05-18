package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PenalizedGearTest {


    @Test
    public void limitsCorrectly() {


        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 100, 100}, 1);
        Meristics second = new FromListMeristics(new double[]{100, 100}, 1);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        Hold hold = mock(Hold.class);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getHold()).thenReturn(hold);
        //only 200 units left!
        when(hold.getMaximumLoad()).thenReturn(300000d);
        when(hold.getTotalWeightOfCatchInHold()).thenReturn(1000000d);

        //caught 500kg in total
        StructuredAbundance firstCatch = new StructuredAbundance(new double[]{1, 1, 1});
        StructuredAbundance secondCatch = new StructuredAbundance(new double[]{1, 1});
        Gear delegate = mock(Gear.class);
        when(delegate.fish(any(), any(), any(), anyInt(), any())).thenReturn(
            new Catch(
                new StructuredAbundance[]{firstCatch, secondCatch},
                bio
            )
        );

        PenalizedGear gear = new PenalizedGear(.1, delegate);
        SeaTile tile = mock(SeaTile.class);
        Catch haul = gear.fish(fisher, tile, tile, 100, bio);
        assertTrue(haul.hasAbundanceInformation());
        assertEquals(haul.getTotalWeight(), 450, .001);
        assertEquals(haul.getWeightCaught(firstSpecies), 270, .001);
//        assertEquals(haul.getWeightCaught(0),120d,.001);
//        assertEquals(haul.getWeightCaught(1),80d,.001);
    }
}
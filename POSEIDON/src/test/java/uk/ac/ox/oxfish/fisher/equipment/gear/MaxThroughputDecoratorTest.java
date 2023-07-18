package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MaxThroughputDecoratorTest {


    @Test
    public void limits() throws Exception {

        Fisher fisher = mock(Fisher.class);

        //catches 100 units
        Gear delegate = mock(Gear.class);
        when(delegate.fish(any(), any(), any(), anyInt(), any())).thenReturn(
            new Catch(new double[]{70, 30})
        );

        MaxThroughputDecorator gear = new MaxThroughputDecorator(delegate, 50);
        SeaTile tile = mock(SeaTile.class);
        Catch haul = gear.fish(fisher, tile, tile, 100, mock(GlobalBiology.class));
        assertEquals(haul.getTotalWeight(), 50d, .001);
        assertEquals(haul.getWeightCaught(0), 35d, .001);
        assertEquals(haul.getWeightCaught(1), 15d, .001);

        assertFalse(haul.hasAbundanceInformation());

    }

    @Test
    public void limitsWithAbundance() throws Exception {

        //set up copied from the holdsize test
        Meristics first = new FromListMeristics(new double[]{100, 100, 100}, 1);
        Meristics second = new FromListMeristics(new double[]{100, 100}, 1);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second", second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        Fisher fisher = mock(Fisher.class);

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

        MaxThroughputDecorator gear = new MaxThroughputDecorator(
            delegate,
            200
        );
        SeaTile tile = mock(SeaTile.class);
        Catch haul = gear.fish(fisher, tile, tile, 100, bio);
        assertTrue(haul.hasAbundanceInformation());
        assertEquals(haul.getTotalWeight(), 200d, .001);
        assertEquals(haul.getWeightCaught(0), 120d, .001);
        assertEquals(haul.getWeightCaught(1), 80d, .001);

    }

}
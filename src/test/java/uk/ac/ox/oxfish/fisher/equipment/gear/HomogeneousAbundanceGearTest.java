package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 3/10/16.
 */
public class HomogeneousAbundanceGearTest
{


    @Test
    public void catchesCorrectly() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(.5),
                                                                     new FixedProportionFilter(.5));

        SeaTile tile = mock(SeaTile.class);
        when(tile.getNumberOfFemaleFishPerAge(any())).thenReturn(new int[]{100});
        when(tile.getNumberOfMaleFishPerAge(any())).thenReturn(new int[]{0});
        Species species = new Species("test",Meristics.FAKE_MERISTICS);
        GlobalBiology biology = new GlobalBiology(species);

        Catch fish = gear.fish(mock(Fisher.class), tile, 1, biology);
        assertEquals(fish.getPoundsCaught(0), FishStateUtilities.weigh(new int[]{0},new int[]{25},species),.001);

        verify(tile).reactToThisAmountOfFishBeingCaught(species,new int[]{0},new int[]{25});


    }


    @Test
    public void halfAnHour() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(1),
                                                                     new FixedProportionFilter(.5));

        SeaTile tile = mock(SeaTile.class);
        when(tile.getNumberOfFemaleFishPerAge(any())).thenReturn(new int[]{100});
        when(tile.getNumberOfMaleFishPerAge(any())).thenReturn(new int[]{0});
        Species species = new Species("test",Meristics.FAKE_MERISTICS);
        GlobalBiology biology = new GlobalBiology(species);

        Catch fish = gear.fish(mock(Fisher.class), tile, .5, biology);

        assertEquals(fish.getPoundsCaught(0), FishStateUtilities.weigh(new int[]{0},new int[]{25},species),.001);
        verify(tile).reactToThisAmountOfFishBeingCaught(species,new int[]{0},new int[]{25});


    }

    @Test
    public void oneHourAndAHalf() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(1),
                                                                     new FixedProportionFilter(.5));

        SeaTile tile = mock(SeaTile.class);
        when(tile.getNumberOfFemaleFishPerAge(any())).thenReturn(new int[]{100});
        when(tile.getNumberOfMaleFishPerAge(any())).thenReturn(new int[]{0});
        Species species = new Species("test",Meristics.FAKE_MERISTICS);
        GlobalBiology biology = new GlobalBiology(species);

        Catch fish = gear.fish(mock(Fisher.class), tile, 1.5, biology);

        //you are going to catch 50 on the first hour and 25 in the other half (this is because seatile is mocked and doesn't kill off biology)
        assertEquals(fish.getPoundsCaught(0), FishStateUtilities.weigh(new int[]{0},new int[]{50+25},species),.001);
        verify(tile).reactToThisAmountOfFishBeingCaught(species,new int[]{0},new int[]{50});
        verify(tile).reactToThisAmountOfFishBeingCaught(species,new int[]{0},new int[]{25});


    }
}
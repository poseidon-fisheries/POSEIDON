package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/6/17.
 */
public class StandardAgingProcessTest {


    @Test
    public void oldFishDies() throws Exception {

        Species species = mock(Species.class);
        when(species.getMaxAge()).thenReturn(2);
        int[] male = {10, 20, 30};
        int[] female = {100, 200, 300};

        StandardAgingProcess process = new StandardAgingProcess(false);

        AbundanceBasedLocalBiology bio = mock(AbundanceBasedLocalBiology.class);
        when(bio.getNumberOfMaleFishPerAge(species)).thenReturn(male);
        when(bio.getNumberOfFemaleFishPerAge(species)).thenReturn(female);


        process.ageLocally(bio,species,null);

        assertArrayEquals(male,new int[]{0,10,20});
        assertArrayEquals(female,new int[]{0,100,200});

    }


    @Test
    public void oldFishStays() throws Exception {

        Species species = mock(Species.class);
        when(species.getMaxAge()).thenReturn(2);
        int[] male = {10, 20, 30};
        int[] female = {100, 200, 300};

        StandardAgingProcess process = new StandardAgingProcess(true);

        AbundanceBasedLocalBiology bio = mock(AbundanceBasedLocalBiology.class);
        when(bio.getNumberOfMaleFishPerAge(species)).thenReturn(male);
        when(bio.getNumberOfFemaleFishPerAge(species)).thenReturn(female);


        process.ageLocally(bio,species,null);

        assertArrayEquals(male,new int[]{0,10,50});
        assertArrayEquals(female,new int[]{0,100,500});

    }
}
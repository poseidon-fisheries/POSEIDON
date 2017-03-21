package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 3/21/17.
 */
public class ArrayFilterTest {


    @Test
    public void arrayFilter() throws Exception {

        ArrayFilter filter = new ArrayFilter(new double[]{.5, .3}, new double[]{.1, .2});
        int[][] filtered = filter.filter(new int[]{100, 100}, new int[]{1000, 1000}, mock(Species.class));


        assertEquals(filtered[FishStateUtilities.FEMALE][0],100);
        assertEquals(filtered[FishStateUtilities.FEMALE][1],200);
        assertEquals(filtered[FishStateUtilities.MALE][0],50);
        assertEquals(filtered[FishStateUtilities.MALE][1],30);


    }
}
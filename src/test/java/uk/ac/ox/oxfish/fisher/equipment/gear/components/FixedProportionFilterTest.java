package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;



public class FixedProportionFilterTest
{


    @Test
    public void filtersCorrectly() throws Exception {


        Species species = new Species("Longspine", new StockAssessmentCaliforniaMeristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                          0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                          0.111313, 17.826, -1.79, 1,
                                                                                          0, 168434124,
                                                                                          0.6, false));
        FixedProportionFilter filter = new FixedProportionFilter(.2);

        int[] male = new int[81];
        int[] female = new int[81];
        male[20] = 100;

        int[][] filtered = filter.filter(male, female,species);
        assertEquals(filtered[FishStateUtilities.MALE][20],20);
        assertEquals(filtered[FishStateUtilities.MALE][21],0);

        filter = new FixedProportionFilter(1);
        filtered = filter.filter(male, female,species);
        assertEquals(filtered[FishStateUtilities.MALE][20],100);
        assertEquals(filtered[FishStateUtilities.MALE][21],0);
    }
}
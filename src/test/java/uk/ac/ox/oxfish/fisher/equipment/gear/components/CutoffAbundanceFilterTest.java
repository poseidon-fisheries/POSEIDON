package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 3/11/16.
 */
public class CutoffAbundanceFilterTest {

    /**
     * numbers come from the spreadsheet and therefore stock assessment.
     */
    @Test
    public void computesCorrectly() throws Exception {
        Species species = new Species("Longspine", new Meristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                 0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                 0.111313, 17.826, -1.79, 1,
                                                                 0, 168434124,
                                                                 0.6, false));
        CutoffAbundanceFilter filter = new CutoffAbundanceFilter(10,false);
        double[][] selectivity = filter.getProbabilityMatrix(species);
        assertEquals(selectivity[FishStateUtilities.MALE][3], 1, .001);
        assertEquals(selectivity[FishStateUtilities.FEMALE][20],0,.001);
        filter = new CutoffAbundanceFilter(10,true);
        selectivity = filter.getProbabilityMatrix(species);
        assertEquals(selectivity[FishStateUtilities.MALE][3], 0, .001);
        assertEquals(selectivity[FishStateUtilities.FEMALE][20],1,.001);

    }





    @Test
    public void filtersCorrectly() throws Exception {
        Species species = new Species("Longspine",new Meristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                0.111313, 17.826, -1.79, 1,
                                                                0, 168434124,
                                                                0.6, false));
        CutoffAbundanceFilter filter = new CutoffAbundanceFilter(10,true);

        int[] male = new int[81];
        int[] female = new int[81];
        male[5] = 100;
        int[][] filtered = filter.filter(male, female, species);
        assertEquals(filtered[FishStateUtilities.MALE][5],100);
        assertEquals(filtered[FishStateUtilities.MALE][0],0);
        assertEquals(filtered[FishStateUtilities.FEMALE][5],0);


    }


}
package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import static org.junit.Assert.assertEquals;


public class AbundanceBasedLocalBiologyTest
{

    final static private StockAssessmentCaliforniaMeristics meristics=  new StockAssessmentCaliforniaMeristics(80, 40 , 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                                               0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                                                               0.111313, 17.826, -1.79, 1,
                                                                                                               0, 168434124,
                                                                                                               0.6, false);


    @Test
    public void longspineTotalBiomass() throws Exception
    {


        Species longspine = new Species("Longspine",meristics);
        GlobalBiology biology = new GlobalBiology(longspine);

        AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(biology);

        //can modify directly
        local.getNumberOfFemaleFishPerAge(longspine)[5]=100;
        local.getNumberOfMaleFishPerAge(longspine)[5]=200;
        local.getNumberOfMaleFishPerAge(longspine)[6]=100;

        assertEquals(local.getBiomass(longspine),
                     100 * 0.019880139 +
                             200 * 0.019880139 +
                             100 * 0.0300262838,
                     .001);





    }


    @Test
    public void fishOut() throws Exception {


        Species longspine = new Species("Longspine",meristics);
        GlobalBiology biology = new GlobalBiology(longspine);

        AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(biology);

        //can modify directly
        local.getNumberOfFemaleFishPerAge(longspine)[5]=100;
        local.getNumberOfMaleFishPerAge(longspine)[5]=200;
        local.getNumberOfMaleFishPerAge(longspine)[6]=100;

        int[] maleCatches = new int[longspine.getMaxAge()+1];
        int[] femaleCatches = new int[longspine.getMaxAge()+1];
        maleCatches[6] = 50;
        local.reactToThisAmountOfBiomassBeingFished(new Catch(maleCatches,femaleCatches,longspine,biology),
                                                    null,biology);

        assertEquals(local.getNumberOfFemaleFishPerAge(longspine)[5],100);
        assertEquals(local.getNumberOfFemaleFishPerAge(longspine)[6],0);
        assertEquals(local.getNumberOfMaleFishPerAge(longspine)[5],200);
        assertEquals(local.getNumberOfMaleFishPerAge(longspine)[6],50);

    }

}
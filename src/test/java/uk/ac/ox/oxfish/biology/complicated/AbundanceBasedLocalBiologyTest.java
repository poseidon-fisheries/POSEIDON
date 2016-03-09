package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AbundanceBasedLocalBiologyTest
{

    final static private Meristics meristics=  new Meristics(80,40 , 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
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
        local.reactToThisAmountOfFishBeingCaught(longspine,maleCatches,femaleCatches);

        assertEquals(local.getNumberOfFemaleFishPerAge(longspine)[5],100);
        assertEquals(local.getNumberOfFemaleFishPerAge(longspine)[6],0);
        assertEquals(local.getNumberOfMaleFishPerAge(longspine)[5],200);
        assertEquals(local.getNumberOfMaleFishPerAge(longspine)[6],50);

    }

    @Test
    public void fishOutByBiomass() throws Exception {

        //create fake species, lives for 10 years
        Species fake = mock(Species.class);
        GlobalBiology biology = new GlobalBiology(fake);
        when(fake.getMaxAge()).thenReturn(10);
        ImmutableList<Double> weight = mock(ImmutableList.class);
        //every age and sex weights 10 kg
        when(weight.get(anyInt())).thenReturn(10d);
        when(fake.getWeightFemaleInKg()).thenReturn(weight);
        when(fake.getWeightMaleInKg()).thenReturn(weight);

        AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(biology);
        //can modify directly
        local.getNumberOfFemaleFishPerAge(fake)[5]=100;
        local.getNumberOfMaleFishPerAge(fake)[5]=200;
        local.getNumberOfMaleFishPerAge(fake)[6]=100;
        assertEquals(local.getBiomass(fake),400*10,.001);


        local.reactToThisAmountOfBiomassBeingFished(fake,1100d);
        //should kill all male of age 6 and 10 male of age 5
        assertEquals(local.getNumberOfFemaleFishPerAge(fake)[5],100);
        assertEquals(local.getNumberOfFemaleFishPerAge(fake)[6],0);
        assertEquals(local.getNumberOfMaleFishPerAge(fake)[5],190);
        assertEquals(local.getNumberOfMaleFishPerAge(fake)[6],0);


    }
}
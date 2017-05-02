package uk.ac.ox.oxfish.biology;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesLogisticDelayGrowthBiologyTest {


    @Test
    public void nofishing() throws Exception {

        Species aaa = new Species("aaa");
        SingleSpecieLogisticDelayGrowthBiology biology = new SingleSpecieLogisticDelayGrowthBiology(
                aaa,
                1000,
                1032,
                3,
                2,
                0

        );

        //step it for 10 years
        for(int i=0; i<10;i++)
            biology.step(mock(FishState.class));

        Assert.assertEquals(1020,biology.getBiomass(aaa),.0001);

        //another 10 years and you hit the max
        for(int i=0; i<10;i++)
            biology.step(mock(FishState.class));

        Assert.assertEquals(1032,biology.getBiomass(aaa),.0001);


    }


    @Test
    public void suddenlyFishing() throws Exception {

        Species aaa = new Species("aaa");
        SingleSpecieLogisticDelayGrowthBiology biology = new SingleSpecieLogisticDelayGrowthBiology(
                aaa,
                1000,
                1032,
                3,
                3,
                3

        );
        Assert.assertEquals(1000,biology.getBiomass(aaa),.0001);
        GlobalBiology global = mock(GlobalBiology.class);
        when(global.getSize()).thenReturn(1);
        biology.reactToThisAmountOfBiomassBeingFished(new Catch(aaa, 200, global),
                                                      null, global);
        Assert.assertEquals(800,biology.getBiomass(aaa),.0001);


        //step it for 10 years
        for(int i=0; i<10;i++)
            biology.step(mock(FishState.class));

        Assert.assertEquals(829.8954875892,biology.getBiomass(aaa),.001);


    }
}
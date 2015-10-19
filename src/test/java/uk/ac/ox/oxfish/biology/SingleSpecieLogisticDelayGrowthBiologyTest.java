package uk.ac.ox.oxfish.biology;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;


public class SingleSpecieLogisticDelayGrowthBiologyTest {


    @Test
    public void nofishing() throws Exception {

        Specie aaa = new Specie("aaa");
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

        Specie aaa = new Specie("aaa");
        SingleSpecieLogisticDelayGrowthBiology biology = new SingleSpecieLogisticDelayGrowthBiology(
                aaa,
                1000,
                1032,
                3,
                3,
                3

        );
        Assert.assertEquals(1000,biology.getBiomass(aaa),.0001);
        biology.reactToThisAmountOfBiomassBeingFished(aaa,200d);
        Assert.assertEquals(800,biology.getBiomass(aaa),.0001);


        //step it for 10 years
        for(int i=0; i<10;i++)
            biology.step(mock(FishState.class));

        Assert.assertEquals(829.8954875892,biology.getBiomass(aaa),.001);


    }
}
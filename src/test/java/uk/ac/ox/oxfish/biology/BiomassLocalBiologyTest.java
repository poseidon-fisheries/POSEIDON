package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.growers.IndependentLogisticBiomassGrower;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class BiomassLocalBiologyTest
{
    @Test
    public void logisticGrowthWorks() throws Exception {

        BiomassLocalBiology bio = new BiomassLocalBiology(
                new Double[]{100d,200d}, new Double[]{100d,400d}
        );
        IndependentLogisticBiomassGrower grower = new IndependentLogisticBiomassGrower(new Double[]{.5,.5});
        grower.getBiologies().add(bio);
        Species species0 = new Species("0"); species0.resetIndexTo(0);
        Species species1 = new Species("1"); species1.resetIndexTo(1);
        Species species2 = new Species("2"); species2.resetIndexTo(2);

        assertEquals(100, bio.getBiomass(species0), .1);
        assertEquals(200, bio.getBiomass(species1), .1);
        assertEquals(0, bio.getBiomass(species2), .1);

        //grow it
        grower.step(mock(FishState.class));

        assertEquals(100, bio.getBiomass(species0), .1); //didn't grow because it is at capacity
        assertEquals(250, bio.getBiomass(species1), .1); //grew by 50%
        assertEquals(0, bio.getBiomass(species2), .1);  //0 doesn't grow

        bio.setCurrentBiomass(species1, 399.88);
        //grow it again
        grower.step(mock(FishState.class));
        assertEquals(100, bio.getBiomass(species0), .1); //didn't grow because it is at capacity
        assertEquals(400, bio.getBiomass(species1), .1); //grew until capacity
        assertEquals(0, bio.getBiomass(species2), .1);  //0 doesn't grow
    }


}
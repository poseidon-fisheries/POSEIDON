package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class LogisticLocalBiologyTest
{
    @Test
    public void logisticGrowthWorks() throws Exception {

        LogisticLocalBiology bio = new LogisticLocalBiology(
                new Double[]{100d,200d}, new Double[]{100d,400d}, new Double[]{.5,.5}
        );
        Specie specie0 = new Specie("0"); specie0.resetIndexTo(0);
        Specie specie1 = new Specie("1"); specie1.resetIndexTo(1);
        Specie specie2 = new Specie("2"); specie2.resetIndexTo(2);

        assertEquals(100, bio.getBiomass(specie0), .1);
        assertEquals(200, bio.getBiomass(specie1), .1);
        assertEquals(0, bio.getBiomass(specie2), .1);

        //grow it
        bio.step(mock(FishState.class));

        assertEquals(100,bio.getBiomass(specie0),.1); //didn't grow because it is at capacity
        assertEquals(250,bio.getBiomass(specie1),.1); //grew by 50%
        assertEquals(0, bio.getBiomass(specie2), .1);  //0 doesn't grow

        bio.setCurrentBiomass(specie1,399.88);
        //grow it again
        bio.step(mock(FishState.class));
        assertEquals(100,bio.getBiomass(specie0),.1); //didn't grow because it is at capacity
        assertEquals(400,bio.getBiomass(specie1),.1); //grew until capacity
        assertEquals(0, bio.getBiomass(specie2), .1);  //0 doesn't grow
    }

    @Test
    public void canAddSpecieshalfwaythrough() throws Exception {

        //starts with only specie0
        LogisticLocalBiology bio = new LogisticLocalBiology(
                new Double[]{100d}, new Double[]{100d}, new Double[]{.5d}
        );
        Specie specie0 = new Specie("0"); specie0.resetIndexTo(0);
        Specie specie2 = new Specie("2"); specie2.resetIndexTo(2);

        assertEquals(100, bio.getBiomass(specie0), .1);
        assertEquals(0, bio.getBiomass(specie2), .1);

        //nothing happens
        bio.step(mock(FishState.class));
        assertEquals(100, bio.getBiomass(specie0), .1);
        assertEquals(0, bio.getBiomass(specie2), .1);

        bio.setCarryingCapacity(specie2, 100);
        bio.setCurrentBiomass(specie2, 50);
        bio.setMalthusianParameter(specie2, .5);
        assertEquals(100, bio.getBiomass(specie0), .1);

        //added specie will work
        bio.step(mock(FishState.class));
        assertEquals(100, bio.getBiomass(specie0), .1);
        assertEquals(62.5, bio.getBiomass(specie2), .1);



    }
}
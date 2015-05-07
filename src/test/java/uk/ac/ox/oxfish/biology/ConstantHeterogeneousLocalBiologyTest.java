package uk.ac.ox.oxfish.biology;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConstantHeterogeneousLocalBiologyTest {

    @Test
    public void testManySpeciesBiology() throws Exception {

        ConstantHeterogeneousLocalBiology bio = new ConstantHeterogeneousLocalBiology(100,200,300);
        final Specie specie = mock(Specie.class);
        when(specie.getIndex()).thenReturn(0);
        assertEquals(100, bio.getBiomass(specie), .001);

        when(specie.getIndex()).thenReturn(1);
        assertEquals(200, bio.getBiomass(specie), .001);
        bio.reactToThisAmountOfBiomassBeingFished(specie,4000000d);
        assertEquals(200, bio.getBiomass(specie), .001);

    }
}
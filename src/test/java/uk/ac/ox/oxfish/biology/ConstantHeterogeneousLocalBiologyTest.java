package uk.ac.ox.oxfish.biology;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConstantHeterogeneousLocalBiologyTest {

    @Test
    public void testManySpeciesBiology() throws Exception {

        ConstantHeterogeneousLocalBiology bio = new ConstantHeterogeneousLocalBiology(100,200,300);
        final Species species = mock(Species.class);
        when(species.getIndex()).thenReturn(0);
        assertEquals(100, bio.getBiomass(species), .001);

        when(species.getIndex()).thenReturn(1);
        assertEquals(200, bio.getBiomass(species), .001);
        bio.reactToThisAmountOfBiomassBeingFished(species, 4000000d);
        assertEquals(200, bio.getBiomass(species), .001);

    }
}
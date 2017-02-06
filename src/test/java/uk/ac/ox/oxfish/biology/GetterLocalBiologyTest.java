package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class GetterLocalBiologyTest {


    @Test
    public void getter() throws Exception {

        Species species = mock(Species.class);
        GetterLocalBiology localBiology =
                new GetterLocalBiology(species,
                                       state -> 10 + state.getDay());

        FishState state = mock(FishState.class);
        localBiology.start(state);

        when(state.getDay()).thenReturn(0d);
        assertEquals(localBiology.getBiomass(species),10d,.001);
        when(state.getDay()).thenReturn(10d);
        assertEquals(localBiology.getBiomass(species),20d,.001);




    }
}
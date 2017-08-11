package uk.ac.ox.oxfish.biology;

import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.*;

public class UnfishableLocalBiologyDecoratorTest {


    @Test
    public void protects() throws Exception {


        LocalBiology decorated = mock(LocalBiology.class);
        UnfishableLocalBiologyDecorator decorator = new UnfishableLocalBiologyDecorator(
                1,
                decorated
        );

        FishState state = mock(FishState.class);
        when(state.getYear()).thenReturn(0);
        decorator.start(state);

        decorator.reactToThisAmountOfBiomassBeingFished(null,null,null);
        verify(decorated,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any());


        when(state.getYear()).thenReturn(1);
        decorator.reactToThisAmountOfBiomassBeingFished(null,null,null);
        verify(decorated,times(1)).reactToThisAmountOfBiomassBeingFished(any(),any(),any());
    }
}
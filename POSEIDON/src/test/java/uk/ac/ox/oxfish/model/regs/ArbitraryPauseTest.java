package uk.ac.ox.oxfish.model.regs;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbitraryPauseTest {


    @Test
    public void pause() {

        ArbitraryPause pause = new ArbitraryPause(10,110,new Anarchy());

        Fisher fisher = mock(Fisher.class);
        when(fisher.isAtPortAndDocked()).thenReturn(true);

        FishState model = mock(FishState.class);
        when(model.getDayOfTheYear(anyInt())).thenReturn(5);
        assertTrue(pause.allowedAtSea(fisher,model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(15);
        assertTrue(!pause.allowedAtSea(fisher,model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(105);
        assertTrue(!pause.allowedAtSea(fisher,model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(205);
        assertTrue(pause.allowedAtSea(fisher,model));


        //if the fisher is not at port, the regulation doesn't matter
        when(fisher.isAtPortAndDocked()).thenReturn(false);

        when(model.getDayOfTheYear(anyInt())).thenReturn(5);
        assertTrue(pause.allowedAtSea(fisher,model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(15);
        assertTrue(pause.allowedAtSea(fisher,model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(105);
        assertTrue(pause.allowedAtSea(fisher,model));
        when(model.getDayOfTheYear(anyInt())).thenReturn(205);
        assertTrue(pause.allowedAtSea(fisher,model));



    }
}
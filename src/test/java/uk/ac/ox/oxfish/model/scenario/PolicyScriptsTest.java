package uk.ac.ox.oxfish.model.scenario;

import org.junit.Test;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 5/3/16.
 */
public class PolicyScriptsTest {


    @Test
    public void callsCorrectly() throws Exception {

        HashMap<Integer,PolicyScript> map = new HashMap<>();

        PolicyScript tenYear = mock(PolicyScript.class);
        map.put(10, tenYear);
        PolicyScript twentyYear = mock(PolicyScript.class);
        map.put(20, twentyYear);

        PolicyScripts scripts = new PolicyScripts(map);

        FishState state = mock(FishState.class);
        when(state.scheduleEveryYear(any(),any())).thenReturn(mock(Stoppable.class));

        //check that starts schedules it
        scripts.start(state);
        verify(state).scheduleEveryYear(scripts, StepOrder.DAWN);

        when(state.getYear()).thenReturn(1);
        scripts.step(state);
        verify(tenYear,times(0)).apply(state);
        verify(twentyYear,times(0)).apply(state);

        //notice that the step happens when the year is -1 the specified one
        //since this step technically occurs the last step of the old year rather than the first step of the old one
        when(state.getYear()).thenReturn(9);
        scripts.step(state);
        verify(tenYear,times(1)).apply(state);
        verify(twentyYear,times(0)).apply(state);


        when(state.getYear()).thenReturn(13);
        scripts.step(state);
        verify(tenYear,times(1)).apply(state);
        verify(twentyYear,times(0)).apply(state);

        when(state.getYear()).thenReturn(19);
        scripts.step(state);
        verify(tenYear,times(1)).apply(state);
        verify(twentyYear,times(1)).apply(state);

        //if time goes back (why?) then it steps again
        when(state.getYear()).thenReturn(9);
        scripts.step(state);
        verify(tenYear,times(2)).apply(state);
        verify(twentyYear,times(1)).apply(state);






    }
}
package uk.ac.ox.oxfish.model;

import org.junit.Test;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.market.Markets;

import java.util.HashSet;

import static org.mockito.Mockito.*;


public class FishStateTest {

    @Test
    public void testScheduleEveryYear() throws Exception {

        //steps every 365 steps starting from 365
        Steppable steppable = mock(Steppable.class);

        FishState state = new FishState(1l);
        Scenario scenario = mock(Scenario.class);
        ScenarioResult result = mock(ScenarioResult.class);
        when(scenario.start(state)).thenReturn(result);
        NauticalMap map = mock(NauticalMap.class); when(result.getMap()).thenReturn(map);
        when(map.getPorts()).thenReturn(new HashSet<Port>());

        state.setScenario(scenario);
        state.start();
        state.scheduleEveryYear(steppable, StepOrder.AFTER_FISHER_PHASE);
        //should step twice
        for(int i=0; i<730; i++)
            state.schedule.step(state);
        verify(steppable,times(2)).step(state);

    }
}
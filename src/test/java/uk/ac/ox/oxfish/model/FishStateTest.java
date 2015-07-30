package uk.ac.ox.oxfish.model;

import org.junit.Test;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioEssentials;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;

import java.util.HashSet;

import static org.mockito.Mockito.*;


public class FishStateTest {

    @Test
    public void testScheduleEveryYear() throws Exception {

        //steps every 365 steps starting from 365
        Steppable steppable = mock(Steppable.class);

        FishState state = new FishState(1l);
        Scenario scenario = mock(Scenario.class);
        ScenarioEssentials result = mock(ScenarioEssentials.class);
        when(result.getBiology()).thenReturn(mock(GlobalBiology.class));
        when(scenario.start(state)).thenReturn(result);
        final ScenarioPopulation mock = mock(ScenarioPopulation.class);
        when(mock.getNetwork()).thenReturn(mock(SocialNetwork.class));
        when(scenario.populateModel(state)).thenReturn(mock);
        NauticalMap map = mock(NauticalMap.class); when(result.getMap()).thenReturn(map);
        when(map.getPorts()).thenReturn(new HashSet<>());

        state.setScenario(scenario);
        state.start();
        state.scheduleEveryYear(steppable, StepOrder.POLICY_UPDATE);
        state.scheduleEveryStep(simState -> {},StepOrder.AFTER_DATA);
        //should step twice
        for(int i=0; i<730; i++)
            state.schedule.step(state);
        verify(steppable,times(2)).step(state);

    }
}
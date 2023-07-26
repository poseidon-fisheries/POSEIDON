package uk.ac.ox.oxfish.model.scenario;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

class EpoPathPlannerAbundanceScenarioTest {
    @Test
    void runOneYearWithoutCrashing() {
        final FishState fishState = startTestableScenario(EpoPathPlannerAbundanceScenario.class);
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}
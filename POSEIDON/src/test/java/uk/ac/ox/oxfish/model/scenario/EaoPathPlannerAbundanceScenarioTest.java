package uk.ac.ox.oxfish.model.scenario;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

class EaoPathPlannerAbundanceScenarioTest {
    @Test
    void runOneYearWithoutCrashing() {
        final FishState fishState = startTestableScenario(EaoPathPlannerAbundanceScenario.class);
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }

    @Test
    void runTwoYearsWithoutCrashing() {
        final FishState fishState = startTestableScenario(EaoPathPlannerAbundanceScenario.class);
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 2);
        System.out.println("2 years done...");



    }


}

package uk.ac.ox.poseidon.simulation.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.poseidon.simulation.api.Simulation;

public class ScenarioAdaptor implements uk.ac.ox.poseidon.simulation.api.Scenario {
    private final Scenario delegate;

    ScenarioAdaptor(final Scenario scenario) {
        this.delegate = scenario;
    }

    @Override
    public Simulation newSimulation() {
        final FishState fishState = new FishState();
        fishState.setScenario(this.delegate);
        return new FishStateAdaptor(fishState);
    }
}

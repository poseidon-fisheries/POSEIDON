package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.Adaptor;
import uk.ac.ox.poseidon.simulations.api.Scenario;
import uk.ac.ox.poseidon.simulations.api.Simulation;

public abstract class ScenarioAdaptor<S extends uk.ac.ox.oxfish.model.scenario.Scenario>
    extends Adaptor<S> implements Scenario {

    protected ScenarioAdaptor(final S delegate) {
        super(delegate);
    }

    @Override
    public Simulation newSimulation() {
        final FishState fishState = new FishState();
        fishState.setScenario(getDelegate());
        return new FishStateAdaptor(fishState);
    }
}

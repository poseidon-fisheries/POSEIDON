package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

public abstract class EpoScenarioAdaptor<
    B extends LocalBiology,
    S extends EpoScenario<B>
    > extends ScenarioAdaptor<S> {
    EpoScenarioAdaptor(final S scenario) {
        super(scenario);
    }

}

package uk.ac.ox.poseidon.simulations.adaptors;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.AdaptorFactory;
import uk.ac.ox.poseidon.simulations.api.Scenario;

@AutoService(ScenarioAdaptorFactory.class)
public class ScenarioAdaptorFactory
    implements AdaptorFactory<uk.ac.ox.oxfish.model.scenario.Scenario, uk.ac.ox.poseidon.simulations.api.Scenario> {

    @Override
    public Class<uk.ac.ox.oxfish.model.scenario.Scenario> getDelegateClass() {
        return uk.ac.ox.oxfish.model.scenario.Scenario.class;
    }

    @Override
    public Scenario apply(final uk.ac.ox.oxfish.model.scenario.Scenario scenario) {
        return new ScenarioAdaptor(scenario);
    }
}

package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.EpoScenarioPathfinding;
import uk.ac.ox.poseidon.simulations.api.Scenario;

public class EpoScenarioPathfindingAdaptorFactory
    implements ScenarioAdaptorFactory<EpoScenarioPathfinding> {

    @Override
    public Scenario apply(final EpoScenarioPathfinding epoScenarioPathfinding) {
        return new EpoScenarioPathfindingAdaptor(epoScenarioPathfinding);
    }

    @Override
    public Class<EpoScenarioPathfinding> getDelegateClass() {
        return EpoScenarioPathfinding.class;
    }
}

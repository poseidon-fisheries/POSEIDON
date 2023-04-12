package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;
import uk.ac.ox.poseidon.simulations.api.Scenario;

public class EpoScenarioPathfindingAdaptorFactory
    implements ScenarioAdaptorFactory<EpoPathPlannerAbundanceScenario> {

    @Override
    public Scenario apply(final EpoPathPlannerAbundanceScenario epoPathplanningAbundanceScenario) {
        return new EpoScenarioPathfindingAdaptor(epoPathplanningAbundanceScenario);
    }

    @Override
    public Class<EpoPathPlannerAbundanceScenario> getDelegateClass() {
        return EpoPathPlannerAbundanceScenario.class;
    }
}

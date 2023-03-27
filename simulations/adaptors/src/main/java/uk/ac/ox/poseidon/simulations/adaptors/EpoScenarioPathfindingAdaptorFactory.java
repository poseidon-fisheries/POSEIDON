package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.scenario.EpoPathPlanningAbundanceScenario;
import uk.ac.ox.poseidon.simulations.api.Scenario;

public class EpoScenarioPathfindingAdaptorFactory
    implements ScenarioAdaptorFactory<EpoPathPlanningAbundanceScenario> {

    @Override
    public Scenario apply(final EpoPathPlanningAbundanceScenario epoPathplanningAbundanceScenario) {
        return new EpoScenarioPathfindingAdaptor(epoPathplanningAbundanceScenario);
    }

    @Override
    public Class<EpoPathPlanningAbundanceScenario> getDelegateClass() {
        return EpoPathPlanningAbundanceScenario.class;
    }
}

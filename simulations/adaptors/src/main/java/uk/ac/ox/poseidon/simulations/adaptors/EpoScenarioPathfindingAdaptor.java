package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;

public class EpoScenarioPathfindingAdaptor
    extends EpoScenarioAdaptor<AbundanceLocalBiology, EpoPathPlannerAbundanceScenario> {
    EpoScenarioPathfindingAdaptor(final EpoPathPlannerAbundanceScenario scenario) {
        super(scenario);
    }
}

package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlanningAbundanceScenario;

public class EpoScenarioPathfindingAdaptor
    extends EpoScenarioAdaptor<AbundanceLocalBiology, AbundanceFad, EpoPathPlanningAbundanceScenario> {
    EpoScenarioPathfindingAdaptor(final EpoPathPlanningAbundanceScenario scenario) {
        super(scenario);
    }
}

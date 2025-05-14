package uk.ac.ox.poseidon.agents.behaviours.disposition;

import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

public class ProportionallyLimitingBiomassToHoldFactory
    extends GlobalScopeFactory<ProportionallyLimitingBiomassToHold> {
    @Override
    protected ProportionallyLimitingBiomassToHold newInstance(final Simulation simulation) {
        return new ProportionallyLimitingBiomassToHold();
    }
}

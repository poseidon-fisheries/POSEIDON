package uk.ac.ox.poseidon.agents.behaviours.disposition;

import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

public class ProportionallyLimitBiomassToHoldFactory
    extends GlobalScopeFactory<ProportionallyLimitBiomassToHold> {
    @Override
    protected ProportionallyLimitBiomassToHold newInstance(final Simulation simulation) {
        return new ProportionallyLimitBiomassToHold();
    }
}

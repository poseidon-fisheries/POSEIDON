package uk.ac.ox.poseidon.agents.market;

import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.io.tables.SimulationEventListenerFactory;

public class BiomassSaleAccumulatorFactory
    extends SimulationEventListenerFactory<BiomassSaleAccumulator> {
    @Override
    protected BiomassSaleAccumulator newListener(Simulation simulation) {
        return new BiomassSaleAccumulator();
    }
}

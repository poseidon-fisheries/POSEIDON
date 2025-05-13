package uk.ac.ox.poseidon.agents.behaviours.fishing;

import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.io.tables.SimulationEventListenerFactory;

public class FishingActionAccumulatorFactory
    extends SimulationEventListenerFactory<FishingActionAccumulator> {
    @Override
    protected FishingActionAccumulator newListener(final Simulation simulation) {
        return new FishingActionAccumulator();
    }
}

package uk.ac.ox.poseidon.agents.tables;

import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.io.tables.SimulationEventListenerFactory;

public class MarketSalesListenerTableFactory
    extends SimulationEventListenerFactory<MarketSalesListenerTable> {
    @Override
    protected MarketSalesListenerTable newListener(Simulation simulation) {
        return new MarketSalesListenerTable();
    }
}

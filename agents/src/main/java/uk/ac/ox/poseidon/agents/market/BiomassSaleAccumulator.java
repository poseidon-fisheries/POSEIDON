package uk.ac.ox.poseidon.agents.market;

import uk.ac.ox.poseidon.core.events.EventAccumulator;

public class BiomassSaleAccumulator extends EventAccumulator<BiomassSale> {
    public BiomassSaleAccumulator() {
        super(BiomassSale.class);
    }
}

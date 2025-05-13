package uk.ac.ox.poseidon.agents.behaviours.fishing;

import uk.ac.ox.poseidon.core.events.EventAccumulator;

public class FishingActionAccumulator extends EventAccumulator<FishingAction> {
    public FishingActionAccumulator() {
        super(FishingAction.class);
    }
}

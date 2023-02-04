package uk.ac.ox.poseidon.simulation.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.simulation.api.Simulation;

public class FishStateAdaptor implements Simulation {

    private final FishState fishState;

    FishStateAdaptor(final FishState fishState) {
        this.fishState = fishState;
    }

    @Override
    public int getStep() {
        return fishState.getStep();
    }

    @Override
    public String getId() {
        return fishState.getTrulyUniqueID();
    }

    @Override
    public void step() {
        if (!fishState.isStarted()) {
            fishState.start();
        }
        fishState.schedule.step(fishState);
    }
}

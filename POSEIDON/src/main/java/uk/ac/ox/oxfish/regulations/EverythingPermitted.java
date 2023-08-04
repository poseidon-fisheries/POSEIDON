package uk.ac.ox.oxfish.regulations;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Mode;

import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class EverythingPermitted implements AlgorithmFactory<Mode> {
    @Override
    public Mode apply(final FishState fishState) {
        return PERMITTED;
    }
}

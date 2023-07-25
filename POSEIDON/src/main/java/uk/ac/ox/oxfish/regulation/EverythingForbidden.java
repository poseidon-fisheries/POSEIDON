package uk.ac.ox.oxfish.regulation;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;

public class EverythingForbidden implements AlgorithmFactory<Regulation> {
    @Override
    public Regulation apply(final FishState fishState) {
        return FORBIDDEN;
    }
}

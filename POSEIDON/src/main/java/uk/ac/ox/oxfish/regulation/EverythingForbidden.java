package uk.ac.ox.oxfish.regulation;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;

public class EverythingForbidden implements AlgorithmFactory<Regulations> {
    @Override
    public Regulations apply(final FishState fishState) {
        return FORBIDDEN;
    }
}

package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class EverythingPermittedRegulationFactory implements AlgorithmFactory<Regulation<Object>> {
    @Override
    public Regulation<Object> apply(final FishState fishState) {
        return PERMITTED;
    }
}

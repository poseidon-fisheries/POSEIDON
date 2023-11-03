package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.function.Supplier;

public interface RegulationFactory extends Supplier<AlgorithmFactory<Regulations>>, AlgorithmFactory<Regulations> {
    @Override
    default Regulations apply(final FishState fishState) {
        return get().apply(fishState);
    }
}

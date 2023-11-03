package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.util.function.Supplier;

public interface ConditionFactory extends Supplier<AlgorithmFactory<Condition>>, AlgorithmFactory<Condition> {
    @Override
    default Condition apply(final FishState fishState) {
        return get().apply(fishState);
    }
}

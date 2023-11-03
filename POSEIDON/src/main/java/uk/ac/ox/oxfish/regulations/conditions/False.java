package uk.ac.ox.oxfish.regulations.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;

public enum False implements AlgorithmFactory<Condition> {
    FALSE;

    // needed for YAML initialization
    public False getInstance() {
        return FALSE;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return uk.ac.ox.poseidon.regulations.core.conditions.False.FALSE;
    }
}

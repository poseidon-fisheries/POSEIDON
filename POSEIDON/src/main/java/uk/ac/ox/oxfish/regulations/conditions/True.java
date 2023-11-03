package uk.ac.ox.oxfish.regulations.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;

public enum True implements AlgorithmFactory<Condition> {
    TRUE;

    // needed for YAML initialization
    public True getInstance() {
        return TRUE;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return uk.ac.ox.poseidon.regulations.core.conditions.True.TRUE;
    }
}

package uk.ac.ox.oxfish.regulation;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulation;
import uk.ac.ox.poseidon.regulations.core.ConditionalRegulation;
import uk.ac.ox.poseidon.regulations.core.conditions.Condition;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ForbiddenIf implements AlgorithmFactory<Regulation> {
    private AlgorithmFactory<Condition> condition;

    @SuppressWarnings("unused")
    public ForbiddenIf() {
    }

    public ForbiddenIf(final AlgorithmFactory<Condition> condition) {
        this.condition = condition;
    }

    public AlgorithmFactory<Condition> getCondition() {
        return condition;
    }

    public void setCondition(final AlgorithmFactory<Condition> condition) {
        this.condition = condition;
    }

    @Override
    public Regulation apply(final FishState fishState) {
        return new ConditionalRegulation(
            condition.apply(fishState),
            FORBIDDEN,
            PERMITTED
        );
    }
}

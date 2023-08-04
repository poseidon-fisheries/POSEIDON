package uk.ac.ox.oxfish.regulations;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ConditionalRegulations;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ForbiddenIf implements AlgorithmFactory<Regulations> {
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
    public Regulations apply(final FishState fishState) {
        return new ConditionalRegulations(
            condition.apply(fishState),
            FORBIDDEN,
            PERMITTED
        );
    }
}

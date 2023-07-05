package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Regulation;
import uk.ac.ox.poseidon.regulations.core.ConditionalRegulation;

import java.util.function.Predicate;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ForbiddenIf implements AlgorithmFactory<Regulation> {
    private AlgorithmFactory<Predicate<Action>> condition;

    @SuppressWarnings("unused")
    public ForbiddenIf() {
    }

    public ForbiddenIf(final AlgorithmFactory<Predicate<Action>> condition) {
        this.condition = condition;
    }

    public AlgorithmFactory<Predicate<Action>> getCondition() {
        return condition;
    }

    public void setCondition(final AlgorithmFactory<Predicate<Action>> condition) {
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

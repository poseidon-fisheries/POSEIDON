package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

public class Not implements AlgorithmFactory<Predicate<Action>> {

    private AlgorithmFactory<Predicate<Action>> condition;

    public Not() {
    }

    public Not(final AlgorithmFactory<Predicate<Action>> condition) {
        this.condition = condition;
    }

    public AlgorithmFactory<Predicate<Action>> getCondition() {
        return condition;
    }

    public void setCondition(final AlgorithmFactory<Predicate<Action>> condition) {
        this.condition = condition;
    }

    @Override
    public Predicate<Action> apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.Not(condition.apply(fishState));
    }
}

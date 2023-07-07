package uk.ac.ox.oxfish.regulation.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class Not implements AlgorithmFactory<Condition> {

    private AlgorithmFactory<Condition> condition;

    public Not() {
    }

    public Not(final AlgorithmFactory<Condition> condition) {
        this.condition = condition;
    }

    public AlgorithmFactory<Condition> getCondition() {
        return condition;
    }

    public void setCondition(final AlgorithmFactory<Condition> condition) {
        this.condition = condition;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.Not(condition.apply(fishState));
    }
}

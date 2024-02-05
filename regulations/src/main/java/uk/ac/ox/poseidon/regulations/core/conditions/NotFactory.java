package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class NotFactory implements ComponentFactory<Condition> {

    private ComponentFactory<Condition> condition;

    public NotFactory() {
    }

    public NotFactory(final ComponentFactory<Condition> condition) {
        this.condition = condition;
    }

    public ComponentFactory<Condition> getCondition() {
        return condition;
    }

    public void setCondition(final ComponentFactory<Condition> condition) {
        this.condition = condition;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.Not(condition.apply(modelState));
    }
}

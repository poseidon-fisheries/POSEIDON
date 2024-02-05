package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ForbiddenIfFactory implements ComponentFactory<Regulations> {
    private ComponentFactory<Condition> condition;

    @SuppressWarnings("unused")
    public ForbiddenIfFactory() {
    }

    public ForbiddenIfFactory(final ComponentFactory<Condition> condition) {
        this.condition = condition;
    }

    public ComponentFactory<Condition> getCondition() {
        return condition;
    }

    public void setCondition(final ComponentFactory<Condition> condition) {
        this.condition = condition;
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        return new ConditionalRegulations(
            condition.apply(modelState),
            FORBIDDEN,
            PERMITTED
        );
    }
}

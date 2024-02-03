package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Condition;

import static uk.ac.ox.poseidon.regulations.core.conditions.False.FALSE;

public class FalseFactory implements ComponentFactory<Condition> {
    public FalseFactory() {
    }

    @Override
    public Condition apply(final ModelState ignored) {
        return FALSE;
    }
}

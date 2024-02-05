package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Condition;

import static uk.ac.ox.poseidon.regulations.core.conditions.True.TRUE;

public class TrueFactory implements ComponentFactory<Condition> {

    public TrueFactory() {
    }

    @Override
    public Condition apply(final ModelState ignored) {return TRUE;}
}

package uk.ac.ox.poseidon.regulations.core;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;

public abstract class Interdiction<C> extends ConditionnalRegulation<C> {
    public Interdiction() {
        super(FORBIDDEN);
    }
}

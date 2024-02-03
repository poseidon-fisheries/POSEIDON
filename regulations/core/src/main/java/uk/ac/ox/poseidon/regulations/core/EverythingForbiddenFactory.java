package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;

public class EverythingForbiddenFactory implements ComponentFactory<Regulations> {
    @Override
    public Regulations apply(final ModelState ignored) {
        return FORBIDDEN;
    }
}

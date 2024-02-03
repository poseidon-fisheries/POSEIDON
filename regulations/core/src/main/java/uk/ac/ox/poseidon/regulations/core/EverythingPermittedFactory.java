package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Mode;

import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class EverythingPermittedFactory implements ComponentFactory<Mode> {
    @Override
    public Mode apply(final ModelState ignored) {
        return PERMITTED;
    }
}

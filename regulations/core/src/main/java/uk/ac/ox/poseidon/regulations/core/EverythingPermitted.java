package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import static uk.ac.ox.poseidon.regulations.api.Regulations.Mode.PERMITTED;

public class EverythingPermitted<C> implements Regulations<C> {
    @Override
    public Mode mode(final Action action, final C context) {
        return PERMITTED;
    }
}

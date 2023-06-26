package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import static uk.ac.ox.poseidon.regulations.api.Regulations.Mode.*;

public interface Regulations<C> {

    default boolean isObligatory(final Action action, final C context) {
        return mode(action, context) == OBLIGATORY;
    }

    Mode mode(Action action, C context);

    default boolean isPermitted(final Action action, final C context) {
        final Mode mode = mode(action, context);
        return mode == PERMITTED || mode == OBLIGATORY;
    }

    default boolean isForbidden(final Action action, final C context) {
        return mode(action, context) == FORBIDDEN;
    }

    enum Mode {
        OBLIGATORY, PERMITTED, FORBIDDEN
    }
}

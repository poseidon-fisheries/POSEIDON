package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import static uk.ac.ox.poseidon.regulations.api.Regulation.Mode.*;

public interface Regulation<A extends Action, C> {

    default boolean isObligatory(final A action, final C context) {
        return mode(action, context) == OBLIGATORY;
    }

    Mode mode(A action, C context);

    default boolean isPermitted(final A action, final C context) {
        final Mode mode = mode(action, context);
        return mode == PERMITTED || mode == OBLIGATORY;
    }

    default boolean isForbidden(final A action, final C context) {
        return mode(action, context) == FORBIDDEN;
    }

    enum Mode {
        OBLIGATORY, PERMITTED, FORBIDDEN
    }
}

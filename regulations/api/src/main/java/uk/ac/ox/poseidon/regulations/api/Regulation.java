package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.MANDATORY;

public interface Regulation {

    default boolean isPermitted(final Action action) {
        // the action is permitted of the mode is either
        // PERMITTED or MANDATORY, but not FORBIDDEN
        return !isForbidden(action);
    }

    default boolean isForbidden(final Action action) {
        return mode(action) == FORBIDDEN;
    }

    Mode mode(Action action);

    default boolean isMandatory(final Action action) {
        return mode(action) == MANDATORY;
    }

}

package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.stream.Stream;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.MANDATORY;

public interface Regulation<C> {

    default boolean isPermitted(final Action action, final C context) {
        // the action is permitted of the mode is either
        // PERMITTED or MANDATORY, but not FORBIDDEN
        return !isForbidden(action, context);
    }

    default boolean isForbidden(final Action action, final C context) {
        return mode(action, context) == FORBIDDEN;
    }

    Mode mode(Action action, C context);

    default boolean isMandatory(final Action action, final C context) {
        return mode(action, context) == MANDATORY;
    }

    default Stream<Regulation<?>> asStream() {
        return Stream.of(this);
    }

}

package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ForbiddenIf extends ConditionalRegulation {
    ForbiddenIf(final Predicate<? super Action> condition) {
        super(condition, FORBIDDEN, PERMITTED);
    }
}

package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.regulations.core.conditions.Condition;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ForbiddenIf extends ConditionalRegulation {
    ForbiddenIf(final Condition condition) {
        super(condition, FORBIDDEN, PERMITTED);
    }
}

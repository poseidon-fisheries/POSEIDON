package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.regulations.api.Condition;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class PermittedIf extends ConditionalRegulations {
    PermittedIf(final Condition condition) {
        super(condition, PERMITTED, FORBIDDEN);
    }
}

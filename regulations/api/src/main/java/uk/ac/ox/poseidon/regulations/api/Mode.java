package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import static java.util.Objects.requireNonNull;

public enum Mode implements Regulation {

    PERMITTED, FORBIDDEN, MANDATORY;

    public static Mode and(final Mode a, final Mode b) {
        requireNonNull(a);
        requireNonNull(b);
        if (a == PERMITTED) {
            if (b == PERMITTED) {
                return PERMITTED;
            } else if (b == FORBIDDEN) {
                return FORBIDDEN;
            } else {  // b == MANDATORY
                return MANDATORY;
            }
        } else if (a == FORBIDDEN) {
            if (b == PERMITTED) {
                return FORBIDDEN;
            } else if (b == FORBIDDEN) {
                return FORBIDDEN;
            } else {  // b == MANDATORY
                throw new RuntimeException(a + " and " + b + " contradicted each other.");
            }
        } else {  // a == MANDATORY
            if (b == PERMITTED) {
                return MANDATORY;
            } else if (b == FORBIDDEN) {
                throw new RuntimeException(a + " and " + b + " contradicted each other.");
            } else {  // b == MANDATORY
                return MANDATORY;
            }
        }
    }

    static Mode not(final Mode a) {
        return requireNonNull(a) == PERMITTED ? FORBIDDEN : PERMITTED;
    }

    @Override
    public Mode mode(final Action action) {
        return this;
    }
}

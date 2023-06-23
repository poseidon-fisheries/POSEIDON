package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;

import static uk.ac.ox.poseidon.regulations.api.Regulation.Mode.*;

public class ConjunctiveRegulations<A extends Action, C> implements Regulation<A, C> {

    private final Collection<Regulation<A, C>> regulations;

    public ConjunctiveRegulations(final Collection<Regulation<A, C>> regulations) {
        this.regulations = ImmutableList.copyOf(regulations);
    }

    @Override
    public Mode mode(final A act, final C context) {
        final boolean forbidden = regulations.stream().anyMatch(r -> r.isForbidden(act, context));
        final boolean obligatory = regulations.stream().anyMatch(r -> r.isObligatory(act, context));
        if (forbidden && obligatory) {
            throw new IllegalStateException(
                "Inconsistent regulations: act " + act +
                    " is both forbidden and obligatory in context " + context
            );
        } else if (forbidden) {
            return FORBIDDEN;
        } else if (obligatory) {
            return OBLIGATORY;
        } else {
            return PERMITTED;
        }
    }
}

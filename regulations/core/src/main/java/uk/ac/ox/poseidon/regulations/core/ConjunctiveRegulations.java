package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.ac.ox.poseidon.regulations.api.Regulations.Mode.*;

public class ConjunctiveRegulations<C> implements Regulations<C> {

    private final Collection<Regulations<C>> regulations;

    public ConjunctiveRegulations(final Collection<Regulations<C>> regulations) {
        this.regulations = ImmutableList.copyOf(regulations);
    }

    @Override
    public Mode mode(final Action action, final C context) {
        final Set<Mode> modes =
            regulations.stream()
                .map(r -> r.mode(action, context))
                .collect(Collectors.toSet());
        final boolean forbidden = modes.contains(FORBIDDEN);
        final boolean obligatory = modes.contains(OBLIGATORY);
        if (forbidden && obligatory) {
            throw new IllegalStateException(
                "Inconsistent regulations: action " + action +
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

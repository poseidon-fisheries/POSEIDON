package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;

import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ConjunctiveRegulation<C> extends AbstractCompositeRegulation<C> {

    protected ConjunctiveRegulation(final Collection<Regulation<C>> subRegulations) {
        super(subRegulations);
    }

    @Override
    public Mode mode(final Action action, final C context) {
        return getSubRegulations()
            .stream()
            .map(r -> r.mode(action, context))
            .reduce(Mode::and)
            .orElse(PERMITTED);
    }
}

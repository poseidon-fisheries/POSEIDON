package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.Collection;

import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

public class ConjunctiveRegulation extends AbstractCompositeRegulation {

    public ConjunctiveRegulation(final Collection<Regulation> subRegulations) {
        super(subRegulations);
    }

    @Override
    public Mode mode(final Action action) {
        return getSubRegulations()
            .stream()
            .map(r -> r.mode(action))
            .reduce(Mode::and)
            .orElse(PERMITTED);
    }
}

package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static uk.ac.ox.poseidon.regulations.api.Mode.PERMITTED;

abstract class ConditionnalRegulation<C>
    implements Regulation<C>, BiPredicate<Action, C> {

    private final Regulation<? super C> delegate;

    public ConditionnalRegulation(
        final Regulation<? super C> delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    public Stream<Regulation<?>> asStream() {
        return Stream.concat(Stream.of(this), delegate.asStream());
    }

    @Override
    public Mode mode(final Action action, final C context) {
        return test(action, context)
            ? delegate.mode(action, context)
            : PERMITTED;
    }

}

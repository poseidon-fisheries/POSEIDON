package uk.ac.ox.poseidon.regulations.api;

import java.util.Collection;
import java.util.stream.Stream;

public interface CompositeRegulation<C> extends Regulation<C> {
    @Override
    default Stream<Regulation<?>> asStream() {
        return getSubRegulations().stream().flatMap(Regulation::asStream);
    }

    Collection<Regulation<C>> getSubRegulations();
}

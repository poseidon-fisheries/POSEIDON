package uk.ac.ox.poseidon.datasets.api;

import com.google.common.collect.Streams;

import java.util.stream.Stream;

public interface Column<T> extends Iterable<T> {
    String getName();

    default Stream<T> stream() {
        return Streams.stream(this);
    }
}

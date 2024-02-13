package uk.ac.ox.poseidon.common.core.csv;

import com.univocity.parsers.common.record.Record;

import java.util.function.Function;
import java.util.stream.Stream;

public interface RecordProcessor<V> extends Function<Stream<Record>, V> {
}

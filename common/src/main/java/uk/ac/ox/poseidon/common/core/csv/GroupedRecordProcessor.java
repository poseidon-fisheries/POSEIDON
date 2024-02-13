package uk.ac.ox.poseidon.common.core.csv;

import com.univocity.parsers.common.record.Record;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class GroupedRecordProcessor<G, V> implements RecordProcessor<Map<G, V>> {

    private final String groupColumnName;
    private final Function<? super String, ? extends G> groupColumnReader;
    private final RecordProcessor<? extends V> downstreamRecordProcessor;

    public GroupedRecordProcessor(
        final String groupColumnName,
        final Function<? super String, ? extends G> groupColumnReader,
        final RecordProcessor<? extends V> downstreamRecordProcessor
    ) {
        this.groupColumnName = groupColumnName;
        this.groupColumnReader = groupColumnReader;
        this.downstreamRecordProcessor = downstreamRecordProcessor;
    }

    @Override
    public Map<G, V> apply(final Stream<Record> records) {
        return records.collect(groupingBy(
            record -> groupColumnReader.apply(record.getString(groupColumnName)),
            collectingAndThen(
                toList(),
                recordGroup -> downstreamRecordProcessor.apply(recordGroup.stream())
            )
        ));
    }
}

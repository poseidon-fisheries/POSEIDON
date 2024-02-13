package uk.ac.ox.poseidon.geography;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.poseidon.common.core.csv.RecordProcessor;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.stream.Stream;

public class DoubleGridRecordProcessor implements RecordProcessor<DoubleGrid> {

    private final MapExtent mapExtent;
    private final String longitudeColumnName;
    private final String latitudeColumnName;
    private final String valueColumnName;

    public DoubleGridRecordProcessor(
        final MapExtent mapExtent,
        final String longitudeColumnName,
        final String latitudeColumnName,
        final String valueColumnName
    ) {
        this.mapExtent = mapExtent;
        this.longitudeColumnName = longitudeColumnName;
        this.latitudeColumnName = latitudeColumnName;
        this.valueColumnName = valueColumnName;
    }

    @Override
    public DoubleGrid apply(
        final Stream<Record> records
    ) {
        return DoubleGrid.fromRecords(
            mapExtent,
            records,
            longitudeColumnName,
            latitudeColumnName,
            valueColumnName
        );
    }
}

package uk.ac.ox.poseidon.common.core.temporal;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.csv.GroupedRecordProcessorFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class NavigableTemporalMapFromFileFactory<K, V>
    implements ComponentFactory<NavigableTemporalMap<K, V>> {

    private final Function<? super TemporalAccessor, ? extends K> temporalToKey;

    private InputPath filePath;
    private GroupedRecordProcessorFactory<K, V> groupedRecordProcessorFactory;

    public NavigableTemporalMapFromFileFactory(
        final Function<? super TemporalAccessor, ? extends K> temporalToKey,
        final InputPath filePath,
        final GroupedRecordProcessorFactory<K, V> groupedRecordProcessorFactory
    ) {
        this(temporalToKey);
        this.filePath = filePath;
        this.groupedRecordProcessorFactory = groupedRecordProcessorFactory;
    }

    public NavigableTemporalMapFromFileFactory(
        final Function<? super TemporalAccessor, ? extends K> temporalToKey
    ) {
        this.temporalToKey = temporalToKey;
    }

    public GroupedRecordProcessorFactory<K, V> getGroupedRecordProcessorFactory() {
        return groupedRecordProcessorFactory;
    }

    public void setGroupedRecordProcessorFactory(final GroupedRecordProcessorFactory<K, V> groupedRecordProcessorFactory) {
        this.groupedRecordProcessorFactory = groupedRecordProcessorFactory;
    }

    public InputPath getFilePath() {
        return filePath;
    }

    public void setFilePath(final InputPath filePath) {
        this.filePath = filePath;
    }

    @Override
    public NavigableTemporalMap<K, V> apply(final ModelState modelState) {
        return new NavigableTemporalMap<>(
            groupedRecordProcessorFactory
                .apply(modelState)
                .apply(recordStream(filePath.get())),
            temporalToKey
        );
    }
}

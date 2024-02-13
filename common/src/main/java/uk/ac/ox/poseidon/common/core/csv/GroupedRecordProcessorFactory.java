package uk.ac.ox.poseidon.common.core.csv;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;

import java.util.function.Function;

public class GroupedRecordProcessorFactory<G, V>
    implements ComponentFactory<GroupedRecordProcessor<G, V>> {

    private final Function<? super String, ? extends G> groupColumnReader;
    private StringParameter groupColumnName;
    private ComponentFactory<RecordProcessor<V>> downstreamRecordProcessor;

    public GroupedRecordProcessorFactory(
        final Function<? super String, ? extends G> groupColumnReader
    ) {
        this.groupColumnReader = groupColumnReader;
    }

    public GroupedRecordProcessorFactory(
        final Function<String, G> groupColumnReader,
        final StringParameter groupColumnName,
        final ComponentFactory<RecordProcessor<V>> downstreamRecordProcessor
    ) {
        this.groupColumnReader = groupColumnReader;
        this.groupColumnName = groupColumnName;
        this.downstreamRecordProcessor = downstreamRecordProcessor;
    }

    public ComponentFactory<RecordProcessor<V>> getDownstreamRecordProcessor() {
        return downstreamRecordProcessor;
    }

    public void setDownstreamRecordProcessor(final ComponentFactory<RecordProcessor<V>> downstreamRecordProcessor) {
        this.downstreamRecordProcessor = downstreamRecordProcessor;
    }

    public StringParameter getGroupColumnName() {
        return groupColumnName;
    }

    public void setGroupColumnName(final StringParameter groupColumnName) {
        this.groupColumnName = groupColumnName;
    }

    @Override
    public GroupedRecordProcessor<G, V> apply(final ModelState modelState) {
        return new GroupedRecordProcessor<>(
            groupColumnName.getValue(),
            groupColumnReader,
            downstreamRecordProcessor.apply(modelState)
        );
    }

}

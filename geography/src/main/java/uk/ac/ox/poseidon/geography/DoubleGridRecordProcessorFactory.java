package uk.ac.ox.poseidon.geography;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.csv.RecordProcessor;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;

public class DoubleGridRecordProcessorFactory
    implements ComponentFactory<RecordProcessor<DoubleGrid>> {

    private MapExtentFactory mapExtentFactory;
    private StringParameter longitudeColumnName;
    private StringParameter latitudeColumnName;
    private StringParameter valueColumnName;

    public DoubleGridRecordProcessorFactory() {
    }

    public DoubleGridRecordProcessorFactory(
        final StringParameter longitudeColumnName,
        final StringParameter latitudeColumnName,
        final StringParameter valueColumnName,
        final MapExtentFactory mapExtentFactory
    ) {
        this.mapExtentFactory = mapExtentFactory;
        this.longitudeColumnName = longitudeColumnName;
        this.latitudeColumnName = latitudeColumnName;
        this.valueColumnName = valueColumnName;
    }

    public MapExtentFactory getMapExtentFactory() {
        return mapExtentFactory;
    }

    public void setMapExtentFactory(final MapExtentFactory mapExtentFactory) {
        this.mapExtentFactory = mapExtentFactory;
    }

    public StringParameter getLongitudeColumnName() {
        return longitudeColumnName;
    }

    public void setLongitudeColumnName(final StringParameter longitudeColumnName) {
        this.longitudeColumnName = longitudeColumnName;
    }

    public StringParameter getLatitudeColumnName() {
        return latitudeColumnName;
    }

    public void setLatitudeColumnName(final StringParameter latitudeColumnName) {
        this.latitudeColumnName = latitudeColumnName;
    }

    public StringParameter getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(final StringParameter valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    @Override
    public DoubleGridRecordProcessor apply(final ModelState modelState) {
        return new DoubleGridRecordProcessor(
            mapExtentFactory.apply(modelState),
            longitudeColumnName.getValue(),
            latitudeColumnName.getValue(),
            valueColumnName.getValue()
        );
    }
}

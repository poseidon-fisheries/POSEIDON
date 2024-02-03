package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.function.Supplier;

public class MapExtentFactory implements AlgorithmFactory<MapExtent>, Supplier<MapExtent> {

    private IntegerParameter gridWidth;
    private IntegerParameter gridHeight;
    private IntegerParameter westLongitude;
    private IntegerParameter eastLongitude;
    private IntegerParameter southLatitude;
    private IntegerParameter northLatitude;

    public MapExtentFactory(
        final int gridWidth,
        final int gridHeight,
        final int westLongitude,
        final int eastLongitude,
        final int southLatitude,
        final int northLatitude
    ) {
        this.gridWidth = new IntegerParameter(gridWidth);
        this.gridHeight = new IntegerParameter(gridHeight);
        this.westLongitude = new IntegerParameter(westLongitude);
        this.eastLongitude = new IntegerParameter(eastLongitude);
        this.southLatitude = new IntegerParameter(southLatitude);
        this.northLatitude = new IntegerParameter(northLatitude);
    }

    public MapExtentFactory(
        final IntegerParameter gridWidth,
        final IntegerParameter gridHeight,
        final IntegerParameter westLongitude,
        final IntegerParameter eastLongitude,
        final IntegerParameter southLatitude,
        final IntegerParameter northLatitude
    ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.westLongitude = westLongitude;
        this.eastLongitude = eastLongitude;
        this.southLatitude = southLatitude;
        this.northLatitude = northLatitude;
    }

    public MapExtentFactory() {

    }

    public IntegerParameter getWestLongitude() {
        return westLongitude;
    }

    public void setWestLongitude(final IntegerParameter westLongitude) {
        this.westLongitude = westLongitude;
    }

    public IntegerParameter getEastLongitude() {
        return eastLongitude;
    }

    public void setEastLongitude(final IntegerParameter eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    public IntegerParameter getSouthLatitude() {
        return southLatitude;
    }

    public void setSouthLatitude(final IntegerParameter southLatitude) {
        this.southLatitude = southLatitude;
    }

    public IntegerParameter getNorthLatitude() {
        return northLatitude;
    }

    public void setNorthLatitude(final IntegerParameter northLatitude) {
        this.northLatitude = northLatitude;
    }

    public IntegerParameter getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(final IntegerParameter gridWidth) {
        this.gridWidth = gridWidth;
    }

    public IntegerParameter getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(final IntegerParameter gridHeight) {
        this.gridHeight = gridHeight;
    }

    @Override
    public MapExtent apply(final FishState fishState) {
        return get();
    }

    @Override
    public MapExtent get() {
        return MapExtent.from(
            gridWidth.getValue(),
            gridHeight.getValue(),
            new Envelope(
                westLongitude.getValue(),
                eastLongitude.getValue(),
                southLatitude.getValue(),
                northLatitude.getValue()
            )
        );
    }
}


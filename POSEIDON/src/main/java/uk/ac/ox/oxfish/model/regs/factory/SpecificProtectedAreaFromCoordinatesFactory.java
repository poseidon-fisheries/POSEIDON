package uk.ac.ox.oxfish.model.regs.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.oxfish.geography.MapExtent;

import java.util.function.BiPredicate;

public class SpecificProtectedAreaFromCoordinatesFactory extends SpecificProtectedAreaFactory {

    private double northLatitude;
    private double westLongitude;
    private double southLatitude;
    private double eastLongitude;

    public SpecificProtectedAreaFromCoordinatesFactory(
        final String name,
        final double northLatitude,
        final double westLongitude,
        final double southLatitude,
        final double eastLongitude
    ) {
        setName(name);
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;
    }

    @SuppressWarnings("unused")
    public SpecificProtectedAreaFromCoordinatesFactory() {
        this(1, 1, 1, 1);
    }

    public SpecificProtectedAreaFromCoordinatesFactory(
        final double northLatitude,
        final double westLongitude,
        final double southLatitude,
        final double eastLongitude
    ) {
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;
    }

    @Override
    BiPredicate<Integer, Integer> inAreaPredicate(final MapExtent mapExtent) {
        final Envelope envelope = new Envelope(
            new Coordinate(getWestLongitude(), getNorthLatitude()),
            new Coordinate(getEastLongitude(), getSouthLatitude())
        );
        return (x, y) -> envelope.covers(mapExtent.getCoordinates(x, y));
    }

    public double getWestLongitude() {
        return westLongitude;
    }

    public double getNorthLatitude() {
        return northLatitude;
    }

    @SuppressWarnings("unused")
    public void setNorthLatitude(final double northLatitude) {
        this.northLatitude = northLatitude;
    }

    public double getEastLongitude() {
        return eastLongitude;
    }

    public double getSouthLatitude() {
        return southLatitude;
    }

    @SuppressWarnings("unused")
    public void setSouthLatitude(final double southLatitude) {
        this.southLatitude = southLatitude;
    }

    @SuppressWarnings("unused")
    public void setEastLongitude(final double eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    @SuppressWarnings("unused")
    public void setWestLongitude(final double westLongitude) {
        this.westLongitude = westLongitude;
    }
}

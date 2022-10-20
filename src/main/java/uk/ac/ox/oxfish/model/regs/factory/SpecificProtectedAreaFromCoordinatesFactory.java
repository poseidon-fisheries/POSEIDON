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
        double northLatitude,
        double westLongitude,
        double southLatitude,
        double eastLongitude
    ) {
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;
    }

    @SuppressWarnings("unused")
    public SpecificProtectedAreaFromCoordinatesFactory() {
        this(1, 1, 1, 1);
    }

    public double getNorthLatitude() {
        return northLatitude;
    }

    @SuppressWarnings("unused")
    public void setNorthLatitude(double northLatitude) {
        this.northLatitude = northLatitude;
    }

    public double getWestLongitude() {
        return westLongitude;
    }

    @SuppressWarnings("unused")
    public void setWestLongitude(double westLongitude) {
        this.westLongitude = westLongitude;
    }

    public double getSouthLatitude() {
        return southLatitude;
    }

    @SuppressWarnings("unused")
    public void setSouthLatitude(double southLatitude) {
        this.southLatitude = southLatitude;
    }

    public double getEastLongitude() {
        return eastLongitude;
    }

    @SuppressWarnings("unused")
    public void setEastLongitude(double eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    @Override
    BiPredicate<Integer, Integer> inAreaPredicate(MapExtent mapExtent) {
        final Envelope envelope = new Envelope(
            new Coordinate(getWestLongitude(), getNorthLatitude()),
            new Coordinate(getEastLongitude(), getSouthLatitude())
        );
        return (x, y) -> envelope.covers(mapExtent.getCoordinates(x, y));
    }
}

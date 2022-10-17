package uk.ac.ox.oxfish.model.regs.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.BiPredicate;

public class SpecificProtectedAreaFromCoordinatesFactory extends SpecificProtectedAreaFactory {

    private DoubleParameter northLatitude;
    private DoubleParameter westLongitude;
    private DoubleParameter southLatitude;
    private DoubleParameter eastLongitude;

    public SpecificProtectedAreaFromCoordinatesFactory(
        double northLatitude,
        double westLongitude,
        double southLatitude,
        double eastLongitude
    ) {
        this.northLatitude = new FixedDoubleParameter(northLatitude);
        this.westLongitude = new FixedDoubleParameter(westLongitude);
        this.southLatitude = new FixedDoubleParameter(southLatitude);
        this.eastLongitude = new FixedDoubleParameter(eastLongitude);
    }

    @SuppressWarnings("unused")
    public SpecificProtectedAreaFromCoordinatesFactory() {
        this(1, 1, 1, 1);
    }

    @SuppressWarnings("unused")
    public DoubleParameter getNorthLatitude() {
        return northLatitude;
    }

    @SuppressWarnings("unused")
    public void setNorthLatitude(DoubleParameter northLatitude) {
        this.northLatitude = northLatitude;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getWestLongitude() {
        return westLongitude;
    }

    @SuppressWarnings("unused")
    public void setWestLongitude(DoubleParameter westLongitude) {
        this.westLongitude = westLongitude;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getSouthLatitude() {
        return southLatitude;
    }

    @SuppressWarnings("unused")
    public void setSouthLatitude(DoubleParameter southLatitude) {
        this.southLatitude = southLatitude;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getEastLongitude() {
        return eastLongitude;
    }

    @SuppressWarnings("unused")
    public void setEastLongitude(DoubleParameter eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    @Override
    BiPredicate<Integer, Integer> inAreaPredicate(FishState fishState) {
        final NauticalMap map = fishState.getMap();
        final MersenneTwisterFast rng = fishState.getRandom();
        final Envelope envelope = new Envelope(
            new Coordinate(westLongitude.apply(rng), northLatitude.apply(rng)),
            new Coordinate(eastLongitude.apply(rng), southLatitude.apply(rng))
        );
        return (x, y) -> envelope.covers(map.getCoordinates(x, y));
    }
}

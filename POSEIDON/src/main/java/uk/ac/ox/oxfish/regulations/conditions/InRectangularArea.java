package uk.ac.ox.oxfish.regulations.conditions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class InRectangularArea implements AlgorithmFactory<Condition> {

    private DoubleParameter northLatitude;
    private DoubleParameter westLongitude;
    private DoubleParameter southLatitude;
    private DoubleParameter eastLongitude;

    public InRectangularArea() {
    }

    public InRectangularArea(
        final double northLatitude,
        final double westLongitude,
        final double southLatitude,
        final double eastLongitude
    ) {
        this.northLatitude = new FixedDoubleParameter(northLatitude);
        this.westLongitude = new FixedDoubleParameter(westLongitude);
        this.southLatitude = new FixedDoubleParameter(southLatitude);
        this.eastLongitude = new FixedDoubleParameter(eastLongitude);
    }

    public InRectangularArea(
        final DoubleParameter northLatitude,
        final DoubleParameter westLongitude,
        final DoubleParameter southLatitude,
        final DoubleParameter eastLongitude
    ) {
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;
    }

    @Override
    public Condition apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new uk.ac.ox.poseidon.regulations.core.conditions.InRectangularArea(
            new Envelope(
                new Coordinate(
                    getWestLongitude().applyAsDouble(rng),
                    getNorthLatitude().applyAsDouble(rng)
                ),
                new Coordinate(
                    getEastLongitude().applyAsDouble(rng),
                    getSouthLatitude().applyAsDouble(rng)
                )
            )
        );
    }

    public DoubleParameter getWestLongitude() {
        return westLongitude;
    }

    public DoubleParameter getNorthLatitude() {
        return northLatitude;
    }

    public void setNorthLatitude(final DoubleParameter northLatitude) {
        this.northLatitude = northLatitude;
    }

    public DoubleParameter getEastLongitude() {
        return eastLongitude;
    }

    public DoubleParameter getSouthLatitude() {
        return southLatitude;
    }

    public void setSouthLatitude(final DoubleParameter southLatitude) {
        this.southLatitude = southLatitude;
    }

    public void setEastLongitude(final DoubleParameter eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    public void setWestLongitude(final DoubleParameter westLongitude) {
        this.westLongitude = westLongitude;
    }
}

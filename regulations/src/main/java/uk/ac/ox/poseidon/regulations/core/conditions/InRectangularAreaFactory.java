package uk.ac.ox.poseidon.regulations.core.conditions;

import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class InRectangularAreaFactory implements ComponentFactory<Condition> {

    private DoubleParameter northLatitude;
    private DoubleParameter westLongitude;
    private DoubleParameter southLatitude;
    private DoubleParameter eastLongitude;

    public InRectangularAreaFactory() {
    }

    public InRectangularAreaFactory(
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

    public InRectangularAreaFactory(
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
    public Condition apply(final ModelState modelState) {
        final MersenneTwisterFast rng = modelState.getRandom();
        return new uk.ac.ox.poseidon.regulations.core.conditions.InRectangularArea(
            new Envelope(
                getWestLongitude().applyAsDouble(rng),
                getEastLongitude().applyAsDouble(rng),
                getSouthLatitude().applyAsDouble(rng),
                getNorthLatitude().applyAsDouble(rng)
            )
        );
    }

    public DoubleParameter getWestLongitude() {
        return westLongitude;
    }

    public DoubleParameter getEastLongitude() {
        return eastLongitude;
    }

    public DoubleParameter getSouthLatitude() {
        return southLatitude;
    }

    public DoubleParameter getNorthLatitude() {
        return northLatitude;
    }

    public void setNorthLatitude(final DoubleParameter northLatitude) {
        this.northLatitude = northLatitude;
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

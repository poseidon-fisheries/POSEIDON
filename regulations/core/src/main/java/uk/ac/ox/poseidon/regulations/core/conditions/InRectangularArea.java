package uk.ac.ox.poseidon.regulations.core.conditions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class InRectangularArea extends CachedCoordinateCondition {
    private final Envelope envelope;

    public InRectangularArea(final Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    boolean test(final Coordinate coordinate) {
        return envelope.contains(coordinate);
    }
}

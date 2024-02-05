package uk.ac.ox.poseidon.regulations.core.conditions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class InRectangularArea extends CachedCoordinateCondition {
    private final Envelope envelope;

    InRectangularArea(final Envelope envelope) {
        this.envelope = envelope;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    @Override
    public boolean test(final Coordinate coordinate) {
        return envelope.contains(coordinate);
    }

    @Override
    public String toString() {
        return "InRectangularArea{" +
            "envelope=" + envelope +
            '}';
    }
}

package uk.ac.ox.oxfish.geography.fads;

import java.util.Map;
import java.util.function.Function;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

public class DriftingObjectMover implements Function<Double2D, Double2D> {

    private final Map<SeaTile, Double2D> currentVectors;
    private NauticalMap nauticalMap;

    DriftingObjectMover(
        NauticalMap nauticalMap,
        Map<SeaTile, Double2D> currentVectors
    ) {
        this.nauticalMap = nauticalMap;
        this.currentVectors = currentVectors;
    }

    private static final Double2D NULL_VECTOR = new Double2D(0, 0);

    @Override public Double2D apply(Double2D xy) {
        final SeaTile seaTile = nauticalMap.getSeaTile((int) xy.x, (int) xy.y);
        Double2D uv = currentVectors.getOrDefault(seaTile, NULL_VECTOR);
        return xy.add(uv);
    }
}

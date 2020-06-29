package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableMap;

public class DriftingPath {

    private final int initialTimeStep;
    private final Map<Integer, Optional<Double2D>> positions;
    private final CurrentVectors currentVectors;
    private final BiFunction<Integer, Integer, SeaTile> getSeaTile;

    public DriftingPath(
        int initialTimeStep,
        Double2D initialPosition,
        CurrentVectors currentVectors,
        BiFunction<Integer, Integer, SeaTile> getSeaTile
    ) {
        this.initialTimeStep = initialTimeStep;
        this.currentVectors = currentVectors;
        this.getSeaTile = getSeaTile;
        positions = new HashMap<>();
        positions.put(initialTimeStep, Optional.of(initialPosition));
    }

    public Optional<Double2D> position(int timeStep) {
        checkArgument(timeStep >= initialTimeStep);
        // can't use computeIfAbsent here because it doesn't handle recursion,
        // see e.g. https://stackoverflow.com/q/54824656/
        if (!positions.containsKey(timeStep)) {
            final Optional<Double2D> newPosition = position(timeStep - 1)
                .flatMap(previousPosition -> applyDrift(previousPosition, timeStep));
            positions.put(timeStep, newPosition);
        }
        return positions.get(timeStep);
    }

    Optional<Double2D> applyDrift(Double2D position, int timeStep) {
        final SeaTile tile = getSeaTile.apply((int) position.x, (int) position.y);
        final Optional<Double2D> vector = Optional.ofNullable(currentVectors.getVector(timeStep, tile));
        final Optional<Double2D> newPosition = vector.map(position::add);
        final Optional<SeaTile> newTile = newPosition.flatMap(p ->
            Optional.ofNullable(getSeaTile.apply((int) p.x, (int) p.y)).filter(SeaTile::isWater)
        );
        // only return the new position if it's on a tile
        return newTile.flatMap(__ -> newPosition);
    }

    public Map<Integer, Optional<Double2D>> getPositions() { return unmodifiableMap(positions); }
}

package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import sim.util.Int2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.unmodifiableMap;

public class DriftingPath {

    private final int initialTimeStep;
    private final Map<Integer, Optional<Double2D>> positions;
    private final CurrentVectors currentVectors;

    public DriftingPath(
        int initialTimeStep,
        Double2D initialPosition,
        CurrentVectors currentVectors
    ) {
        this.initialTimeStep = initialTimeStep;
        this.currentVectors = currentVectors;
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
        return getGridLocation(position)
            .map(gridLocation -> currentVectors.getVector(timeStep, gridLocation))
            .map(position::add)
            .filter(newPosition -> getGridLocation(newPosition).isPresent());
    }

    private Optional<Int2D> getGridLocation(Double2D position) {
        return position.x >= 0 && position.x < currentVectors.getGridWidth() &&
            position.y >= 0 && position.y < currentVectors.getGridHeight()
            ? Optional.of(new Int2D((int) position.x, (int) position.y))
            : Optional.empty();
    }

    public Map<Integer, Optional<Double2D>> getPositions() { return unmodifiableMap(positions); }
}

package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.Nullable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.DriftingPath;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.unmodifiableMap;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;
import static uk.ac.ox.oxfish.utility.MasonUtils.inBounds;

public class DriftingObjectsMap {

    private final Continuous2D field;
    private final CurrentVectors currentVectors;
    private final BiFunction<Integer, Integer, SeaTile> getSeaTile;
    private final Map<Object, DriftingPath> objectPaths = new HashMap<>();
    private final Map<Object, BiConsumer<Double2D, Optional<Double2D>>> onMoveCallbacks = new HashMap<>();
    DriftingObjectsMap(
        CurrentVectors currentVectors,
        NauticalMap nauticalMap
    ) {
        this(
            new Continuous2D(1.0, nauticalMap.getWidth(), nauticalMap.getHeight()),
            currentVectors,
            nauticalMap::getSeaTile
        );
    }

    private DriftingObjectsMap(
        Continuous2D field,
        CurrentVectors currentVectors,
        BiFunction<Integer, Integer, SeaTile> getSeaTile
    ) {
        this.field = field;
        this.currentVectors = currentVectors;
        this.getSeaTile = getSeaTile;
    }

    public DriftingPath getObjectPath(Object o) { return objectPaths.get(o); }

    public void applyDrift(int timeStep) {
        Bag objects = new Bag(field.allObjects); // make a copy, as objects can be removed
        bagToStream(objects).forEach(o -> {
            final Double2D oldLoc = field.getObjectLocationAsDouble2D(o);
            final Optional<Double2D> newLoc = objectPaths.get(o)
                .position(timeStep)
                .filter(location -> inBounds(location, field));
            if (newLoc.isPresent()) // TODO: use `ifPresentOrElse` once we upgrade to Java >=9.
                move(o, oldLoc, newLoc.get());
            else
                remove(o, oldLoc);
        });
        currentVectors.removeCachedVectors(timeStep);
    }

    private void move(Object object, Double2D oldLocation, Double2D newLocation) {
        setObjectLocation(object, newLocation);
        Optional
            .ofNullable(onMoveCallbacks.get(object))
            .ifPresent(f -> f.accept(oldLocation, Optional.of(newLocation)));
    }

    private void remove(Object object, Double2D oldLocation) {
        final Object result = field.remove(object);
        checkNotNull(result, "Object not on the map!");
        objectPaths.remove(object);
        Optional
            .ofNullable(onMoveCallbacks.remove(object))
            .ifPresent(f -> f.accept(oldLocation, Optional.empty()));
    }

    private void setObjectLocation(Object object, Double2D newLocation) {
        checkArgument(inBounds(newLocation, field));
        final boolean result = field.setObjectLocation(object, newLocation);
        checkState(result);
    }

    /**
     * Removes an object from the map. If a callback is registered for this object, the callback
     * function is applied with an empty {@code newLocation} argument.
     *
     * @param object the object to remove
     */
    public void remove(Object object) {
        remove(object, field.getObjectLocationAsDouble2D(object));
    }

    public void add(
        Object object,
        int timeStep,
        Double2D location,
        BiConsumer<Double2D, Optional<Double2D>> onMove
    ) {
        setObjectLocation(object, location);
        onMoveCallbacks.put(object, onMove);
        objectPaths.put(object, new DriftingPath(timeStep, location, currentVectors, getSeaTile));
    }

    @Nullable
    Double2D getObjectLocation(Object object) {
        return field.getObjectLocation(object);
    }

    public Stream<Object> objects() {
        return bagToStream(field.allObjects);
    }

    public Continuous2D getField() { return field; }

}

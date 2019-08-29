package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.Nullable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;
import static uk.ac.ox.oxfish.utility.MasonUtils.inBounds;

public class DriftingObjectsMap {

    private final Continuous2D field;
    private Map<Object, BiConsumer<Double2D, Optional<Double2D>>> onMoveCallbacks = new HashMap<>();

    DriftingObjectsMap(double width, double height) {
        this.field = new Continuous2D(1.0, width, height);
    }

    void applyDrift(Function<Double2D, Optional<Double2D>> moverFunction) {
        Bag objects = new Bag(field.allObjects); // make a copy, as objects can be removed
        bagToStream(objects).forEach(o -> {
            final Double2D oldLoc = field.getObjectLocationAsDouble2D(o);
            final Optional<Double2D> newLoc = moverFunction.apply(oldLoc);
            if (newLoc.isPresent()) // TODO: use `ifPresentOrElse` once we upgrade to Java >=9.
                move(o, oldLoc, newLoc.get());
            else
                remove(o, oldLoc);
        });
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
        Object object, Double2D location,
        BiConsumer<Double2D, Optional<Double2D>> onMove
    ) {
        setObjectLocation(object, location);
        onMoveCallbacks.put(object, onMove);
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

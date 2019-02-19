package uk.ac.ox.oxfish.geography.fads;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

public class DriftingObjectsMap {

    private final Continuous2D field;
    private final Function<Double2D, Double2D> move;
    private Map<Object, BiConsumer<Double2D, Optional<Double2D>>> onMoveCallbacks = new HashMap<>();

    DriftingObjectsMap(double width, double height, Function<Double2D, Double2D> move) {
        this.field = new Continuous2D(1.0, width, height);
        this.move = move;
    }

    private boolean inBounds(Double2D location) {
        return location.x >= 0 && location.x < field.getWidth() &&
            location.y >= 0 && location.y < field.getHeight();
    }

    void applyDrift() {
        Bag objects = new Bag(field.allObjects); // make a copy, as objects can be removed
        bagToStream(objects).forEach(o -> {
            final Double2D oldLoc = field.getObjectLocationAsDouble2D(o);
            final Double2D newLoc = move.apply(oldLoc);
            if (inBounds(newLoc)) {
                move(o, oldLoc, newLoc);
            }
            else {
                remove(o, oldLoc);
            }
        });
    }

    private void move(Object object, Double2D oldLocation, Double2D newLocation) {
        field.setObjectLocation(object, newLocation);
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

    /**
     * Removes an object from the map. If a callback is registered for this object, the callback
     * function is applied with an empty {@code newLocation} argument.
     *
     * @param object the object to remove
     */
    public void remove(Object object) {
        remove(object, field.getObjectLocationAsDouble2D(object));
    }

    public void add(Object object, Double2D location,
        BiConsumer<Double2D, Optional<Double2D>> onMove) {
        Preconditions.checkArgument(inBounds(location));
        field.setObjectLocation(object, location);
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

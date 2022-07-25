package uk.ac.ox.oxfish.geography.currents;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2016;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;

public class CurrentVectorsEPO implements CurrentVectors {

    public static final Double2D ZERO_VECTOR = new Double2D();

    private final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

    private final Map<Integer, Cache<Int2D, Optional<Double2D>>> vectorCache = new ConcurrentHashMap<>();
    private final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps;
    private final Function<? super Integer, CurrentPattern> currentPatternAtStep;
    private final int gridHeight;
    private final int gridWidth;
    private final int stepsPerDay;

    public CurrentVectorsEPO(
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps,
        final int stepsPerDay,
        final int gridWidth,
        final int gridHeight
    ) {
        this(vectorMaps, step -> step < 365 ? Y2016 : Y2017, gridWidth, gridHeight, stepsPerDay);
    }

    public CurrentVectorsEPO(
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps,
        final Function<? super Integer, CurrentPattern> currentPatternAtStep,
        final int gridWidth,
        final int gridHeight,
        final int stepsPerDay
    ) {
        this.vectorMaps = vectorMaps;
        this.currentPatternAtStep = currentPatternAtStep;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.stepsPerDay = stepsPerDay;
    }

    @NotNull
    public static Double2D getInterpolatedVector(
        final Double2D vectorBefore, final int offsetBefore,
        final Double2D vectorAfter, final int offsetAfter
    ) {
        final double totalOffset = (double) offsetBefore + offsetAfter;
        final Double2D v1 = vectorBefore.multiply((totalOffset - offsetBefore) / totalOffset);
        final Double2D v2 = vectorAfter.multiply((totalOffset - offsetAfter) / totalOffset);
        return v1.add(v2);
    }

    static int positiveDaysOffset(final int sourceDay, final int targetDay) {
        checkArgument(sourceDay >= 1 && sourceDay <= 365);
        checkArgument(targetDay >= 1 && targetDay <= 365);
        return sourceDay <= targetDay ?
            targetDay - sourceDay :
            (365 - sourceDay) + targetDay;
    }

    static int negativeDaysOffset(final int sourceDay, final int targetDay) {
        return -positiveDaysOffset(targetDay, sourceDay);
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    private int getDayOfTheYear(final int timeStep) {
        return ((timeStep / stepsPerDay) % 365) + 1;
    }

    @Override
    public Double2D getVector(final int step, final Int2D location) {
        try {
            return vectorMaps.isEmpty() ?
                ZERO_VECTOR :
                vectorCache
                    .computeIfAbsent(step, __ -> cacheBuilder.build())
                    .get(location, () -> computeVector(step, location))
                    .orElse(ZERO_VECTOR);
        } catch (final ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the current vector for seaTile at step. Returns an empty optional if we have no currents for that location.
     */
    private Optional<Double2D> computeVector(final int step, final Int2D location) {
        final int dayOfTheYear = getDayOfTheYear(step);
        if (vectorMaps.containsKey(dayOfTheYear)) {
            final Map<CurrentPattern, Map<Int2D, Double2D>> mapsAtStep = vectorMaps.get(dayOfTheYear);
            final CurrentPattern currentPattern = currentPatternAtStep.apply(step);
            if (mapsAtStep.containsKey(currentPattern)) {
                return Optional.ofNullable(mapsAtStep.get(currentPattern).get(location));
            }
        }
        return getInterpolatedVector(location, step);
    }

    private Optional<VectorMapAtStep> lookupVectorMap(
        final int step,
        final Function<? super Integer, Integer> keyLookup,
        final Supplier<Integer> keyFallback,
        final BiFunction<? super Integer, ? super Integer, Integer> offsetFunction,
        final int stepDirection // +1 or -1
    ) {
        final int oldDay = getDayOfTheYear(step);
        return Optional
            .ofNullable(keyLookup.apply(oldDay))
            .map(Optional::of) // wrap it because `orElse` unwraps; wouldn't need that with `Optional::or` in Java 9+
            .orElseGet(() -> Optional.ofNullable(keyFallback.get()))
            .flatMap(newDay -> {
                final int offsetInDays = offsetFunction.apply(oldDay, newDay);
                final int newStep = step + (offsetInDays * stepsPerDay);
                final Map<CurrentPattern, Map<Int2D, Double2D>> mapsOnNewDay = vectorMaps.get(newDay);
                final CurrentPattern patternAtNewStep = currentPatternAtStep.apply(newStep);
                return Optional
                    .ofNullable(mapsOnNewDay.get(patternAtNewStep))
                    .map(vectorMap -> Optional.of(new VectorMapAtStep(newStep, vectorMap)))
                    .orElseGet(() -> lookupVectorMap(
                        newStep + stepDirection,
                        keyLookup,
                        keyFallback,
                        offsetFunction,
                        stepDirection
                    ));
            });
    }

    private Optional<VectorMapAtStep> getVectorMapBefore(final int step) {
        return lookupVectorMap(
            step,
            vectorMaps::floorKey,
            () -> Optional.ofNullable(vectorMaps.lastEntry()).map(Entry::getKey).orElse(null),
            CurrentVectorsEPO::negativeDaysOffset,
            -1
        );
    }

    private Optional<VectorMapAtStep> getVectorMapAfter(final int step) {
        return lookupVectorMap(
            step,
            vectorMaps::ceilingKey,
            () -> Optional.ofNullable(vectorMaps.firstEntry()).map(Entry::getKey).orElse(null),
            CurrentVectorsEPO::positiveDaysOffset,
            +1
        );
    }

    /**
     * Return the interpolated vector between the currents we have before and after step.
     * Returns Optional.empty() if we don't have currents for the desired sea tile.
     */
    private Optional<Double2D> getInterpolatedVector(final Int2D location, final int step) {
        // I'm sorry, I know how dreadful that looks in Java but this really
        // is just simple monad chaining and I don't know how else
        // to do it without checking for null every other line. -- Nicolas
        return getVectorMapBefore(step - 1).flatMap(vectorMapBefore ->
            Optional.ofNullable(vectorMapBefore.vectorMap.get(location)).flatMap(vectorBefore ->
                getVectorMapAfter(step + 1).flatMap(vectorMapAfter ->
                    Optional.ofNullable(vectorMapAfter.vectorMap.get(location)).map(vectorAfter ->
                        getInterpolatedVector(
                            vectorBefore,
                            abs(step - vectorMapBefore.step),
                            vectorAfter,
                            abs(step - vectorMapAfter.step)
                        )
                    )
                )
            )
        );
    }

    static class VectorMapAtStep {

        final int step;
        final Map<Int2D, Double2D> vectorMap;

        VectorMapAtStep(final int step, final Map<Int2D, Double2D> vectorMap) {
            this.step = step;
            this.vectorMap = vectorMap;
        }

    }

}

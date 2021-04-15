package uk.ac.ox.oxfish.geography.currents;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import sim.util.Double2D;
import sim.util.Int2D;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;

public class CurrentVectors {

    // Even caching just 50 vectors per time step gives a hit rate of about 80%!
    private static final int MAX_CACHE_ENTRIES = 50;
    private final CacheBuilder<Object, Object> cacheBuilder =
        CacheBuilder.newBuilder().maximumSize(MAX_CACHE_ENTRIES).recordStats();

    private final Map<Integer, Cache<Int2D, Optional<Double2D>>> vectorCache = new ConcurrentHashMap<>();
    private final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps;
    private final Function<Integer, CurrentPattern> currentPatternAtStep;
    private final int gridHeight;
    private final int gridWidth;
    private final int stepsPerDay;

    public CurrentVectors(
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps,
        final int stepsPerDay,
        final int gridWidth,
        final int gridHeight
    ) {
        this(vectorMaps, __ -> Y2017, gridWidth, gridHeight, stepsPerDay);
    }

    public CurrentVectors(
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps,
        final Function<Integer, CurrentPattern> currentPatternAtStep,
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

    public Map<Integer, Cache<Int2D, Optional<Double2D>>> getVectorCache() {
        return Collections.unmodifiableMap(vectorCache);
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    private int getDayOfTheYear(final int timeStep) { return ((timeStep / stepsPerDay) % 365) + 1; }

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

    public Optional<Double2D> getVector(final int step, final Int2D location) {
        try {
            return vectorCache
                .computeIfAbsent(step, __ -> cacheBuilder.build())
                .get(location, () -> computeVector(step, location));
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
        return Optional.ofNullable(getInterpolatedVector(location, step));
    }

    private VectorMapAtStep lookupVectorMap(
        final int step,
        final Function<Integer, Integer> keyLookup,
        final Supplier<Integer> keyFallback,
        final BiFunction<Integer, Integer, Integer> offsetFunction,
        final int stepDirection // +1 or -1
    ) {
        final int oldDay = getDayOfTheYear(step);
        final Integer newKey = keyLookup.apply(oldDay);
        final int newDay = newKey != null ? newKey : keyFallback.get();
        final int offsetInDays = offsetFunction.apply(oldDay, newDay);
        final int newStep = step + (offsetInDays * stepsPerDay);
        final Map<CurrentPattern, Map<Int2D, Double2D>> mapsOnNewDay = vectorMaps.get(newDay);
        final CurrentPattern patternAtNewStep = currentPatternAtStep.apply(newStep);
        return mapsOnNewDay.containsKey(patternAtNewStep) ?
            new VectorMapAtStep(newStep, mapsOnNewDay.get(patternAtNewStep)) :
            lookupVectorMap(newStep + stepDirection, keyLookup, keyFallback, offsetFunction, stepDirection);
    }

    private VectorMapAtStep getVectorMapBefore(final int step) {
        return lookupVectorMap(
            step,
            vectorMaps::floorKey,
            vectorMaps::lastKey,
            CurrentVectors::negativeDaysOffset,
            -1
        );
    }

    private VectorMapAtStep getVectorMapAfter(final int step) {
        return lookupVectorMap(
            step,
            vectorMaps::ceilingKey,
            vectorMaps::firstKey,
            CurrentVectors::positiveDaysOffset,
            +1
        );
    }

    /**
     * Return the interpolated vector between the currents we have before and after step.
     * Returns null if we don't have currents for the desired sea tile.
     */
    private Double2D getInterpolatedVector(final Int2D location, final int step) {
        final VectorMapAtStep vectorMapBefore = getVectorMapBefore(step - 1);
        final Double2D vectorBefore = vectorMapBefore.vectorMap.get(location);
        if (vectorBefore == null) return null;
        final int offsetBefore = abs(step - vectorMapBefore.step);
        final VectorMapAtStep vectorMapAfter = getVectorMapAfter(step + 1);
        final Double2D vectorAfter = vectorMapAfter.vectorMap.get(location);
        if (vectorAfter == null) return null;
        final int offsetAfter = abs(step - vectorMapAfter.step);
        return getInterpolatedVector(vectorBefore, offsetBefore, vectorAfter, offsetAfter);
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

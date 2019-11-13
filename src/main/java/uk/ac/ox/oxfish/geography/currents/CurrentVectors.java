package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;

public class CurrentVectors {

    private final TreeMap<Integer, Map<SeaTile, Double2D>> vectorCache = new TreeMap<>();
    private final TreeMap<Integer, EnumMap<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps;
    private final Function<Integer, CurrentPattern> currentPatternAtStep;

    private final int stepsPerDay;

    public CurrentVectors(
        TreeMap<Integer, EnumMap<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps,
        int stepsPerDay
    ) {
        this(vectorMaps, __ -> NEUTRAL, stepsPerDay);
    }

    public CurrentVectors(
        TreeMap<Integer, EnumMap<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps,
        Function<Integer, CurrentPattern> currentPatternAtStep,
        int stepsPerDay
    ) {
        this.vectorMaps = vectorMaps;
        this.currentPatternAtStep = currentPatternAtStep;
        this.stepsPerDay = stepsPerDay;
    }

    private int getDayOfTheYear(int timeStep) { return ((timeStep / stepsPerDay) % 365) + 1; }

    int positiveDaysOffset(int sourceDay, int targetDay) {
        checkArgument(sourceDay >= 1 && sourceDay <= 365);
        checkArgument(targetDay >= 1 && targetDay <= 365);
        return sourceDay <= targetDay ?
            targetDay - sourceDay :
            (365 - sourceDay) + targetDay;
    }

    int negativeDaysOffset(int sourceDay, int targetDay) {
        return -positiveDaysOffset(targetDay, sourceDay);
    }

    Double2D getVector(int step, SeaTile seaTile) {
        return vectorCache
            .computeIfAbsent(step, __ -> new HashMap<>())
            .computeIfAbsent(seaTile, __ -> computeVector(step, seaTile));
    }

    /**
     * Returns the current vector for seaTile at step. Returns null if we have no currents for that sea tile.
     */
    private Double2D computeVector(int step, SeaTile seaTile) {
        final int dayOfTheYear = getDayOfTheYear(step);
        if (vectorMaps.containsKey(dayOfTheYear)) {
            final Map<CurrentPattern, Map<SeaTile, Double2D>> mapsAtStep = vectorMaps.get(dayOfTheYear);
            final CurrentPattern currentPattern = currentPatternAtStep.apply(step);
            if (mapsAtStep.containsKey(currentPattern)) {
                return mapsAtStep.get(currentPattern).get(seaTile);
            }
        }
        return getInterpolatedVector(seaTile, step);
    }

    private VectorMapAtStep lookupVectorMap(
        int step,
        Function<Integer, Integer> keyLookup,
        Supplier<Integer> keyFallback,
        BiFunction<Integer, Integer, Integer> offsetFunction,
        int stepDirection // +1 or -1
    ) {
        final int oldDay = getDayOfTheYear(step);
        final Integer newKey = keyLookup.apply(oldDay);
        final int newDay = newKey != null ? newKey : keyFallback.get();
        final int offsetInDays = offsetFunction.apply(oldDay, newDay);
        final int newStep = step + (offsetInDays * stepsPerDay);
        final Map<CurrentPattern, Map<SeaTile, Double2D>> mapsOnNewDay = vectorMaps.get(newDay);
        final CurrentPattern patternAtNewStep = currentPatternAtStep.apply(newStep);
        return mapsOnNewDay.containsKey(patternAtNewStep) ?
            new VectorMapAtStep(newStep, mapsOnNewDay.get(patternAtNewStep)) :
            lookupVectorMap(newStep + stepDirection, keyLookup, keyFallback, offsetFunction, stepDirection);
    }

    private VectorMapAtStep getVectorMapBefore(int step) {
        return lookupVectorMap(
            step,
            vectorMaps::floorKey,
            vectorMaps::lastKey,
            this::negativeDaysOffset,
            -1
        );
    }

    private VectorMapAtStep getVectorMapAfter(int step) {
        return lookupVectorMap(
            step,
            vectorMaps::ceilingKey,
            vectorMaps::firstKey,
            this::positiveDaysOffset,
            +1
        );
    }

    /**
     * Return the interpolated vector between the currents we have before and after step.
     * Returns null if we don't have currents for the desired sea tile.
     */
    private Double2D getInterpolatedVector(SeaTile seaTile, int step) {

        final VectorMapAtStep vectorMapBefore = getVectorMapBefore(step - 1);
        final Double2D vectorBefore = vectorMapBefore.vectorMap.get(seaTile);
        if (vectorBefore == null) return null;
        final int offsetBefore = abs(step - vectorMapBefore.step);

        final VectorMapAtStep vectorMapAfter = getVectorMapAfter(step + 1);
        final Double2D vectorAfter = vectorMapAfter.vectorMap.get(seaTile);
        if (vectorAfter == null) return null;
        final int offsetAfter = abs(step - vectorMapAfter.step);

        final double totalOffset = (double) offsetBefore + offsetAfter;
        final Double2D v1 = vectorBefore.multiply(offsetBefore / totalOffset);
        final Double2D v2 = vectorAfter.multiply(offsetAfter / totalOffset);
        return v1.add(v2);
    }

    public void removeCachedVectors(int timeStep) { vectorCache.remove(timeStep); }

    private static class VectorMapAtStep {
        final int step;
        final Map<SeaTile, Double2D> vectorMap;

        VectorMapAtStep(int step, Map<SeaTile, Double2D> vectorMap) {
            this.step = step;
            this.vectorMap = vectorMap;
        }
    }
}

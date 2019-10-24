package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;

public class CurrentVectors {

    private final TreeMap<Integer, Map<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps;
    private final Function<Integer, CurrentPattern> currentPatternAtStep;

    public CurrentVectors(
        TreeMap<Integer, Map<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps,
        Function<Integer, CurrentPattern> currentPatternAtStep
    ) {
        this.vectorMaps = vectorMaps;
        this.currentPatternAtStep = currentPatternAtStep;
    }

    public CurrentVectors(TreeMap<Integer, Map<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps) {
        this(vectorMaps, __ -> NEUTRAL);
    }

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

    public Double2D getVector(FishState fishState, SeaTile seaTile, int step) {
        final int dayOfTheYear = fishState.getDayOfTheYear(step);
        if (vectorMaps.containsKey(dayOfTheYear)) {
            final Map<CurrentPattern, Map<SeaTile, Double2D>> mapsAtStep = vectorMaps.get(dayOfTheYear);
            final CurrentPattern currentPattern = currentPatternAtStep.apply(step);
            if (mapsAtStep.containsKey(currentPattern)) {
                return mapsAtStep.get(currentPattern).get(seaTile);
            }
        }
        return getInterpolatedVector(fishState, seaTile, step);
    }

    private VectorMapAtStep lookupVectorMap(
        FishState fishState,
        int step,
        Function<Integer, Integer> keyLookup,
        Supplier<Integer> keyFallback,
        BiFunction<Integer, Integer, Integer> offsetFunction,
        int stepDirection // +1 or -1
    ) {
        final int oldDay = fishState.getDayOfTheYear(step);
        final Integer newKey = keyLookup.apply(oldDay);
        final int newDay = newKey != null ? newKey : keyFallback.get();
        final int offsetInDays = offsetFunction.apply(oldDay, newDay);
        final int newStep = step + (offsetInDays * fishState.getStepsPerDay());
        final Map<CurrentPattern, Map<SeaTile, Double2D>> mapsOnNewDay = vectorMaps.get(newDay);
        final CurrentPattern patternAtNewStep = currentPatternAtStep.apply(newStep);
        return mapsOnNewDay.containsKey(patternAtNewStep) ?
            new VectorMapAtStep(newStep, mapsOnNewDay.get(patternAtNewStep)) :
            lookupVectorMap(fishState, newStep + stepDirection, keyLookup, keyFallback, offsetFunction, stepDirection);
    }

    private VectorMapAtStep getVectorMapBefore(FishState fishState, int step) {
        return lookupVectorMap(
            fishState,
            step,
            vectorMaps::floorKey,
            vectorMaps::lastKey,
            this::negativeDaysOffset,
            -1
        );
    }

    private VectorMapAtStep getVectorMapAfter(FishState fishState, int step) {
        return lookupVectorMap(
            fishState,
            step,
            vectorMaps::ceilingKey,
            vectorMaps::firstKey,
            this::positiveDaysOffset,
            +1
        );
    }

    private Double2D getInterpolatedVector(FishState fishState, SeaTile seaTile, int step) {

        final VectorMapAtStep vectorMapBefore = getVectorMapBefore(fishState, step - 1);
        final Double2D vectorBefore = vectorMapBefore.vectorMap.get(seaTile);
        final int offsetBefore = abs(step - vectorMapBefore.step);

        final VectorMapAtStep vectorMapAfter = getVectorMapAfter(fishState, step + 1);
        final Double2D vectorAfter = vectorMapAfter.vectorMap.get(seaTile);
        final int offsetAfter = abs(step - vectorMapAfter.step);

        final double totalOffset = (double) offsetBefore + offsetAfter;
        final Double2D v1 = vectorBefore.multiply(offsetBefore / totalOffset);
        final Double2D v2 = vectorAfter.multiply(offsetAfter / totalOffset);
        return v1.add(v2);
    }

    private static class VectorMapAtStep {
        final int step;
        final Map<SeaTile, Double2D> vectorMap;
        VectorMapAtStep(int step, Map<SeaTile, Double2D> vectorMap) {
            this.step = step;
            this.vectorMap = vectorMap;
        }
    }
}

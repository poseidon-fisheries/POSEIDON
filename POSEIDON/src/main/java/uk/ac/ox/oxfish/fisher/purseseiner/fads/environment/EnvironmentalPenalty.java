package uk.ac.ox.oxfish.fisher.purseseiner.fads.environment;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.poseidon.common.core.temporal.TemporalMap;
import uk.ac.ox.poseidon.geography.DoubleGrid;

import java.time.LocalDate;
import java.util.function.ToDoubleBiFunction;

import static java.lang.Math.*;

public class EnvironmentalPenalty implements ToDoubleBiFunction<LocalDate, SeaTile> {
    private final TemporalMap<? extends DoubleGrid> grids;
    private final double target;
    private final double margin;
    private final double penalty;

    public EnvironmentalPenalty(
        final TemporalMap<? extends DoubleGrid> grids,
        final double target,
        final double margin,
        final double penalty
    ) {
        this.grids = grids;
        this.target = target;
        this.margin = margin;
        this.penalty = penalty;
    }

    @Override
    public double applyAsDouble(
        final LocalDate localDate,
        final SeaTile seaTile
    ) {
        final double valueHere = grids.get(localDate).get(seaTile.getGridX(), seaTile.getGridY());
        final double valueDifference = abs(valueHere - target) - margin;
        return (valueDifference > 0) ? 1 / pow(1 + (-valueDifference * log(1 - penalty)), 4) : 1;
    }
}

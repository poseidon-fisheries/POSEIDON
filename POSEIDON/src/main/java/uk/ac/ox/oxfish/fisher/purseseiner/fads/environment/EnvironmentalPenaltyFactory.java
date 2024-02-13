package uk.ac.ox.oxfish.fisher.purseseiner.fads.environment;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.temporal.TemporalMap;
import uk.ac.ox.poseidon.geography.DoubleGrid;

public class EnvironmentalPenaltyFactory implements ComponentFactory<EnvironmentalPenalty> {
    private ComponentFactory<? extends TemporalMap<DoubleGrid>> grids;
    private DoubleParameter target;
    private DoubleParameter margin;
    private DoubleParameter penalty;

    @SuppressWarnings("unused")
    public EnvironmentalPenaltyFactory() {
    }

    public EnvironmentalPenaltyFactory(
        final ComponentFactory<? extends TemporalMap<DoubleGrid>> grids,
        final DoubleParameter target,
        final DoubleParameter margin,
        final DoubleParameter penalty
    ) {
        this.grids = grids;
        this.target = target;
        this.margin = margin;
        this.penalty = penalty;
    }

    public ComponentFactory<? extends TemporalMap<DoubleGrid>> getGrids() {
        return grids;
    }

    public void setGrids(final ComponentFactory<? extends TemporalMap<DoubleGrid>> grids) {
        this.grids = grids;
    }

    public DoubleParameter getTarget() {
        return target;
    }

    public void setTarget(final DoubleParameter target) {
        this.target = target;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getMargin() {
        return margin;
    }

    @SuppressWarnings("unused")
    public void setMargin(final DoubleParameter margin) {
        this.margin = margin;
    }

    public DoubleParameter getPenalty() {
        return penalty;
    }

    public void setPenalty(final DoubleParameter penalty) {
        this.penalty = penalty;
    }

    @Override
    public EnvironmentalPenalty apply(final ModelState modelState) {
        final MersenneTwisterFast rng = modelState.getRandom();
        return new EnvironmentalPenalty(
            grids.apply(modelState),
            target.applyAsDouble(rng),
            margin.applyAsDouble(rng),
            penalty.applyAsDouble(rng)
        );
    }
}

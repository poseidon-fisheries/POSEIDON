/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

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

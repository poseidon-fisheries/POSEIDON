/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization.generic;

import static java.lang.Math.abs;

/**
 * Implements the SMAPE (Symmetric mean absolute percentage error) measure.
 * <p>
 * This is the version of the measure where the denominator is not divided by two, thus producing a result between 0 and
 * 1. We also don't multiply it by 100 in order to make it usable with other measures in the unit interval.
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/Symmetric_mean_absolute_percentage_error">https://en.wikipedia
 * .org/wiki/Symmetric_mean_absolute_percentage_error</a>
 */
public class SymmetricMeanAbsolutePercentage implements ErrorMeasure {
    @Override
    public double applyAsDouble(
        final double target,
        final double result
    ) {
        return (target == result)
            ? 0 // avoids NaN when both actual and predicted are 0
            : abs(target - result) / (abs(result) + abs(target));
    }
}

/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.calibration.errors;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * A calibration error metric that sums the squared differences between a fixed map of target
 * values and a map of actual values extracted from a {@link Simulation}, keyed by an arbitrary
 * type {@code K}. A key present in only one of the two maps is treated as having a value of
 * {@code 0.0} on the other side, so it still contributes to the error.
 *
 * @param <K> the type of key the target and actual value maps are indexed by
 */
@RequiredArgsConstructor
public class SumSquaredErrors<K> implements ToDoubleFunction<Simulation> {

    private final Map<K, Double> targetValues;
    private final Function<Simulation, Map<K, Double>> actualValues;

    /**
     * Extracts the actual values from {@code simulation} and returns the sum of squared
     * differences against {@link #targetValues}, over the union of both maps' keys.
     */
    @Override
    public double applyAsDouble(final Simulation simulation) {
        final Map<K, Double> actualValues = this.actualValues.apply(simulation);
        return Sets
            .union(targetValues.keySet(), actualValues.keySet())
            .stream()
            .mapToDouble(key -> {
                final var targetValue = targetValues.getOrDefault(key, 0.0);
                final var actualValue = actualValues.getOrDefault(key, 0.0);
                final double error = targetValue - actualValue;
                return error * error;
            })
            .sum();
    }
}

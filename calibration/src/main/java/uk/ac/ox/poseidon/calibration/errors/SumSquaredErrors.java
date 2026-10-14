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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import uk.ac.ox.poseidon.core.Simulation;

/**
 * A calibration error metric that sums the squared differences between a fixed
 * map of target values and a map of actual values extracted from a
 * {@link Simulation}, keyed by an arbitrary type {@code K}. A key present in
 * only one of the two maps is treated as having a value of {@code 0.0} on the
 * other side, so it still contributes to the error. Every target must be a
 * finite number: a single {@code NaN} target would make every error
 * {@code NaN}, and the calibration would have nothing to compare.
 *
 * @param <K> the type of key the target and actual value maps are indexed by
 */
public class SumSquaredErrors<K> implements ToDoubleFunction<Simulation> {

  private final Map<K, Double> targetValues;
  private final Function<Simulation, Map<K, Double>> actualValues;

  /**
   * @throws IllegalArgumentException if a target value is {@code null},
   *                                  {@code NaN} or infinite
   */
  public SumSquaredErrors(
    final Map<K, Double> targetValues,
    final Function<Simulation, Map<K, Double>> actualValues
  ) {
    final List<K> invalidKeys = targetValues
      .entrySet()
      .stream()
      .filter(
        entry -> entry.getValue() == null || !Double.isFinite(entry.getValue())
      )
      .map(Map.Entry::getKey)
      .toList();
    checkArgument(
      invalidKeys.isEmpty(),
      "Target values must be finite numbers, but %s of %s are not: %s",
      invalidKeys.size(),
      targetValues.size(),
      invalidKeys
    );
    this.targetValues = Map.copyOf(targetValues);
    this.actualValues = actualValues;
  }

  /**
   * Extracts the actual values from {@code simulation} and returns the sum of
   * squared differences against {@link #targetValues}, over the union of both
   * maps' keys.
   */
  @Override
  public double applyAsDouble(final Simulation simulation) {
    final Map<K, Double> actualValues = this.actualValues.apply(simulation);
    return Sets.union(targetValues.keySet(), actualValues.keySet())
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

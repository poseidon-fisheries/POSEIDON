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

package uk.ac.ox.poseidon.calibration;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.Value;

/**
 * A positive, finite, inclusive range with equal ratios at equal search steps.
 */
@Value
public class LogarithmicParameterRange implements ParameterRange {

  /** Inclusive lower bound. */
  double min;
  /** Inclusive upper bound. */
  double max;

  /**
   * @param min inclusive lower bound
   * @param max inclusive upper bound
   * @throws IllegalArgumentException if the bounds are invalid
   */
  public LogarithmicParameterRange(final double min, final double max) {
    checkArgument(
      Double.isFinite(min) && Double.isFinite(max),
      "Bounds must be finite: %s, %s",
      min,
      max
    );
    checkArgument(min > 0, "Logarithmic bounds must be positive: %s", min);
    checkArgument(
      min <= max,
      "Lower bound must not exceed upper bound: %s, %s",
      min,
      max
    );
    this.min = min;
    this.max = max;
  }

  @Override
  public Double decode(final double allele) {
    checkArgument(
      allele >= 0 && allele <= 1,
      "Search coordinate must be in [0, 1]: %s",
      allele
    );
    final double value;
    if (allele == 0 || min == max) {
      value = min;
    } else if (allele == 1) {
      value = max;
    } else {
      value = Math.clamp(
        Math.exp(Math.log(min) + allele * (Math.log(max) - Math.log(min))),
        min,
        max
      );
    }
    return value;
  }
}

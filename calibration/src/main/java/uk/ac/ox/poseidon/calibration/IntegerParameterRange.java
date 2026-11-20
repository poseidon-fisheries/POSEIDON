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
 * An inclusive integer range with one equal-width search bin per integer.
 */
@Value
public class IntegerParameterRange implements ParameterRange {

  /** Inclusive lower bound. */
  int min;
  /** Inclusive upper bound. */
  int max;

  /**
   * @param min inclusive lower bound
   * @param max inclusive upper bound
   * @throws IllegalArgumentException if the bounds are invalid
   */
  public IntegerParameterRange(final int min, final int max) {
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
  public Integer decode(final double allele) {
    checkArgument(
      allele >= 0 && allele <= 1,
      "Search coordinate must be in [0, 1]: %s",
      allele
    );
    // This is rounding over [min - 0.5, max + 0.5), expressed without
    // adding and cancelling the half-unit at floating-point bin boundaries.
    // Use long arithmetic so the full int domain is a valid range.
    return (int) Math.min(
      max,
      (long) min + (long) Math.floor(allele * ((long) max - min + 1))
    );
  }
}

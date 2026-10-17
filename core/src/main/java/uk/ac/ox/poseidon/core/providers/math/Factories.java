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

package uk.ac.ox.poseidon.core.providers.math;

import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for values computed from two other values via a basic math
 * operation (currently max and min). Each keeps {@link Math}'s behaviour, NaN
 * included, so the {@code Double} and {@code Integer} variants stay apart.
 */
public class Factories {

  private Factories() {}

  /**
   * @param a one of the two values to compare
   * @param b the other value to compare
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link MaxDouble}
   * @see MaxDouble
   */
  public static <S extends Scope, C> MaxDoubleFactory<S, C> maxDouble(
    final ValueFactory<? super S, ? super C, Double> a,
    final ValueFactory<? super S, ? super C, Double> b
  ) {
    return new MaxDoubleFactory<>(a, b);
  }

  /**
   * @param a one of the two values to compare
   * @param b the other value to compare
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link MaxInt}
   * @see MaxInt
   */
  public static <S extends Scope, C> MaxIntFactory<S, C> maxInt(
    final ValueFactory<? super S, ? super C, Integer> a,
    final ValueFactory<? super S, ? super C, Integer> b
  ) {
    return new MaxIntFactory<>(a, b);
  }

  /**
   * @param a one of the two values to compare
   * @param b the other value to compare
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link MinDouble}
   * @see MinDouble
   */
  public static <S extends Scope, C> MinDoubleFactory<S, C> minDouble(
    final ValueFactory<? super S, ? super C, Double> a,
    final ValueFactory<? super S, ? super C, Double> b
  ) {
    return new MinDoubleFactory<>(a, b);
  }

  /**
   * @param a one of the two values to compare
   * @param b the other value to compare
   * @return a {@link uk.ac.ox.poseidon.core.ValueFactory} for a
   *         {@link MinInt}
   * @see MinInt
   */
  public static <S extends Scope, C> MinIntFactory<S, C> minInt(
    final ValueFactory<? super S, ? super C, Integer> a,
    final ValueFactory<? super S, ? super C, Integer> b
  ) {
    return new MinIntFactory<>(a, b);
  }
}

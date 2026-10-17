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

package uk.ac.ox.poseidon.core.functions;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Turns a live value or mapping whose context is {@link Void} into the plain
 * {@link Supplier}, {@link Predicate} or {@link Function} held by a component
 * that has no context to give: a grid built cell by cell, a regulation judging
 * an action, a gear fishing. The component's factory takes the value with
 * {@code Void} as its context, so a value that reads a vessel is refused when
 * the scenario is compiled, and converts it once, when it builds. Each call
 * passes {@code null}, the only value of {@code Void}.
 */
public final class WithoutContext {

  private WithoutContext() {}

  /**
   * @param value a value whose context is {@code Void}
   * @return a supplier of that value
   */
  public static <R> Supplier<R> supplier(
    final Function<? super Void, ? extends R> value
  ) {
    return () -> value.apply(null);
  }

  /**
   * @param mapping a yes/no mapping whose context is {@code Void}
   * @return a predicate testing what the mapping tests
   */
  public static <T> Predicate<T> predicate(
    final BiFunction<? super Void, ? super T, Boolean> mapping
  ) {
    return t -> mapping.apply(null, t);
  }

  /**
   * @param mapping a mapping whose context is {@code Void}
   * @return a function mapping what the mapping maps
   */
  public static <T, R> Function<T, R> function(
    final BiFunction<? super Void, ? super T, ? extends R> mapping
  ) {
    return t -> mapping.apply(null, t);
  }
}

/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.core.utils;

import com.google.common.collect.ImmutableList;
import java.util.*;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RunIdFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;

/**
 * General-purpose factories that don't belong to a more specific domain
 * package: literal and collection pass-throughs (e.g. {@link ObjectFactory}),
 * simple combinations of other resolved components (e.g. {@link PairFactory}),
 * scope-changing wrappers, small standalone utilities like a unique-id
 * supplier, and calibration support for cloning a factory with varied property
 * values.
 */
@SuppressWarnings("varargs")
public final class Factories {

  private Factories() {}

  /**
   * @param value the value to always resolve to
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always
   *         resolves to {@code value}
   * @see ObjectFactory
   */
  public static <T> ObjectFactory<T> object(final T value) {
    return new ObjectFactory<>(value);
  }

  /**
   * @param values the component factories to resolve, in order
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link List} of the resolved values
   * @see ListFactory
   */
  @SafeVarargs
  public static <S extends Scope, P> ListFactory<S, P> listOf(
    final Factory<S, ? extends P>... values
  ) {
    return new ListFactory<>(ImmutableList.copyOf(values));
  }

  /**
   * @param values the component factories to resolve, in order
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link List} of the resolved values
   * @see ListFactory
   */
  public static <S extends Scope, P> ListFactory<S, P> listOf(
    final List<? extends Factory<S, ? extends P>> values
  ) {
    return new ListFactory<>(ImmutableList.copyOf(values));
  }

  /**
   * @param values the literal values to wrap
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always
   *         resolves to a {@link List} of {@code values}
   * @see ObjectFactory
   */
  @SafeVarargs
  public static <T> ObjectFactory<List<T>> listOf(final T... values) {
    return new ObjectFactory<>(List.of(values));
  }

  /**
   * @param values the literal values to wrap, deduplicated and order-preserved
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} that always
   *         resolves to a {@link Set} of {@code values}
   * @see ObjectFactory
   */
  @SafeVarargs
  public static <T> ObjectFactory<Set<T>> setOf(final T... values) {
    // using LinkedHashSet to preserve insertion order and be consistent with
    // SnakeYAML
    final LinkedHashSet<T> set = new LinkedHashSet<>(Arrays.asList(values));
    return new ObjectFactory<>(Collections.unmodifiableSet(set));
  }

  /**
   * @param first  factory for the first value
   * @param second factory for the second value
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link Pair} of the resolved values
   * @see Pair
   */
  public static <S extends Scope, A, B> PairFactory<S, A, B> pair(
    final Factory<? super S, ? extends A> first,
    final Factory<? super S, ? extends B> second
  ) {
    return new PairFactory<>(first, second);
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for the
   *         id of the simulation run
   * @see RunIdFactory
   */
  public static RunIdFactory runId() {
    return new RunIdFactory();
  }

  /**
   * @param prefix the prefix prepended to every generated id
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link PrefixedIdSupplier} with that prefix
   * @see PrefixedIdSupplier
   */
  public static PrefixedIdSupplierFactory prefixedIdSupplier(
    final String prefix
  ) {
    return new PrefixedIdSupplierFactory(prefix);
  }

  /**
   * @param process factory for the steppable to register as a final process
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for the
   *         resolved steppable, registered to run at simulation
   *         {@code finish()}
   * @see FinalProcessFactory
   */
  public static <P extends Steppable> FinalProcessFactory<P> finalProcess(
    final Factory<? super SimulationScope, P> process
  ) {
    return new FinalProcessFactory<>(process);
  }
}

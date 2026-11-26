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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A {@link RelativeScopeFactory} counterpart of
 * {@link CompositeCatchSorting}, built via
 * {@link Factories#compositeCatchSorting}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CompositeCatchSortingFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  CompositeCatchSorting
> {

  @Singular
  private final ImmutableList<Factory<? super S, ? extends CatchSorting>> steps;

  /** @param steps the steps to chain, in order */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public CompositeCatchSortingFactory(
    final Factory<? super S, ? extends CatchSorting>... steps
  ) {
    this(ImmutableList.copyOf(steps));
  }

  /**
   * @return a {@link CompositeCatchSorting} chaining every resolved step,
   *         in order
   */
  @Override
  protected CompositeCatchSorting newInstance(final S scope) {
    return new CompositeCatchSorting(
      steps
        .stream()
        .map(f -> f.get(scope))
        .collect(toImmutableList())
    );
  }
}

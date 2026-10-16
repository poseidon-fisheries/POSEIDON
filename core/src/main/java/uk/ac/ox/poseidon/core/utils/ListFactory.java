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
import java.util.List;
import lombok.*;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A {@link RelativeScopeFactory} for a {@link List} of resolved values from the
 * given component factories, in order. No separate plain component class here:
 * the produced value is a bare {@link List}, with no wrapper to carry
 * documentation. Built via {@link Factories Factories.listOf(...)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class ListFactory<
  S extends Scope,
  P
> extends RelativeScopeFactory<S, List<P>> {

  @Singular
  private final ImmutableList<Factory<? super S, ? extends P>> factories;

  @Override
  protected List<P> newInstance(final S scope) {
    return factories
      .stream()
      .map(f -> (P) f.get(scope))
      .toList();
  }
}

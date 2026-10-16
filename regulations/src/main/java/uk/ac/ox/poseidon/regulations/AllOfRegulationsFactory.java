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

package uk.ac.ox.poseidon.regulations;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link AllOfRegulations}, built
 * via {@link Factories#allOf(Factory...)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class AllOfRegulationsFactory<
  S extends Scope,
  A extends Action<?>
> extends RelativeScopeFactory<S, AllOfRegulations<A>> {

  private List<
    Factory<? super S, ? extends Regulations<? super A>>
  > regulations;

  @Override
  protected AllOfRegulations<A> newInstance(final S scope) {
    return new AllOfRegulations<>(
      regulations
        .stream()
        .map(regulation -> regulation.get(scope))
        .collect(toImmutableList())
    );
  }
}

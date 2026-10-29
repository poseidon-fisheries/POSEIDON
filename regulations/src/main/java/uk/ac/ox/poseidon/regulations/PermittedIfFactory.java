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

import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toPredicate;

import lombok.*;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A {@link RelativeScopeFactory} for a {@link PermittedIf}. Built via
 * {@link Factories#permittedIf}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class PermittedIfFactory<
  S extends Scope,
  A extends Action<?>
> extends RelativeScopeFactory<S, PermittedIf<A>> {

  private final MappingFactory<
    ? super S,
    ? super Void,
    ? super A,
    Boolean
  > actionPredicate;

  @Override
  protected PermittedIf<A> newInstance(final S scope) {
    return new PermittedIf<>(toPredicate(actionPredicate.get(scope)));
  }
}

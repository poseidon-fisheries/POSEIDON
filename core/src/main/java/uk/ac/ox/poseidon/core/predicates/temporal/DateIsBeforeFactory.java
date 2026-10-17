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

package uk.ac.ox.poseidon.core.predicates.temporal;

import java.time.LocalDate;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link DateIsBefore}, built via
 * {@link Factories#dateIsBefore(uk.ac.ox.poseidon.core.ValueFactory)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class DateIsBeforeFactory<S extends Scope, C>
  extends RelativeScopeFactory<S, BiFunction<C, LocalDate, Boolean>>
  implements MappingFactory<S, C, LocalDate, Boolean>
{

  private final ValueFactory<
    ? super S,
    ? super C,
    ? extends LocalDate
  > referenceDate;

  @Override
  protected BiFunction<C, LocalDate, Boolean> newInstance(final S scope) {
    return new DateIsBefore<>(referenceDate.get(scope));
  }
}

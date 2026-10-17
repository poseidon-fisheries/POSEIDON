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

package uk.ac.ox.poseidon.core.predicates;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * A yes/no value that reads a value under the context, then tests it against a
 * yes/no mapping under the same context. Built via
 * {@link Factories#condition(uk.ac.ox.poseidon.core.ValueFactory, uk.ac.ox.poseidon.core.MappingFactory)}
 * in this package.
 *
 * @param <C> the type of the context
 * @param <U> the type of the value tested
 */
@RequiredArgsConstructor
public class ConditionValue<C, U> implements Function<C, Boolean> {

  private final Function<? super C, ? extends U> extractor;
  private final BiFunction<? super C, ? super U, Boolean> predicate;

  @Override
  public Boolean apply(final C context) {
    return predicate.apply(context, extractor.apply(context));
  }
}

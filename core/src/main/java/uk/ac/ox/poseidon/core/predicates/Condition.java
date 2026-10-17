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
import lombok.RequiredArgsConstructor;

/**
 * A yes/no mapping that states a predicate of a subject: it reads the subject
 * from the thing tested, then tests it against the predicate, both under the
 * same context. Built via {@link Factories Factories.condition(...)} in this
 * package.
 *
 * @param <C> the type of the context
 * @param <T> the type of the thing tested
 * @param <U> the type of the subject, read from it and actually tested
 */
@RequiredArgsConstructor
public class Condition<C, T, U> implements BiFunction<C, T, Boolean> {

  private final BiFunction<? super C, ? super T, ? extends U> subject;
  private final BiFunction<? super C, ? super U, Boolean> predicate;

  @Override
  public Boolean apply(final C context, final T t) {
    return predicate.apply(context, subject.apply(context, t));
  }
}

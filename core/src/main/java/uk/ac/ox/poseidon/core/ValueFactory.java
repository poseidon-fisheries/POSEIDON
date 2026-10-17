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

package uk.ac.ox.poseidon.core;

import java.util.function.Function;

/**
 * A {@link Factory} of a value of a context: a {@link Function} from the
 * context its caller passes, such as the vessel a task runs for, to the value.
 * A value that does not depend on the context, such as a constant or the
 * current date, has {@code Object} as its context and fits wherever a value of
 * any context is expected.
 *
 * <p>
 * A building block whose result can be a value or a mapping (a condition, a
 * conjunction, a negation) has one overload per shape, told apart by these
 * factory types. A helper method returning a live value must therefore
 * declare its result as a {@code ValueFactory} (or a subtype): declared as a
 * plain {@link Factory}, it compiles, but every use of it fails, at the use.
 *
 * @param <S> the scope this factory resolves against
 * @param <C> the type of the context
 * @param <R> the type of the value
 */
public interface ValueFactory<
  S extends Scope,
  C,
  R
> extends Factory<S, Function<C, R>> {}

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

import java.util.function.BiFunction;

/**
 * A {@link Factory} of a mapping under a context: a {@link BiFunction} from the
 * context its caller passes, such as the vessel a task runs for, and the thing
 * mapped, such as a cell or an option, to the result. A mapping that does not
 * depend on the context, such as a comparison with a constant, has
 * {@code Object} as its context and fits wherever a mapping under any context
 * is expected.
 *
 * <p>
 * A building block whose result can be a value or a mapping (a condition, a
 * conjunction, a negation) has one overload per shape, told apart by these
 * factory types. A helper method returning a mapping must therefore declare
 * its result as a {@code MappingFactory} (or a subtype): declared as a plain
 * {@link Factory}, it compiles, but every use of it fails, at the use.
 *
 * @param <S> the scope this factory resolves against
 * @param <C> the type of the context
 * @param <T> the type of the thing mapped
 * @param <R> the type of the result
 */
public interface MappingFactory<
  S extends Scope,
  C,
  T,
  R
> extends Factory<S, BiFunction<C, T, R>> {}

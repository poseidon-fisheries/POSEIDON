/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import lombok.Getter;

/**
 * A mapping that gives a new id each time it is called: the given prefix
 * followed by a counter starting at 0, whatever the context and whatever it
 * maps. An id is always the id of something, and what the mapping maps is
 * what the caller names: the cell a port is placed on, the port vessels are
 * created at. It is the counterpart of
 * {@link uk.ac.ox.poseidon.core.providers.constant.ConstantMapping} for ids
 * that must differ.
 *
 * <p>
 * Unlike most mappings, it is not pure: every call moves the counter on, so a
 * consumer calls it exactly once per thing it names. Built via
 * {@link Factories#prefixedId(String)}.
 */
public class PrefixedId implements BiFunction<Object, Object, String> {

  @Getter
  private final String prefix;

  private final AtomicLong counter = new AtomicLong();

  /**
   * @param prefix the prefix prepended to every id, with a fresh counter
   *               starting at 0
   */
  public PrefixedId(final String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String apply(final Object context, final Object named) {
    return prefix + counter.getAndIncrement();
  }
}

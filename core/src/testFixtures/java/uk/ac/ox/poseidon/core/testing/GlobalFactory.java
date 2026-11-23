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

package uk.ac.ox.poseidon.core.testing;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A global factory that resolves to an object a test has built, such as a mock.
 * A scenario's {@code ObjectFactory} holds only literals, so a test that needs
 * a global input of any other kind uses this.
 *
 * @param <P> the type of the object
 */
@RequiredArgsConstructor
public final class GlobalFactory<P> extends GlobalScopeFactory<P> {

  private final P object;

  /**
   * @param object what the factory resolves to
   * @return a global factory for {@code object}
   */
  public static <P> GlobalFactory<P> globally(final P object) {
    return new GlobalFactory<>(object);
  }

  @Override
  protected P newInstance(final Scope scope) {
    return object;
  }
}

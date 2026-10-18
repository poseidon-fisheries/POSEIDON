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

package uk.ac.ox.poseidon.fishing.vessels;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A vessel component that one factory builds, for every vessel alike: it has
 * no choice, so it is never rebuilt. Built via {@link Factories#vesselComponent}
 * or {@link Factories#behaviour}.
 *
 * @param <T> the type of the component
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public final class FactoryComponent<T>
  extends GlobalScopeFactory<Function<Vessel, T>>
  implements VesselComponent<T>
{

  /**
   * Builds the component; its own level decides where its product lives: a
   * component-level factory gives each vessel its own, built fresh. Only the
   * vessel builds it.
   */
  @NonNull
  private final Factory<? super VesselComponentScope, ? extends T> factory;

  @Override
  protected Function<Vessel, T> newInstance(final Scope scope) {
    return this::of;
  }

  /** @return whether {@code other} is this very component */
  @Override
  public boolean equals(final Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}

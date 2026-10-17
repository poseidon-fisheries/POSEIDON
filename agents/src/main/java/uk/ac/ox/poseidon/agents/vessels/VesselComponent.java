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

package uk.ac.ox.poseidon.agents.vessels;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ScenarioNode;

/**
 * The scenario's single statement of one vessel component: what builds it.
 * Consumers hold the declaration and read the vessel's current component
 * through it, when they use it; the vessel keeps its components by
 * declaration. A declaration is not a factory, so that a consumer holding
 * one never builds it: the vessel does, when it is created. It is compared
 * by identity, as the key it is. Built via {@link Factories#vesselComponent}.
 *
 * @param <T> the type of the component
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public final class VesselComponent<T> implements ScenarioNode {

  /**
   * Builds the component; its own level decides where its product lives: a
   * component-level factory gives each vessel its own, built fresh.
   */
  @NonNull
  private final Factory<? super VesselComponentScope, ? extends T> factory;

  /**
   * @param vessel the vessel whose component to read
   * @return {@code vessel}'s component for this declaration
   * @throws IllegalStateException if {@code vessel} has no such component, or
   *                               was not created with this declaration
   */
  public T of(final Vessel vessel) {
    return findIn(vessel).orElseThrow(() ->
      new IllegalStateException(
        "Vessel " + vessel.getId() + " has no component declared by " + this
      )
    );
  }

  /**
   * @param vessel the vessel whose component to read
   * @return {@code vessel}'s component for this declaration, or nothing if it
   *         has none
   * @throws IllegalStateException if {@code vessel} was not created with this
   *                               declaration
   */
  public Optional<T> findIn(final Vessel vessel) {
    return vessel.findComponent(this);
  }
}

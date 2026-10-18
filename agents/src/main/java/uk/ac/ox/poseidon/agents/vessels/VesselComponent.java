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
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * One of a vessel's components, such as its gear: what builds it, a single
 * factory ({@link FactoryComponent}) or a choice by the vessel's attributes
 * ({@link ChosenComponent}), and the key under which each vessel keeps what
 * was built, with a store of its own, so with a lifetime of its own. The
 * vessel builds it, when it is created and again when a change of attributes
 * makes a choice pick anew. Readers get a vessel's current component when
 * they use it, through {@link #of} or {@link #findIn}, or by taking this as a
 * value of the vessel: its product is a function from a vessel to that
 * vessel's component, and building it never builds the component. It is
 * compared by identity, as the key it is.
 *
 * @param <T> the type of the component
 */
public sealed interface VesselComponent<T>
  extends ValueFactory<Scope, Vessel, T>
  permits FactoryComponent, ChosenComponent
{
  /**
   * @param vessel the vessel whose component to read
   * @return {@code vessel}'s current component
   * @throws IllegalStateException if {@code vessel} has none, or was not
   *                               created with this component
   */
  default T of(final Vessel vessel) {
    return findIn(vessel).orElseThrow(() ->
      new IllegalStateException(
        "Vessel " + vessel.getId() + " has no component " + this
      )
    );
  }

  /**
   * @param vessel the vessel whose component to read
   * @return {@code vessel}'s current component, or nothing if it has none
   * @throws IllegalStateException if {@code vessel} was not created with this
   *                               component
   */
  default Optional<T> findIn(final Vessel vessel) {
    return vessel.findComponent(this);
  }
}

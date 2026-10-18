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

import static org.mockito.Mockito.when;

import java.util.Optional;
import uk.ac.ox.poseidon.core.ProductStore;

/**
 * Declarations whose components tests give mocked vessels directly, and the
 * scopes of components built outside a vessel's creation.
 */
public final class DeclaredComponents {

  private DeclaredComponents() {}

  /**
   * @param vessel    a mocked vessel
   * @param component the component {@code vessel} has
   * @return a declaration through which {@code vessel} has {@code component}
   */
  public static <T> VesselComponent<T> declaredFor(
    final Vessel vessel,
    final T component
  ) {
    final VesselComponent<T> declaration = new FactoryComponent<>(
      scope -> component
    );
    giveComponent(vessel, declaration, component);
    return declaration;
  }

  /**
   * @param vesselScope the scope of the vessel the component belongs to
   * @return the scope of a new component of that vessel, with a store of its
   *         own
   */
  public static VesselComponentScope componentScopeOf(
    final VesselScope vesselScope
  ) {
    return componentScopeOf(vesselScope, new ProductStore());
  }

  /**
   * @param vesselScope the scope of the vessel the component belongs to
   * @param store       the store of the component
   * @return the scope of a component of that vessel, with {@code store}
   */
  public static VesselComponentScope componentScopeOf(
    final VesselScope vesselScope,
    final ProductStore store
  ) {
    return new VesselComponentScope(vesselScope, store);
  }

  /**
   * @param vessel      a mocked vessel
   * @param declaration the declaration to give the component through
   * @param component   the component {@code vessel} has
   */
  public static <T> void giveComponent(
    final Vessel vessel,
    final VesselComponent<T> declaration,
    final T component
  ) {
    when(vessel.findComponent(declaration)).thenReturn(Optional.of(component));
  }
}

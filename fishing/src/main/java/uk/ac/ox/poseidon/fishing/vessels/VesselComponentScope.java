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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.ac.ox.poseidon.core.ProductStore;

/**
 * A {@link VesselScope} narrowed to one of the vessel's components, for
 * factories building what belongs to that component: its tasks, its holds,
 * its learners. It carries the store the vessel keeps for the component, so
 * a product built under it, and the teardown of a registration made while
 * building it, go when the component does. Only the vessel's creation
 * constructs one; the store has no other accessor.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class VesselComponentScope extends VesselScope {

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final ProductStore store;

  /**
   * @param vesselScope the scope of the vessel the component belongs to
   * @param store       the store the vessel keeps for the component
   */
  VesselComponentScope(
    final VesselScope vesselScope,
    final ProductStore store
  ) {
    super(vesselScope);
    this.store = store;
  }

  /** @return the store the vessel keeps for the component */
  @Override
  protected ProductStore getProductStore() {
    return store;
  }
}

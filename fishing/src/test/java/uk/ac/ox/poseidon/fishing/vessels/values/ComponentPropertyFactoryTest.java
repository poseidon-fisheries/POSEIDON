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

package uk.ac.ox.poseidon.fishing.vessels.values;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentGiving;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.componentProperty;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;
import uk.ac.ox.poseidon.fishing.vessels.holds.InfiniteBiomassHold;

class ComponentPropertyFactoryTest {

  @Test
  void readsThePropertyOfTheComponentTheVesselHas() {
    final Vessel vessel = mock(Vessel.class);
    final Vessel otherVessel = mock(Vessel.class);
    final Hold hold = mock(Hold.class);
    final Hold otherHold = mock(Hold.class);
    when(hold.getAvailableCapacityInKg()).thenReturn(1.0);
    when(otherHold.getAvailableCapacityInKg()).thenReturn(2.0);
    final VesselComponent<Hold> component = componentGiving(vessel, hold);
    giveComponent(otherVessel, component, otherHold);

    final var property = componentProperty(
      component,
      "availableCapacityInKg"
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(property.apply(vessel)).isEqualTo(1.0);
    assertThat(property.apply(otherVessel)).isEqualTo(2.0);
  }

  @Test
  void readsComponentsOfDifferentClasses() {
    final Vessel vessel = mock(Vessel.class);
    final Vessel otherVessel = mock(Vessel.class);
    final Hold hold = mock(Hold.class);
    when(hold.getAvailableCapacityInKg()).thenReturn(1.0);
    final VesselComponent<Hold> component = componentGiving(vessel, hold);
    giveComponent(otherVessel, component, new InfiniteBiomassHold(null));

    final var property = componentProperty(
      component,
      "availableCapacityInKg"
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(property.apply(vessel)).isEqualTo(1.0);
    assertThat(property.apply(otherVessel)).isEqualTo(Double.POSITIVE_INFINITY);
  }
}

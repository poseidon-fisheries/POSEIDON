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

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselSpecification;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;

class VesselSpecificationFactoryTest {

  private final Factory<SimulationScope, Fleet> fleet = scope ->
    mock(Fleet.class);
  private final VesselComponent<?> holdComponent = vesselComponent(
    scope -> "hold"
  );
  private final VesselComponent<?> gearComponent = vesselComponent(
    scope -> "gear"
  );

  @Test
  void keepsListingOrder() {
    final var specification = vesselSpecification(
      fleet,
      behaviour(scope -> null),
      entry("z", holdComponent),
      entry("a", gearComponent)
    );
    assertThat(specification.getComponents().keySet()).containsExactly(
      "z",
      "a"
    );
  }

  @Test
  void rejectsDuplicateName() {
    assertThatThrownBy(() ->
      vesselSpecification(
        fleet,
        behaviour(scope -> null),
        entry("hold", holdComponent),
        entry("hold", gearComponent)
      )
    ).isInstanceOf(IllegalArgumentException.class);
  }
}

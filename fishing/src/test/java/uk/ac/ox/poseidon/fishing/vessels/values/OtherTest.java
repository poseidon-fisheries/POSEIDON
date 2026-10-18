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
import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.predicates.Factories.equal;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.allOf;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.other;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.vesselIsActive;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.vesselProperty;

import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

class OtherTest {

  private final Port home = mock(Port.class);
  private final Port elsewhere = mock(Port.class);

  private static Vessel vessel(final Port homePort, final boolean active) {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getHomePort()).thenReturn(homePort);
    when(vessel.isActive()).thenReturn(active);
    return vessel;
  }

  @Test
  void readsTheValueOffTheOtherVesselAndNotTheContext() {
    final Vessel vessel = vessel(home, true);
    final Vessel otherVessel = vessel(elsewhere, false);

    assertThat(
      other(vesselIsActive()).get(Scope.GLOBAL_SCOPE).apply(vessel, otherVessel)
    ).isFalse();
  }

  @Test
  void relatesTheOtherVesselToTheVesselTheCallIsFor() {
    final BiFunction<? super Vessel, ? super Vessel, Boolean> friendship =
      allOf(
        other(vesselIsActive()),
        condition(
          other(vesselProperty("homePort")),
          equal(vesselProperty("homePort"))
        )
      ).get(Scope.GLOBAL_SCOPE);
    final Vessel vessel = vessel(home, false);

    assertThat(friendship.apply(vessel, vessel(home, true))).isTrue();
    assertThat(friendship.apply(vessel, vessel(elsewhere, true))).isFalse();
    assertThat(friendship.apply(vessel, vessel(home, false))).isFalse();
  }
}

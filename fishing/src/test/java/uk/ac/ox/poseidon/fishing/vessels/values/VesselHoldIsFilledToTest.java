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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentGiving;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.giveComponent;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.vesselHoldIsFull;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;

class VesselHoldIsFilledToTest {

  private final Vessel vessel = mock(Vessel.class);
  private final Hold hold = mock(Hold.class);
  private final VesselComponent<Hold> holdComponent = componentGiving(
    vessel,
    hold
  );

  @Test
  void holdsWhenTheVesselsHoldIsFilledToTheThreshold() {
    when(hold.isFilledTo(0.9)).thenReturn(true);

    assertThat(
      new VesselHoldIsFilledTo(holdComponent, v -> 0.9).apply(vessel)
    ).isTrue();
  }

  @Test
  void doesNotHoldWhenTheVesselsHoldIsNotFilledToTheThreshold() {
    when(hold.isFilledTo(0.9)).thenReturn(false);

    assertThat(
      new VesselHoldIsFilledTo(holdComponent, v -> 0.9).apply(vessel)
    ).isFalse();
  }

  @Test
  void readsTheThresholdForTheVesselOnEveryCall() {
    final Vessel other = mock(Vessel.class);
    final Hold otherHold = mock(Hold.class);
    when(hold.isFilledTo(0.5)).thenReturn(true);
    when(otherHold.isFilledTo(1.0)).thenReturn(false);
    giveComponent(other, holdComponent, otherHold);
    final var filled = new VesselHoldIsFilledTo(holdComponent, v ->
      v == vessel ? 0.5 : 1.0
    );

    assertThat(filled.apply(vessel)).isTrue();
    assertThat(filled.apply(other)).isFalse();
  }

  @Test
  void throwsForAVesselWithoutThatHold() {
    final Vessel holdless = mock(Vessel.class);

    assertThatIllegalStateException().isThrownBy(() ->
      new VesselHoldIsFilledTo(holdComponent, v -> 1.0).apply(holdless)
    );
  }

  @Test
  void aFullHoldIsOneFilledToItsWholeCapacity() {
    when(hold.isFilledTo(1.0)).thenReturn(true);

    assertThat(
      vesselHoldIsFull(holdComponent).get(Scope.GLOBAL_SCOPE).apply(vessel)
    ).isTrue();
  }
}

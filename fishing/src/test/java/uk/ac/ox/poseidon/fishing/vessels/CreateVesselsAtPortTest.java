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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

final class CreateVesselsAtPortTest {

  private final VesselSpecification specification = mock(
    VesselSpecification.class
  );
  private final Fleet fleet = mock(Fleet.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Port port = mock(Port.class);
  private final AtomicInteger counter = new AtomicInteger();

  @BeforeEach
  void setUp() {
    when(specification.getFleet()).thenReturn(fleet);
    when(fleet.getPortGrid()).thenReturn(portGrid);
    when(port.getCode()).thenReturn("P1");
    when(portGrid.getObject("P1")).thenReturn(Optional.of(port));
  }

  private CreateVesselsAtPort creating(final int numberOfVessels) {
    return new CreateVesselsAtPort(
      specification,
      p -> p.getCode() + "-" + counter.incrementAndGet(),
      port,
      numberOfVessels
    );
  }

  @Test
  void createsTheGivenNumberOfVesselsWithNewIdsAndThePortAsHome() {
    creating(2).step(mock(SimState.class));

    verify(specification).create("P1-1", Optional.of("P1"), Map.of());
    verify(specification).create("P1-2", Optional.of("P1"), Map.of());
    verify(specification, times(2)).create(any(), any(), anyMap());
  }

  @Test
  void createsNoVesselsWhenTheNumberIsZero() {
    creating(0).step(mock(SimState.class));

    verify(specification, never()).create(any(), any(), anyMap());
  }

  @Test
  void rejectsAPortItsGridDoesNotHold() {
    when(portGrid.getObject("P1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> creating(1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("P1");
  }

  @Test
  void rejectsAPortOfAnotherGridWithTheSameCode() {
    when(portGrid.getObject("P1")).thenReturn(Optional.of(mock(Port.class)));

    assertThatThrownBy(() -> creating(1)).isInstanceOf(
      IllegalArgumentException.class
    );
  }
}

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

package uk.ac.ox.poseidon.fishing.fuel;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;

class FuelStationGridTest {

  private final Port portA = mock(Port.class);
  private final Port portB = mock(Port.class);
  private final FuelStation fuelStationA = fuelStation(portA);
  private final FuelStation fuelStationB = fuelStation(portB);

  private static FuelStation fuelStation(final Port port) {
    final FuelStation fuelStation = mock(FuelStation.class);
    when(fuelStation.getPort()).thenReturn(port);
    return fuelStation;
  }

  /** @return a grid with all {@code fuelStations} on the same cell */
  private static FuelStationGrid fuelStationGrid(
    final FuelStation... fuelStations
  ) {
    final Cell cell = new Cell(1, 2);
    final ModelGrid modelGrid = mock(ModelGrid.class);
    when(modelGrid.getGridWidth()).thenReturn(10);
    when(modelGrid.getGridHeight()).thenReturn(10);
    return new ImmutableFuelStationGrid(
      modelGrid,
      Arrays.stream(fuelStations).collect(
        toMap(identity(), fuelStation -> cell)
      )
    );
  }

  @Test
  void givesTheStationOfEachPortWhenTwoPortsShareACell() {
    final FuelStationGrid fuelStationGrid = fuelStationGrid(
      fuelStationA,
      fuelStationB
    );

    assertThat(fuelStationGrid.getFuelStation(portA)).isSameAs(fuelStationA);
    assertThat(fuelStationGrid.getFuelStation(portB)).isSameAs(fuelStationB);
  }

  @Test
  void findsTheStationOfAPortWithoutScanningEveryStation() {
    final FuelStationGrid fuelStationGrid = fuelStationGrid(
      fuelStationA,
      fuelStationB
    );
    clearInvocations(fuelStationA, fuelStationB);

    fuelStationGrid.getFuelStation(portA);

    verify(fuelStationB, never()).getPort();
  }

  @Test
  void throwsWithoutAStationForThePort() {
    final FuelStationGrid fuelStationGrid = fuelStationGrid(fuelStationB);

    assertThatIllegalStateException().isThrownBy(() ->
      fuelStationGrid.getFuelStation(portA)
    );
  }

  @Test
  void throwsWithMoreThanOneStationForThePort() {
    final FuelStationGrid fuelStationGrid = fuelStationGrid(
      fuelStationA,
      fuelStation(portA)
    );

    assertThatIllegalStateException().isThrownBy(() ->
      fuelStationGrid.getFuelStation(portA)
    );
  }
}

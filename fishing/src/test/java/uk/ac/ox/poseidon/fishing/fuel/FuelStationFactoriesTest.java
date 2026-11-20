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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

final class FuelStationFactoriesTest {

  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);
  private final EventManager eventManager = mock(EventManager.class);
  private final PortGrid portGrid = mock(PortGrid.class);
  private final Port portA = port("A", new Cell(1, 2));
  private final Port portB = port("B", new Cell(3, 4));

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  private Port port(final String code, final Cell cell) {
    final Port port = mock(Port.class);
    when(port.getCode()).thenReturn(code);
    when(portGrid.getCell(port)).thenReturn(cell);
    return port;
  }

  @Test
  void createsOneStationPerPortWithTheGivenPriceAndPumpRate() {
    when(portGrid.getPorts()).thenReturn(Stream.of(portA, portB));
    final Money price = Money.of(CurrencyUnit.EUR, 1.5);

    final List<FuelStation> fuelStations = new OneFuelStationPerPortFactory(
      _ -> portGrid,
      _ -> _ -> price,
      20.0,
      _ -> eventManager
    ).get(scope);

    assertThat(fuelStations)
      .extracting(FuelStation::getPort, FuelStation::getCode)
      .containsExactly(tuple(portA, "A"), tuple(portB, "B"));
    assertThat(fuelStations.getFirst().getPricePerLitre()).isEqualTo(price);
    assertThat(
      fuelStations.getFirst().getPumpRateInLitresPerMinute()
    ).isEqualTo(20.0);
  }

  @Test
  void placesEachStationAtItsPortsCell() {
    final ModelGrid modelGrid = mock(ModelGrid.class);
    when(modelGrid.getGridWidth()).thenReturn(10);
    when(modelGrid.getGridHeight()).thenReturn(10);
    when(portGrid.getModelGrid()).thenReturn(modelGrid);
    final FuelStation stationA = mock(FuelStation.class);
    when(stationA.getPort()).thenReturn(portA);
    final FuelStation stationB = mock(FuelStation.class);
    when(stationB.getPort()).thenReturn(portB);

    final FuelStationGrid grid = new FuelStationGridFactory<SimulationScope>(
      _ -> portGrid,
      _ -> List.of(stationA, stationB)
    ).get(scope);

    assertThat(grid.getCell(stationA)).isEqualTo(new Cell(1, 2));
    assertThat(grid.getCell(stationB)).isEqualTo(new Cell(3, 4));
    assertThat(grid.getFuelStation(portB)).isSameAs(stationB);
  }
}

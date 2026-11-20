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

package uk.ac.ox.poseidon.geography.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.bathymetry.DefaultBathymetricGrid;
import uk.ac.ox.poseidon.geography.distance.CartesianDistanceCalculator;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class PortGridFactoryTest {

  private final Port port = new Port("P", "Port P");

  @Test
  void aCoastalLandCellKeepsThePortAtItsOriginalCell() {
    final var bathymetricGrid = grid(new double[][] { { -2 }, { 0 }, { -3 } });

    final var portGrid = create(bathymetricGrid, new Coordinate(1.6, 0.5));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(1, 0));
    assertThat(portGrid.getPorts()).containsExactly(port);
    assertThat(portGrid.getBathymetricGrid()).isSameAs(bathymetricGrid);
  }

  @Test
  void waterCoordinatesSnapToTheNearestCoastalLandCellUsingTheGivenDistance() {
    final var bathymetricGrid = grid(new double[][] { { 0 }, { -2 }, { 3 } });
    final var coordinate = new Coordinate(1.2, 0.5);
    final var distanceCalculator = mock(DistanceCalculator.class);
    when(
      distanceCalculator.distanceInKm(coordinate, new Coordinate(0.5, 0.5))
    ).thenReturn(9.0);
    when(
      distanceCalculator.distanceInKm(coordinate, new Coordinate(2.5, 0.5))
    ).thenReturn(2.0);
    final var factory = new PortGridFactory<Scope>(
      ignored -> List.of(Pair.of(port, coordinate)),
      ignored -> bathymetricGrid,
      ignored -> distanceCalculator
    );

    final var portGrid = factory.get(GLOBAL_SCOPE);

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(2, 0));
  }

  @Test
  void aCloserInlandReplacementIsRejectedInFavourOfCoastalLand() {
    final var bathymetricGrid = grid(new double[][] {
      { -2 },
      { 0 },
      { 0 },
      { 0 },
      { 0 },
      { 0 },
      { -2 }
    });

    final var portGrid = create(bathymetricGrid, new Coordinate(2.9, 0.5));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(1, 0));
  }

  @Test
  void inlandNeighboursDoNotProvideASuitableReplacement() {
    final var bathymetricGrid = grid(new double[][] {
      { -2 },
      { 0 },
      { 0 },
      { 0 },
      { 0 },
      { 0 },
      { -2 }
    });

    assertThatThrownBy(() -> create(bathymetricGrid, new Coordinate(3.5, 0.5)))
      .isExactlyInstanceOf(RuntimeException.class)
      .hasMessageContaining("No suitable land cell found for port")
      .hasMessageContaining("Port P");
  }

  @Test
  void originalLandWithOnlyInactiveWaterNeighboursIsAccepted() {
    final var modelGrid = ModelGrid.create(
      3,
      1,
      new Envelope(0, 3, 0, 1)
    ).withActiveCells(List.of(new Cell(1, 0), new Cell(2, 0)));
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { -2 }, { 0 }, { 0 } }
    );

    final var portGrid = create(bathymetricGrid, new Coordinate(1.5, 0.5));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(1, 0));
  }

  @Test
  void replacementLandWithOnlyInactiveWaterNeighboursIsAccepted() {
    final var modelGrid = ModelGrid.create(
      5,
      1,
      new Envelope(0, 5, 0, 1)
    ).withActiveCells(
      List.of(new Cell(1, 0), new Cell(2, 0), new Cell(3, 0), new Cell(4, 0))
    );
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { -2 }, { 0 }, { 0 }, { 0 }, { -2 } }
    );

    final var portGrid = create(bathymetricGrid, new Coordinate(2.1, 0.5));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(1, 0));
  }

  @Test
  void inactiveLandCanBeAReplacementCandidate() {
    final var modelGrid = ModelGrid.create(
      3,
      1,
      new Envelope(0, 3, 0, 1)
    ).withActiveCells(List.of(new Cell(1, 0), new Cell(2, 0)));
    final BathymetricGrid bathymetricGrid = new DefaultBathymetricGrid(
      modelGrid,
      new double[][] { { 0 }, { -2 }, { 0 } }
    );

    final var portGrid = create(bathymetricGrid, new Coordinate(1.1, 0.5));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(0, 0));
  }

  @Test
  void aCoordinateJustOutsideTheGridCanSnapToCoastalLand() {
    final var bathymetricGrid = grid(new double[][] { { 0 }, { -2 } });

    final var portGrid = create(bathymetricGrid, new Coordinate(-0.1, 0.5));

    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(0, 0));
  }

  @Test
  void waterWithoutLandNeighboursFailsWithThePortAndCoordinate() {
    final var bathymetricGrid = grid(new double[][] { { -2 }, { -3 } });
    final var coordinate = new Coordinate(0.5, 0.5);

    assertThatThrownBy(() -> create(bathymetricGrid, coordinate))
      .isExactlyInstanceOf(RuntimeException.class)
      .hasMessage(
        "No suitable land cell found for port " + port + " near " + coordinate
      );
  }

  @Test
  void twoPortsCanShareTheSameCoastalCell() {
    final var bathymetricGrid = grid(new double[][] { { -2 }, { 0 } });
    final var otherPort = new Port("Q", "Port Q");
    final var factory = new PortGridFactory<Scope>(
      ignored ->
        List.of(
          Pair.of(port, new Coordinate(1.2, 0.5)),
          Pair.of(otherPort, new Coordinate(1.8, 0.5))
        ),
      ignored -> bathymetricGrid,
      ignored ->
        new CartesianDistanceCalculator(bathymetricGrid.getModelGrid(), 1)
    );

    final var portGrid = factory.get(GLOBAL_SCOPE);

    assertThat(portGrid.getObjectsAt(new Cell(1, 0))).containsExactlyInAnyOrder(
      port,
      otherPort
    );
    assertThat(portGrid.getCell(port)).isEqualTo(new Cell(1, 0));
    assertThat(portGrid.getCell(otherPort)).isEqualTo(new Cell(1, 0));
  }

  @Test
  void anEmptyPortListProducesAnEmptyGrid() {
    final var bathymetricGrid = grid(new double[][] { { -2 }, { 0 } });
    final var factory = new PortGridFactory<Scope>(
      ignored -> List.of(),
      ignored -> bathymetricGrid,
      ignored ->
        new CartesianDistanceCalculator(bathymetricGrid.getModelGrid(), 1)
    );

    assertThat(factory.get(GLOBAL_SCOPE).getPorts()).isEmpty();
  }

  private ImmutablePortGrid create(
    final BathymetricGrid bathymetricGrid,
    final Coordinate coordinate
  ) {
    final var factory = new PortGridFactory<Scope>(
      ignored -> List.of(Pair.of(port, coordinate)),
      ignored -> bathymetricGrid,
      ignored ->
        new CartesianDistanceCalculator(bathymetricGrid.getModelGrid(), 1)
    );
    return factory.get(GLOBAL_SCOPE);
  }

  private static BathymetricGrid grid(final double[][] elevations) {
    return new DefaultBathymetricGrid(elevations);
  }
}

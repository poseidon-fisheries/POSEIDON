/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.grids;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sim.util.Double2D;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;

class ModelGridTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    10,
    10,
    new Envelope(-5, 5, -5, 5)
  );

  @Test
  void getActiveNeighbours() {
    assertThat(
      modelGrid.getActiveNeighbours(new Cell(5, 5))
    ).containsExactlyInAnyOrder(
      new Cell(4, 4),
      new Cell(4, 5),
      new Cell(4, 6),
      new Cell(5, 4),
      new Cell(6, 6),
      new Cell(5, 6),
      new Cell(6, 4),
      new Cell(6, 5)
    );
    assertThat(
      modelGrid.getActiveNeighbours(new Cell(0, 0))
    ).containsExactlyInAnyOrder(new Cell(0, 1), new Cell(1, 0), new Cell(1, 1));
  }

  @Test
  void cellToCoordinate() {
    assertThat(modelGrid.toCoordinate(new Cell(0, 0))).isEqualTo(
      new Coordinate(-4.5, 4.5)
    );
    assertThat(modelGrid.toCoordinate(new Cell(9, 9))).isEqualTo(
      new Coordinate(4.5, -4.5)
    );
  }

  @Test
  void pointToCoordinate() {
    assertThat(modelGrid.toCoordinate(new Double2D(0, 0))).isEqualTo(
      new Coordinate(-5, 5)
    );
    assertThatIllegalArgumentException()
      .isThrownBy(() -> modelGrid.toCoordinate(new Double2D(10, 10)))
      .withMessage("Double2D[10.0,10.0] outside of grid");
  }

  @Test
  void coordinateToPoint() {
    assertThat(modelGrid.toPoint(new Coordinate(-5, 5))).isEqualTo(
      new Double2D(0, 0)
    );
    assertThat(modelGrid.toPoint(new Coordinate(5, -5))).isEqualTo(
      new Double2D(10, 10)
    );
  }

  @Test
  void coordinateToCell() {
    assertThat(modelGrid.toCell(new Coordinate(-5, 5))).isEqualTo(
      new Cell(0, 0)
    );
    assertThat(modelGrid.toCell(new Coordinate(-4.5, 4.5))).isEqualTo(
      new Cell(0, 0)
    );
    assertThat(modelGrid.toCell(new Coordinate(4.5, -4.5))).isEqualTo(
      new Cell(9, 9)
    );
    assertThat(modelGrid.toCell(new Coordinate(5, -5))).isEqualTo(
      new Cell(10, 10)
    );
  }

  @Test
  void rejectsAWestLongitudeEastOfTheEastLongitude() {
    final ModelGridFactory factory = new ModelGridFactory(1.0, 170, -170, 0, 1);
    assertThatIllegalStateException()
      .isThrownBy(() -> factory.get(Scope.GLOBAL_SCOPE))
      .withMessageContaining("antimeridian");
  }

  @Test
  void crossesTheAntimeridianWithLongitudesPast180() {
    final ModelGrid modelGrid = new ModelGridFactory(1.0, 170, 190, 0, 1).get(
      Scope.GLOBAL_SCOPE
    );
    assertThat(modelGrid.getGridWidth()).isEqualTo(20);
  }

  @Test
  void testCoordToXyAndBack() {
    final ModelGrid modelGrid = new ModelGridFactory(
      1.0,
      -171,
      -70,
      -50,
      50
    ).get(Scope.GLOBAL_SCOPE);
    final Envelope envelope = modelGrid.getEnvelope();
    final List<Coordinate> coordinates = range(
      (int) envelope.getMinX(),
      (int) envelope.getMaxX() - 1
    )
      .mapToObj(x -> x + 0.5)
      .flatMap(x ->
        range((int) envelope.getMinY(), (int) envelope.getMaxY() - 1)
          .mapToObj(y -> y + 0.5)
          .map(y -> new Coordinate(x, y))
      )
      .toList();

    coordinates.forEach(coordinate -> {
      final Double2D xy = modelGrid.toPoint(coordinate);
      assertThat(
        modelGrid.toCoordinate(new Cell((int) xy.x, (int) xy.y))
      ).isEqualTo(coordinate);
    });
  }

  @Test
  void everyOrderedPairOfCellsHasItsOwnKey() {
    final ModelGrid grid = ModelGrid.create(3, 2, new Envelope(0, 3, 0, 2));
    final List<Cell> cells = grid.getAllCells().toList();
    final Set<Long> keys = cells
      .stream()
      .flatMap(start -> cells.stream().map(end -> grid.cellPairKey(start, end)))
      .collect(toSet());
    assertThat(keys).hasSize(cells.size() * cells.size());
  }

  @Test
  void aCellOutsideTheGridHasNoPairKey() {
    final Cell inside = new Cell(0, 0);
    final Cell outside = new Cell(10, 0);
    assertThatIllegalArgumentException().isThrownBy(() ->
      modelGrid.cellPairKey(inside, outside)
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      modelGrid.cellPairKey(outside, inside)
    );
  }

  @Test
  void coordinateValidationAcceptsInteriorAndEnvelopeBoundaries() {
    assertThatCode(() -> {
      modelGrid.checkIsInGrid(new Coordinate(0, 0));
      modelGrid.checkIsInGrid(new Coordinate(-5, 5));
      modelGrid.checkIsInGrid(new Coordinate(5, -5));
    }).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @CsvSource({ "-5.1, 0", "5.1, 0", "0, -5.1", "0, 5.1" })
  void coordinateValidationRejectsCoordinatesBeyondEachEdge(
    final double lon,
    final double lat
  ) {
    final Coordinate coordinate = new Coordinate(lon, lat);

    assertThat(modelGrid.isInGrid(coordinate)).isFalse();
    assertThatIllegalArgumentException()
      .isThrownBy(() -> modelGrid.checkIsInGrid(coordinate))
      .withMessageContaining("outside of grid");
  }

  @Test
  void coordinateActivityUsesTheContainingCell() {
    final ModelGrid partiallyActiveGrid = modelGrid.withActiveCells(
      List.of(new Cell(0, 0))
    );

    assertThat(
      partiallyActiveGrid.isActive(new Coordinate(-4.75, 4.25))
    ).isTrue();
    assertThat(
      partiallyActiveGrid.isActive(new Coordinate(-3.75, 4.25))
    ).isFalse();
  }

  @Test
  void pointActivityUsesTheContainingCell() {
    final ModelGrid partiallyActiveGrid = modelGrid.withActiveCells(
      List.of(new Cell(0, 0))
    );

    assertThat(partiallyActiveGrid.isActive(new Double2D(0.25, 0.75))).isTrue();
    assertThat(
      partiallyActiveGrid.isActive(new Double2D(1.25, 0.75))
    ).isFalse();
  }

  @ParameterizedTest
  @CsvSource({ "0, 0", "2, 1" })
  void randomCellUsesBothGridDimensionsAndCanSelectInactiveCells(
    final int x,
    final int y
  ) {
    final ModelGrid partiallyActiveGrid = ModelGrid.create(
      3,
      2,
      new Envelope(0, 3, 0, 2)
    ).withActiveCells(List.of(new Cell(0, 1)));
    final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);
    when(rng.nextInt(3)).thenReturn(x);
    when(rng.nextInt(2)).thenReturn(y);

    final Cell selectedCell = partiallyActiveGrid.randomCell(rng);

    assertThat(selectedCell).isEqualTo(new Cell(x, y));
    assertThat(partiallyActiveGrid.isActive(selectedCell)).isFalse();
  }

  @Test
  void continuousPointConversionScalesBothAxesAndInvertsTheVerticalAxis() {
    final ModelGrid scaledGrid = ModelGrid.create(
      4,
      3,
      new Envelope(10, 18, -6, 3)
    );

    assertThat(scaledGrid.toCoordinate(new Double2D(1.25, 2.5))).isEqualTo(
      new Coordinate(12.5, -4.5)
    );
    assertThat(scaledGrid.toPoint(new Coordinate(12.5, -4.5))).isEqualTo(
      new Double2D(1.25, 2.5)
    );
  }
}

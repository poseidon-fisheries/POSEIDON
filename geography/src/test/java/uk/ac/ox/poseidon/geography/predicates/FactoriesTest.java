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

package uk.ac.ox.poseidon.geography.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.geography.predicates.Factories.inDepthRange;
import static uk.ac.ox.poseidon.geography.predicates.Factories.inGeometries;
import static uk.ac.ox.poseidon.geography.predicates.Factories.inRectangularArea;
import static uk.ac.ox.poseidon.geography.predicates.Factories.isActiveWaterCell;

import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.Cell;

final class FactoriesTest {

  private final BathymetricGrid bathymetricGrid = mock(BathymetricGrid.class);
  private final Cell cell = new Cell(1, 2);

  @Test
  void inDepthRangeAcceptsDepthsFromMinimumToMaximumInclusive() {
    final var predicate = inDepthRange(ignored -> bathymetricGrid, 10, 50).get(
      GLOBAL_SCOPE
    );

    assertThat(valueAt(predicate, -10)).isTrue();
    assertThat(valueAt(predicate, -30)).isTrue();
    assertThat(valueAt(predicate, -50)).isTrue();
  }

  @Test
  void inDepthRangeRejectsDepthsOutsideTheRangeAndLand() {
    final var predicate = inDepthRange(ignored -> bathymetricGrid, 10, 50).get(
      GLOBAL_SCOPE
    );

    assertThat(valueAt(predicate, -9)).isFalse();
    assertThat(valueAt(predicate, -51)).isFalse();
    assertThat(valueAt(predicate, 0)).isFalse();
    assertThat(valueAt(predicate, 30)).isFalse();
  }

  @Test
  void isActiveWaterCellFollowsTheBathymetricGrid() {
    when(bathymetricGrid.isActiveWater(cell)).thenReturn(true);
    final var predicate = isActiveWaterCell(ignored -> bathymetricGrid).get(
      GLOBAL_SCOPE
    );

    assertThat(predicate.apply(null, cell)).isTrue();
    assertThat(predicate.apply(null, new Cell(0, 0))).isFalse();
  }

  @Test
  void inRectangularAreaTestsCoordinatesAgainstTheEnvelope() {
    final var predicate = inRectangularArea(ignored ->
      new Envelope(0, 2, 10, 12)
    ).get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, new Coordinate(1, 11))).isTrue();
    assertThat(predicate.apply(null, new Coordinate(3, 11))).isFalse();
  }

  @Test
  void inGeometriesTestsCoordinatesAgainstTheGeometries() {
    final var geometryFactory = new GeometryFactory();
    final var predicate = inGeometries(ignored ->
      List.of(
        geometryFactory.toGeometry(
          new org.locationtech.jts.geom.Envelope(0, 2, 0, 2)
        )
      )
    ).get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, new Coordinate(1, 1))).isTrue();
    assertThat(predicate.apply(null, new Coordinate(5, 1))).isFalse();
  }

  private boolean valueAt(
    final BiFunction<Object, Cell, Boolean> predicate,
    final double elevation
  ) {
    when(bathymetricGrid.getValue(cell)).thenReturn(elevation);
    return predicate.apply(null, cell);
  }
}

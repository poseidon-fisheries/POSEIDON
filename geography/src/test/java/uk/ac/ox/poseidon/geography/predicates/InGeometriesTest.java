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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;

final class InGeometriesTest {

  private final GeometryFactory geometryFactory = new GeometryFactory();
  private final Geometry west = box(0, 0, 2, 2);
  private final Geometry east = box(10, 0, 12, 2);

  @Test
  void aCoordinateInsideAGeometryIsIn() {
    final var inGeometries = new InGeometries(List.of(west));

    assertThat(inGeometries.apply(null, new Coordinate(1, 1))).isTrue();
  }

  @Test
  void aCoordinateOutsideEveryGeometryIsNotIn() {
    final var inGeometries = new InGeometries(List.of(west, east));

    assertThat(inGeometries.apply(null, new Coordinate(5, 1))).isFalse();
  }

  @Test
  void aCoordinateInsideAnyOneOfSeveralGeometriesIsIn() {
    final var inGeometries = new InGeometries(List.of(west, east));

    assertThat(inGeometries.apply(null, new Coordinate(1, 1))).isTrue();
    assertThat(inGeometries.apply(null, new Coordinate(11, 1))).isTrue();
  }

  @Test
  void aCoordinateOnTheBoundaryIsNotIn() {
    final var inGeometries = new InGeometries(List.of(west));

    assertThat(inGeometries.apply(null, new Coordinate(0, 1))).isFalse();
  }

  @Test
  void noGeometriesContainNothing() {
    final var inGeometries = new InGeometries(List.of());

    assertThat(inGeometries.apply(null, new Coordinate(1, 1))).isFalse();
  }

  @Test
  void nullGeometriesAreRejected() {
    assertThatThrownBy(() -> new InGeometries(null)).isInstanceOf(
      NullPointerException.class
    );
  }

  @Test
  void theFactoryBuildsAPredicateOverItsGeometries() {
    final var factory = new InGeometriesFactory<Scope>(ignored ->
      List.of(west)
    );

    final var predicate = factory.get(GLOBAL_SCOPE);

    assertThat(predicate.apply(null, new Coordinate(1, 1))).isTrue();
    assertThat(predicate.apply(null, new Coordinate(5, 1))).isFalse();
  }

  private Geometry box(
    final double minX,
    final double minY,
    final double maxX,
    final double maxY
  ) {
    return geometryFactory.toGeometry(
      new org.locationtech.jts.geom.Envelope(minX, maxX, minY, maxY)
    );
  }
}

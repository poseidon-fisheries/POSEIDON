/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class CoordinateTest {

  @Test
  void testFromJTSWithValidInput() {
    final double x = 1.23;
    final double y = 4.56;
    final org.locationtech.jts.geom.Coordinate jtsCoordinate =
      new org.locationtech.jts.geom.Coordinate(x, y);

    final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

    assertThat(result).isNotNull();
    assertThat(result.getLon()).isEqualTo(x);
    assertThat(result.getLat()).isEqualTo(y);
  }

  @Test
  void testFromJTSWithExtremelyLargeValues() {
    final double x = Double.MAX_VALUE;
    final double y = -Double.MAX_VALUE;
    final org.locationtech.jts.geom.Coordinate jtsCoordinate =
      new org.locationtech.jts.geom.Coordinate(x, y);

    final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

    assertThat(result).isNotNull();
    assertThat(result.getLon()).isEqualTo(x);
    assertThat(result.getLat()).isEqualTo(y);
  }

  @Test
  void testFromJTSWithZeroCoordinates() {
    final double x = 0.0;
    final double y = 0.0;
    final org.locationtech.jts.geom.Coordinate jtsCoordinate =
      new org.locationtech.jts.geom.Coordinate(x, y);

    final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

    assertThat(result).isNotNull();
    assertThat(result.getLon()).isEqualTo(x);
    assertThat(result.getLat()).isEqualTo(y);
  }

  @Test
  void testFromJTSWithNegativeCoordinates() {
    final double x = -12.34;
    final double y = -56.78;
    final org.locationtech.jts.geom.Coordinate jtsCoordinate =
      new org.locationtech.jts.geom.Coordinate(x, y);

    final Coordinate result = Coordinate.fromJTS(jtsCoordinate);

    assertThat(result).isNotNull();
    assertThat(result.getLon()).isEqualTo(x);
    assertThat(result.getLat()).isEqualTo(y);
  }

  @Test
  void testFromJTSWithNullInput() {
    // noinspection DataFlowIssue
    assertThatNullPointerException().isThrownBy(() -> Coordinate.fromJTS(null));
  }
}

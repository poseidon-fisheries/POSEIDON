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

package uk.ac.ox.poseidon.geography.bathymetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.geography.bathymetry.Factories.bathymetricGridFromElevationValues;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGridFactory;

class BathymetricGridFromElevationValuesFactoryTest {

  @Test
  void elevationsValuesMustNotBeNull() {
    assertThatThrownBy(() -> factory(null)).isInstanceOf(
      NullPointerException.class
    );
  }

  @Test
  void incorrectNumberOfElevationValuesFails() {
    Stream.of(0, 3, 5)
      .map(n ->
        Stream.<Number>generate(() -> 0.0)
          .limit(n)
          .toList()
      )
      .forEach(elevationValues ->
        assertThatThrownBy(() ->
          factory(elevationValues).get(Scope.GLOBAL_SCOPE)
        ).isInstanceOf(IllegalArgumentException.class)
      );
  }

  @Test
  void correctNumberOfElevationValuesSucceeds() {
    factory(List.of(0.0, 0.0, 0.0, 0.0)).get(Scope.GLOBAL_SCOPE);
  }

  @Test
  void valuesAreMappedIntuitively() {
    final BathymetricGrid bathymetricGrid = factory(
      List.of(0.0, 1.0, 2.0, 3.0)
    ).get(Scope.GLOBAL_SCOPE);
    assertThat(bathymetricGrid.getElevation(new Cell(0, 0))).isEqualTo(0.0);
    assertThat(bathymetricGrid.getElevation(new Cell(1, 0))).isEqualTo(1.0);
    assertThat(bathymetricGrid.getElevation(new Cell(0, 1))).isEqualTo(2.0);
    assertThat(bathymetricGrid.getElevation(new Cell(1, 1))).isEqualTo(3.0);
  }

  @Test
  void valuesAreMappedOnANonSquareGrid() {
    // Three cells wide and two high
    final BathymetricGrid bathymetricGrid = bathymetricGridFromElevationValues(
      new ModelGridFactory(1, -1, 2, -1, 1),
      List.<Number>of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
    ).get(Scope.GLOBAL_SCOPE);
    assertThat(bathymetricGrid.getElevation(new Cell(0, 0))).isEqualTo(0.0);
    assertThat(bathymetricGrid.getElevation(new Cell(2, 0))).isEqualTo(2.0);
    assertThat(bathymetricGrid.getElevation(new Cell(0, 1))).isEqualTo(3.0);
    assertThat(bathymetricGrid.getElevation(new Cell(2, 1))).isEqualTo(5.0);
  }

  private static BathymetricGridFromElevationValuesFactory<Scope> factory(
    final List<Number> elevationValues
  ) {
    return bathymetricGridFromElevationValues(
      new ModelGridFactory(1, -1, 1, -1, 1),
      elevationValues
    );
  }
}

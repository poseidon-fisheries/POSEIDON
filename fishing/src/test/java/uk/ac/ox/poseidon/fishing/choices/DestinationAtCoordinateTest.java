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

package uk.ac.ox.poseidon.fishing.choices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class DestinationAtCoordinateTest {

  // Three columns and one row, one degree each, from longitude 0 to 3
  private final ModelGrid modelGrid = ModelGrid.create(
    3,
    1,
    new Envelope(0, 3, 0, 1)
  );

  private final Function<Map<String, Double>, Double> longitude = vessel ->
    vessel.get("lon");
  private final Function<Map<String, Double>, Double> latitude = vessel ->
    vessel.get("lat");

  @Test
  void eachVesselGetsTheCellOfItsOwnCoordinate() {
    final var destination = new DestinationAtCoordinate<>(
      modelGrid,
      longitude,
      latitude
    );
    assertThat(destination.apply(Map.of("lon", 0.5, "lat", 0.5))).isEqualTo(
      Optional.of(new Cell(0, 0))
    );
    assertThat(destination.apply(Map.of("lon", 2.5, "lat", 0.5))).isEqualTo(
      Optional.of(new Cell(2, 0))
    );
  }

  @Test
  void aCoordinateOutsideTheGridIsAnError() {
    final var destination = new DestinationAtCoordinate<>(
      modelGrid,
      longitude,
      latitude
    );
    assertThatThrownBy(() ->
      destination.apply(Map.of("lon", 5.0, "lat", 0.5))
    ).isInstanceOf(IllegalArgumentException.class);
  }
}

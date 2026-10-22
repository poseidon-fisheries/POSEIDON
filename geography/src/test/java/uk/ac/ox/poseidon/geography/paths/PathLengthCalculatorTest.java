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

package uk.ac.ox.poseidon.geography.paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.distance.DistanceCalculator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

class PathLengthCalculatorTest {

  private final Cell start = new Cell(0, 0);
  private final Cell waypoint = new Cell(1, 0);
  private final Cell end = new Cell(1, 1);
  private final GridPathFinder pathFinder = mock(GridPathFinder.class);
  private final DistanceCalculator distanceCalculator = mock(
    DistanceCalculator.class
  );
  private final PathLengthCalculator pathLengthCalculator =
    new PathLengthCalculator(pathFinder, distanceCalculator);

  @BeforeEach
  void setUp() {
    when(pathFinder.getModelGrid()).thenReturn(
      ModelGrid.create(2, 2, new Envelope(0, 2, 0, 2))
    );
    when(
      distanceCalculator.distanceInKm(any(Cell.class), any(Cell.class))
    ).thenReturn(5.0);
    when(distanceCalculator.distanceInKm(anyList())).thenCallRealMethod();
    when(pathFinder.getPath(start, end)).thenReturn(
      Optional.of(ImmutableList.of(start, waypoint, end))
    );
  }

  @Test
  void measuresThePathThePathFinderGives() {
    assertThat(pathLengthCalculator.lengthInKm(start, end)).isEqualTo(10.0);
  }

  @Test
  void aCellIsNoDistanceFromItself() {
    when(pathFinder.getPath(start, start)).thenReturn(
      Optional.of(ImmutableList.of(start))
    );

    assertThat(pathLengthCalculator.lengthInKm(start, start)).isZero();
  }

  @Test
  void measuresThePathBetweenTwoCellsOnlyOnce() {
    pathLengthCalculator.lengthInKm(start, end);
    pathLengthCalculator.lengthInKm(start, end);

    verify(pathFinder, times(1)).getPath(start, end);
    verify(distanceCalculator, times(1)).distanceInKm(anyList());
  }

  @Test
  void measuresEachDirectionApart() {
    when(pathFinder.getPath(end, start)).thenReturn(
      Optional.of(ImmutableList.of(end, start))
    );

    assertThat(pathLengthCalculator.lengthInKm(start, end)).isEqualTo(10.0);
    assertThat(pathLengthCalculator.lengthInKm(end, start)).isEqualTo(5.0);
  }

  @Test
  void throwsEveryTimeThereIsNoPath() {
    when(pathFinder.getPath(start, end)).thenReturn(Optional.empty());

    for (int i = 0; i < 2; i++) {
      assertThatThrownBy(() -> pathLengthCalculator.lengthInKm(start, end))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(start.toString())
        .hasMessageContaining(end.toString());
    }
  }
}

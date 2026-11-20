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

package uk.ac.ox.poseidon.geography.bathymetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.util.MersenneTwisterFast;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class RoughCoastalBathymetricGridFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);
  private final MersenneTwisterFast rng = mock(MersenneTwisterFast.class);

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(rng.nextDouble()).thenReturn(0.5);
    when(rng.nextBoolean(0.25)).thenReturn(true);
  }

  @Test
  void initialLandOccupiesTheConfiguredEasternColumns() {
    final var modelGrid = grid(4, 2);

    final var bathymetricGrid = factory(modelGrid).get(scope);

    assertThat(bathymetricGrid.getModelGrid()).isSameAs(modelGrid);
    assertThat(bathymetricGrid.getElevation(new Cell(0, 0))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(0, 1))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(1, 0))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(1, 1))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(2, 0))).isEqualTo(2);
    assertThat(bathymetricGrid.getElevation(new Cell(2, 1))).isEqualTo(2);
    assertThat(bathymetricGrid.getElevation(new Cell(3, 0))).isEqualTo(2);
    assertThat(bathymetricGrid.getElevation(new Cell(3, 1))).isEqualTo(2);
  }

  @ParameterizedTest
  @CsvSource({ "0, -4", "4, 2" })
  void landStripCanBeAbsentOrSpanTheWholeGrid(
    final int maximumLandWidth,
    final double expectedElevation
  ) {
    final var factory = factory(grid(4, 2))
      .toBuilder()
      .maximumLandWidth(maximumLandWidth)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getAllCells().map(bathymetricGrid::getElevation))
      .hasSize(8)
      .containsOnly(expectedElevation);
  }

  @ParameterizedTest
  @ValueSource(doubles = { 0, 1 })
  void minimumElevationMustBeNegative(final double minimumElevation) {
    final var factory = factory(grid(4, 2))
      .toBuilder()
      .minimumElevation(minimumElevation)
      .build();

    assertThatThrownBy(() -> factory.get(scope)).isInstanceOf(
      IllegalStateException.class
    );
  }

  @ParameterizedTest
  @ValueSource(doubles = { 0, -1 })
  void maximumElevationMustBePositive(final double maximumElevation) {
    final var factory = factory(grid(4, 2))
      .toBuilder()
      .maximumElevation(maximumElevation)
      .build();

    assertThatThrownBy(() -> factory.get(scope)).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void oneRougheningPassFlipsOnlyLandAlreadyAdjacentToWater() {
    final var factory = factory(grid(4, 2))
      .toBuilder()
      .coastalRoughness(1)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getWaterCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(0, 1),
      new Cell(1, 0),
      new Cell(1, 1),
      new Cell(2, 0),
      new Cell(2, 1)
    );
    assertThat(bathymetricGrid.getElevation(new Cell(2, 0))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(2, 1))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(3, 0))).isEqualTo(2);
    assertThat(bathymetricGrid.getElevation(new Cell(3, 1))).isEqualTo(2);
  }

  @ParameterizedTest
  @ValueSource(doubles = { 0, 0.25 })
  void anUnsuccessfulFlipLeavesCoastalLandUnchanged(final double probability) {
    when(rng.nextBoolean(probability)).thenReturn(false);
    final var factory = factory(grid(4, 2))
      .toBuilder()
      .coastalRoughness(1)
      .probabilityOfFlippingLandToWater(probability)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getLandCells()).containsExactlyInAnyOrder(
      new Cell(2, 0),
      new Cell(2, 1),
      new Cell(3, 0),
      new Cell(3, 1)
    );
    assertThat(bathymetricGrid.getElevation(new Cell(2, 0))).isEqualTo(2);
  }

  @Test
  void rougheningDoesNotFlipLandWithoutAWaterNeighbour() {
    final var factory = factory(grid(4, 2))
      .toBuilder()
      .coastalRoughness(2)
      .maximumLandWidth(4)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getWaterCells()).isEmpty();
    assertThat(bathymetricGrid.getAllCells().map(bathymetricGrid::getElevation))
      .hasSize(8)
      .containsOnly(2.0);
  }

  @Test
  void eachRougheningPassCanReachOneMoreLandColumn() {
    final var factory = factory(grid(5, 1))
      .toBuilder()
      .coastalRoughness(2)
      .maximumLandWidth(3)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getWaterCells()).containsExactlyInAnyOrder(
      new Cell(0, 0),
      new Cell(1, 0),
      new Cell(2, 0),
      new Cell(3, 0)
    );
    assertThat(bathymetricGrid.getElevation(new Cell(4, 0))).isEqualTo(2);
  }

  @Test
  void inactiveWaterNeighboursDoNotTriggerCoastalFlips() {
    final var modelGrid = grid(4, 2).withActiveCells(
      List.of(new Cell(2, 0), new Cell(2, 1), new Cell(3, 0), new Cell(3, 1))
    );
    final var factory = factory(modelGrid)
      .toBuilder()
      .coastalRoughness(1)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getLandCells()).containsExactlyInAnyOrder(
      new Cell(2, 0),
      new Cell(2, 1),
      new Cell(3, 0),
      new Cell(3, 1)
    );
  }

  @ParameterizedTest
  @CsvSource({
    "0, 0.5, -2, -6",
    "1, 0, -2, -6",
    "1, 0.5, -3, -5",
    "2, 0.5, -3.5, -4.5",
    "1, 1, -4, -4"
  })
  void smoothingUsesTheConfiguredStrengthAndNumberOfSimultaneousPasses(
    final int smoothingIterations,
    final double smoothingStrength,
    final double expectedWest,
    final double expectedEast
  ) {
    when(rng.nextDouble()).thenReturn(0.25, 0.75, 0.5);
    final var factory = factory(grid(2, 1))
      .toBuilder()
      .maximumLandWidth(0)
      .smoothingIterations(smoothingIterations)
      .smoothingStrength(smoothingStrength)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getElevation(new Cell(0, 0))).isEqualTo(
      expectedWest
    );
    assertThat(bathymetricGrid.getElevation(new Cell(1, 0))).isEqualTo(
      expectedEast
    );
  }

  @Test
  void smoothingKeepsWaterAndLandOnTheirOwnSidesOfSeaLevel() {
    final var factory = factory(grid(2, 1))
      .toBuilder()
      .maximumLandWidth(1)
      .smoothingIterations(1)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getElevation(new Cell(0, 0))).isEqualTo(-3);
    assertThat(bathymetricGrid.getElevation(new Cell(1, 0))).isEqualTo(1.5);
  }

  @Test
  void smoothingAveragesAllActiveNeighboursBeforeClampingToSeaLevel() {
    final var factory = factory(grid(4, 1))
      .toBuilder()
      .smoothingIterations(1)
      .build();

    final var bathymetricGrid = factory.get(scope);

    assertThat(bathymetricGrid.getElevation(new Cell(0, 0))).isEqualTo(-4);
    assertThat(bathymetricGrid.getElevation(new Cell(1, 0))).isEqualTo(-3.25);
    assertThat(bathymetricGrid.getElevation(new Cell(2, 0))).isEqualTo(1.5);
    assertThat(bathymetricGrid.getElevation(new Cell(3, 0))).isEqualTo(2);
  }

  private RoughCoastalBathymetricGridFactory factory(
    final ModelGrid modelGrid
  ) {
    return new RoughCoastalBathymetricGridFactory(
      ignored -> modelGrid,
      0,
      0,
      0.5,
      2,
      -8,
      4,
      0.25,
      ignored -> rng
    );
  }

  private static ModelGrid grid(final int width, final int height) {
    return ModelGrid.create(width, height, new Envelope(0, width, 0, height));
  }
}

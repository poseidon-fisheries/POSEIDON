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

package uk.ac.ox.poseidon.biology.biomass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.biology.biomass.Factories.biomassGrid;
import static uk.ac.ox.poseidon.biology.biomass.Factories.biomassGrids;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

final class BiomassGridsFactoryTest {

  private static final Species COD = new Species("COD", null, "Cod");
  private static final Species HAD = new Species("HAD", null, "Haddock");
  private static final Cell FIRST = new Cell(0, 0);
  private static final Cell SECOND = new Cell(1, 0);
  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);
  private final ModelGrid modelGrid = ModelGrid.create(
    2,
    1,
    new Envelope(0, 2, 0, 1)
  );
  private final Allocator allocator = Map.of(FIRST, 4.0, SECOND, 9.0)::get;

  @BeforeEach
  void setUpStore() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
  }

  @Test
  void singleGridUsesTheSpeciesAndEveryCellAllocation() {
    final BiomassGrid grid = biomassGrid(
      s -> modelGrid,
      s -> COD,
      s -> allocator
    ).get(scope);

    assertThat(grid.getSpecies()).isEqualTo(COD);
    assertThat(grid.getModelGrid()).isSameAs(modelGrid);
    assertThat(grid.getValue(FIRST)).isEqualTo(4);
    assertThat(grid.getValue(SECOND)).isEqualTo(9);
  }

  @Test
  void singleAllocatorIsReusedForEverySpeciesWithIndependentStorage() {
    final List<BiomassGrid> grids = biomassGrids(
      s -> modelGrid,
      s -> List.of(HAD, COD),
      s -> List.of(allocator)
    ).get(scope);

    assertThat(grids)
      .extracting(BiomassGrid::getSpecies)
      .containsExactly(HAD, COD);
    assertThat(grids)
      .extracting(grid -> grid.getValue(FIRST))
      .containsExactly(4.0, 4.0);
    assertThat(grids)
      .extracting(grid -> grid.getValue(SECOND))
      .containsExactly(9.0, 9.0);

    grids.getFirst().setBiomass(FIRST, 1);

    assertThat(grids.getLast().getValue(FIRST)).isEqualTo(4);
  }

  @Test
  void perSpeciesAllocatorsFollowTheConfiguredSpeciesOrder() {
    final Allocator otherAllocator = Map.of(
      FIRST,
      12.0,
      SECOND,
      Double.NaN
    )::get;

    final List<BiomassGrid> grids = biomassGrids(
      s -> modelGrid,
      s -> List.of(HAD, COD),
      s -> List.of(allocator, otherAllocator)
    ).get(scope);

    assertThat(grids)
      .extracting(BiomassGrid::getSpecies)
      .containsExactly(HAD, COD);
    assertThat(grids.getFirst().getValue(FIRST)).isEqualTo(4);
    assertThat(grids.getFirst().getValue(SECOND)).isEqualTo(9);
    assertThat(grids.getLast().getValue(FIRST)).isEqualTo(12);
    assertThat(grids.getLast().getValue(SECOND)).isNaN();
  }

  @Test
  void rejectsAnAllocatorCountThatDoesNotMatchTheSpecies() {
    final BiomassGridsFactory factory = biomassGrids(
      s -> modelGrid,
      s -> List.of(COD, HAD),
      s -> List.of(allocator, allocator, allocator)
    );

    assertThatIllegalStateException()
      .isThrownBy(() -> factory.get(scope))
      .withMessage(
        "Number of biomass allocators must either be 1 or 2 (the same size as species) but was 3."
      );
  }

  @Test
  void rejectsMissingAllocatorsForANonemptySpeciesList() {
    final BiomassGridsFactory factory = biomassGrids(
      s -> modelGrid,
      s -> List.of(COD, HAD),
      s -> List.of()
    );

    assertThatIllegalStateException().isThrownBy(() -> factory.get(scope));
  }
}

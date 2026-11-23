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

package uk.ac.ox.poseidon.gui.portrayals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.utils.Factories.listOf;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.biology.biomass.CarryingCapacityGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.MutableDoubleGrid;

class SpeciesBiomassFieldsPortrayalFactoryTest {

  private final ModelGrid modelGrid = ModelGrid.create(
    3,
    1,
    new Envelope(0, 3, 0, 1)
  );

  @Test
  void portraysASingleSpecies() {
    final BiomassGrid biomassGrid = mock(BiomassGrid.class);
    when(biomassGrid.getSpecies()).thenReturn(new Species("A", null, "A"));
    when(biomassGrid.getModelGrid()).thenReturn(modelGrid);
    when(biomassGrid.getField()).thenReturn(new DoubleGrid2D(3, 1));
    final var factory = new SpeciesBiomassFieldsPortrayalFactory(
      listOf(_ -> biomassGrid),
      listOf(_ ->
        new CarryingCapacityGrid(new MutableDoubleGrid(modelGrid, 1.0))
      ),
      true
    );

    final var simulation = Scenario.builder()
      .component("portrayals", factory)
      .build()
      .withStartingDateTime(LocalDateTime.of(2013, 1, 1, 0, 0))
      .startNewSimulation();

    assertThat(simulation.getComponents(NamedPortrayal.class)).hasSize(1);
  }
}

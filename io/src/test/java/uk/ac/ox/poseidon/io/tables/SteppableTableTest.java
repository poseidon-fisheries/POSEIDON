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

package uk.ac.ox.poseidon.io.tables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.io.tables.Factories.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;

class SteppableTableTest {

  private final Simulation simulation = mock(Simulation.class);
  private final Simulation otherSimulation = mock(Simulation.class);

  @Test
  void samplesLiveValuesOncePerStepAndKeepsRowsSeparateBetweenSimulations() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(otherSimulation.getProductStore()).thenReturn(new ProductStore());
    final var readsOfName = new AtomicInteger();
    final var readsOfValue = new AtomicInteger();
    final ValueFactory<SimulationScope, Void, String> name = _ -> context -> {
      assertThat(context).isNull();
      return "row " + readsOfName.incrementAndGet();
    };
    final ValueFactory<SimulationScope, Void, Double> value = _ -> context -> {
      assertThat(context).isNull();
      return readsOfValue.incrementAndGet() * 1.25;
    };
    final var factory = steppableTable(
      tableDefinition(
        columnDefinition("name", "STRING"),
        columnDefinition("value", "DOUBLE")
      ),
      name,
      value
    );
    final var scope = new SimulationScope(simulation);
    final var table = factory.get(scope);

    assertThat(readsOfName.get()).isZero();
    assertThat(readsOfValue.get()).isZero();
    assertThat(table.get().rowCount()).isZero();
    assertThat(factory.get(scope)).isSameAs(table);

    table.step(null);
    table.step(null);

    assertThat(table.get().stringColumn("name").asList()).containsExactly(
      "row 1",
      "row 2"
    );
    assertThat(
      table.get().doubleColumn("value").asDoubleArray()
    ).containsExactly(1.25, 2.5);
    assertThat(readsOfName.get()).isEqualTo(2);
    assertThat(readsOfValue.get()).isEqualTo(2);
    final var otherTable = factory.get(new SimulationScope(otherSimulation));
    assertThat(otherTable).isNotSameAs(table);
    assertThat(otherTable.get().rowCount()).isZero();
    assertThat(table.get().rowCount()).isEqualTo(2);
  }

  @Test
  void rejectsTooFewOrTooManyValueSuppliers() {
    final var definition = tableDefinition(
      columnDefinition("value", "DOUBLE")
    ).get(uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE);

    assertThatThrownBy(() ->
      new SteppableTable(definition, List.of())
    ).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() ->
      new SteppableTable(definition, List.of(() -> 1.0, () -> 2.0))
    ).isInstanceOf(IllegalArgumentException.class);
  }
}

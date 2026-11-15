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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.core.functions.Factories.some;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.fishing.choices.Factories.epsilonGreedyPicker;
import static uk.ac.ox.poseidon.fishing.choices.Factories.fallbackPicker;
import static uk.ac.ox.poseidon.fishing.choices.Factories.greedyPicker;
import static uk.ac.ox.poseidon.fishing.choices.Factories.memoryBasedOptionValues;
import static uk.ac.ox.poseidon.fishing.choices.Factories.numbersByCell;
import static uk.ac.ox.poseidon.fishing.choices.Factories.randomPicker;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselComponent;

import ec.util.MersenneTwisterFast;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

class PickerContractsFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final Vessel vessel = mock(Vessel.class);
  private final VesselScope scope = new VesselScope(
    new SimulationScope(simulation),
    vessel
  );

  @BeforeEach
  void setUp() {
    simulation.random = new MersenneTwisterFast(42);
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(vessel.getProductStore()).thenReturn(new ProductStore());
  }

  @Test
  void directMemoryReadingFeedsAnUnfilteredGreedyPicker() {
    final var memory = numbersByCell();
    final Cell cell = new Cell(2, 3);
    memory
      .get(scope)
      .apply(vessel)
      .record(
        cell,
        -4.0,
        LocalDateTime.of(2026, 1, 1, 0, 0),
        new KeepLastReceived<>()
      );
    final var values = memoryBasedOptionValues(memory);
    final Map<Cell, Double> visited = new LinkedHashMap<>();
    values.get(scope).forEachEntry(vessel, visited::put);
    assertThat(visited).containsExactly(Map.entry(cell, -4.0));
    assertThat(greedyPicker(values).get(scope).apply(vessel)).contains(cell);
  }

  @Test
  void epsilonAndFallbackFactoriesAcceptNonCellOptions() {
    final EpsilonGreedyPickerFactory<Integer> factory =
      new EpsilonGreedyPickerFactory<>(
        constant(0.0),
        some(constant(9)),
        fallbackPicker(some(constant(7))),
        ignored -> new MersenneTwisterFast(42)
      );
    assertThat(
      factory
        .newInstance(mock(VesselComponentScope.class, CALLS_REAL_METHODS))
        .apply(vessel)
    ).contains(7);
    assertThat(
      randomPicker(constant(List.of(7)))
        .get(scope)
        .apply(vessel)
    ).contains(7);
  }

  @Test
  void directReadingAndGenericPickerCompositionSurviveYamlRoundTrip() {
    final Scenario scenario = Scenario.builder()
      .component(
        "memoryReader",
        vesselComponent(memoryBasedOptionValues(numbersByCell()))
      )
      .component(
        "picker",
        vesselComponent(
          epsilonGreedyPicker(
            0.0,
            randomPicker(constant(List.of(1, 2))),
            fallbackPicker(some(constant(7)), some(constant(9)))
          )
        )
      )
      .build();
    final String yaml = new ScenarioWriter().write(scenario);
    final Scenario loaded = new ScenarioLoader().load(yaml);
    assertThat(new ScenarioWriter().write(loaded)).isEqualTo(yaml);
  }
}

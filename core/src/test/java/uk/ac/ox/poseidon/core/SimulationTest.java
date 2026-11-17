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

package uk.ac.ox.poseidon.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * A simulation starts once and finishes by running its final processes and
 * ending its product store, after which the store refuses further use.
 */
class SimulationTest {

  private final Simulation simulation = newScenario().startNewSimulation();

  private static Scenario newScenario() {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .build();
  }

  @Test
  void aStartedSimulationRefusesToStartAgain() {
    assertThatThrownBy(simulation::start)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("already started");
  }

  @Test
  void finishRunsEveryRegisteredFinalProcess() {
    final List<String> ran = new ArrayList<>();
    simulation.addFinalProcess(() -> ran.add("first"));
    simulation.addFinalProcess(() -> ran.add("second"));

    simulation.finish();

    assertThat(ran).containsExactly("first", "second");
  }

  @Test
  void finishEndsTheProductStore() {
    simulation.finish();

    assertThatThrownBy(simulation.getProductStore()::end)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ended");
  }

  @Test
  void finishSealsTheSchedule() {
    simulation.finish();

    assertThat(simulation.schedule.isSealed()).isTrue();
  }

  @Test
  void startUnsealsTheSchedule() {
    final Simulation fresh = new Simulation(
      42,
      new TemporalSchedule(LocalDateTime.of(2000, 1, 1, 0, 0)),
      UUID.randomUUID()
    );
    fresh.schedule.seal();

    fresh.start();

    assertThat(fresh.schedule.isSealed()).isFalse();
  }
}

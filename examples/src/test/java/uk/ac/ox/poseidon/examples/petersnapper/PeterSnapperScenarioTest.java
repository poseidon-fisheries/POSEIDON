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

package uk.ac.ox.poseidon.examples.petersnapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.TripEndEvent;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.SimpleListener;

class PeterSnapperScenarioTest {

  @Test
  void runsAStep() {
    final Simulation simulation = new PeterSnapperScenario()
      .get()
      .startNewSimulation();
    simulation.step();
    assertThat(simulation.schedule.getSteps()).isEqualTo(1);
  }

  // Runs whole trips, so that the checks made along a trip (arrival in port before landing and
  // before the trip ends) catch tasks missing from, or misplaced in, the behaviour tree.
  @Test
  void endsATrip() {
    final Simulation simulation = new PeterSnapperScenario()
      .get()
      .startNewSimulation();
    final AtomicBoolean tripEnded = new AtomicBoolean(false);
    simulation
      .getEventManager()
      .addListener(
        new SimpleListener<>(TripEndEvent.class, event -> tripEnded.set(true))
      );
    for (int day = 0; day < 30 && !tripEnded.get(); day++) {
      simulation.getTemporalSchedule().stepFor(simulation, Period.ofDays(1));
    }
    assertThat(tripEnded).isTrue();
  }
}

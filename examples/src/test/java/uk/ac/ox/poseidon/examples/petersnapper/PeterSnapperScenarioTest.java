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
import static uk.ac.ox.poseidon.examples.petersnapper.Factories.totalLandingsPerYearAccumulator;
import static uk.ac.ox.poseidon.examples.petersnapper.PeterSnapperCatchabilityCalibration.CATCHABILITY_PROPERTY;

import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.trips.TripEndEvent;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.SimpleListener;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

class PeterSnapperScenarioTest {

  @Test
  void runsAStep() {
    final Simulation simulation = new PeterSnapperScenario()
      .get()
      .startNewSimulation();
    simulation.step();
    assertThat(simulation.schedule.getSteps()).isEqualTo(1);
  }

  // Runs whole trips, so that the checks made along a trip (arrival in port
  // before landing and before the trip ends) catch tasks missing from, or
  // misplaced in, the behaviour tree.
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

  // Each run hands out identity hash codes, so a result that depended on
  // them (iterating a HashMap keyed by vessels, say) would differ between
  // these runs.
  @Test
  void sameSeedGivesSameLandings() {
    final Scenario scenario = new PeterSnapperScenario().get();
    final Map<Integer, Double> firstLandings = landingsForAYear(scenario);
    assertThat(landingsForAYear(scenario)).isEqualTo(firstLandings);
    assertThat(landingsForAYear(new PeterSnapperScenario().get())).isEqualTo(
      firstLandings
    );
  }

  // Stored from a run, so that a change that was meant to keep behaviour
  // and does not fails here. A change meant to alter the draws replaces
  // these values and says why.
  @Test
  void landingsMatchTheBaseline() {
    assertThat(landingsForAYear(new PeterSnapperScenario().get())).isEqualTo(
      Map.of(1, 2.108111508988373E7)
    );
  }

  // The loader builds every factory through its builder, so this also checks
  // that each factory in the scenario has one that sets all its properties
  @Test
  void aScenarioWrittenAndLoadedAgainIsTheSame() {
    final Scenario scenario = new PeterSnapperScenario().get();
    final String yaml = new ScenarioWriter().write(scenario);
    final Scenario loaded = new ScenarioLoader().load(yaml);
    assertThat(new ScenarioWriter().write(loaded)).isEqualTo(yaml);
    assertThat(landingsForAYear(loaded)).isEqualTo(landingsForAYear(scenario));
  }

  @Test
  void simulationsRunInParallelGiveTheirSequentialLandings() {
    final Scenario scenario = new PeterSnapperScenario().get();
    final Map<Integer, Double> sequentialLandings = landingsForAYear(scenario);
    final List<Map<Integer, Double>> parallelLandings = IntStream.range(0, 4)
      .parallel()
      .mapToObj(_ -> landingsForAYear(scenario))
      .toList();
    assertThat(parallelLandings).containsOnly(sequentialLandings);
  }

  // Building and stepping a simulation must not change the scenario's
  // factories, which every simulation of the scenario shares.
  @Test
  void runningASimulationLeavesTheScenarioUnchanged() {
    final Scenario scenario = new PeterSnapperScenario().get();
    // A new writer each time: one writer keeps numbering its anchors
    final String before = new ScenarioWriter().write(scenario);
    landingsForAYear(scenario);
    assertThat(new ScenarioWriter().write(scenario)).isEqualTo(before);
  }

  // Calibration varies the gear's proportion through this address, a scenario
  // component's value. The derived scenario must be the one written with the
  // value set: the component, the gear and the declarations above it replaced
  // wherever they are shared, nothing else changed.
  @Test
  void theCalibratedAddressDerivesTheScenarioWithTheValueSet() {
    final Scenario scenario = new PeterSnapperScenario().get();
    final String yaml = new ScenarioWriter().write(scenario);
    final String yamlWithTheValueSet = yaml.replace(
      "value: 6.41964E-4",
      "value: 0.001"
    );
    assertThat(yamlWithTheValueSet).isNotEqualTo(yaml);

    final Scenario derived = scenario.with(CATCHABILITY_PROPERTY, 0.001);

    assertThat(new ScenarioWriter().write(derived)).isEqualTo(
      yamlWithTheValueSet
    );
    assertThat(landingsFor(derived, Period.ofDays(60)))
      .isNotEmpty()
      .isEqualTo(
        landingsFor(
          new ScenarioLoader().load(yamlWithTheValueSet),
          Period.ofDays(60)
        )
      );
    assertThat(new ScenarioWriter().write(scenario)).isEqualTo(yaml);
  }

  private static Map<Integer, Double> landingsForAYear(
    final Scenario scenario
  ) {
    return landingsFor(scenario, Period.ofYears(1));
  }

  private static Map<Integer, Double> landingsFor(
    final Scenario scenario,
    final Period period
  ) {
    final Simulation simulation = scenario
      .withComponent("totalLandingsPerYear", totalLandingsPerYearAccumulator())
      .startNewSimulation(0);
    try {
      simulation.getTemporalSchedule().stepFor(simulation, period);
      return Map.copyOf(
        simulation.getComponent(TotalLandingsPerYearAccumulator.class).get()
      );
    } finally {
      simulation.finish();
    }
  }
}

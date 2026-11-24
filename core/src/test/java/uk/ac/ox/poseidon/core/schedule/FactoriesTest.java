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

package uk.ac.ox.poseidon.core.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledOnce;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledRepeating;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledRepeatingFromStart;
import static uk.ac.ox.poseidon.core.schedule.Factories.steppableRandomSequence;
import static uk.ac.ox.poseidon.core.schedule.Factories.steppableSequence;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.time.Factories.hours;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sim.engine.RandomSequence;
import sim.engine.Sequence;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

class FactoriesTest {

  private static final LocalDateTime START = LocalDateTime.of(2000, 1, 1, 0, 0);

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation();
  private final SimulationScope scope = new SimulationScope(simulation);
  private final TemporalSchedule schedule = simulation.getTemporalSchedule();
  private final List<LocalDateTime> firings = new ArrayList<>();
  private final Steppable recordFiring = _ -> firings.add(schedule.now());

  @Test
  void scheduledOnceSchedulesTheSteppableAndReturnsIt() {
    final LocalDateTime at = START.plusHours(2);

    final Steppable result = scheduledOnce(dateTime(at), _ -> recordFiring).get(
      scope
    );

    assertThat(result).isSameAs(recordFiring);
    schedule.stepUntil(simulation, at.plusSeconds(1));
    assertThat(firings).containsExactly(at);
  }

  @Test
  void scheduledOnceWithAnOrderingSchedulesTheSteppableAndReturnsIt() {
    final LocalDateTime at = START.plusHours(2);

    final Steppable result = scheduledOnce(
      dateTime(at),
      _ -> recordFiring,
      3
    ).get(scope);

    assertThat(result).isSameAs(recordFiring);
    schedule.stepUntil(simulation, at.plusSeconds(1));
    assertThat(firings).containsExactly(at);
  }

  @Test
  void scheduledRepeatingSchedulesTheSteppableAtEveryInterval() {
    final LocalDateTime first = START.plusHours(1);

    final Steppable result = scheduledRepeating(
      dateTime(first),
      hours(2),
      _ -> recordFiring
    ).get(scope);

    assertThat(result).isSameAs(recordFiring);
    schedule.stepUntil(simulation, START.plusHours(6));
    assertThat(firings).containsExactly(
      first,
      first.plusHours(2),
      first.plusHours(4)
    );
  }

  @Test
  void scheduledRepeatingWithAnOrderingSchedulesAtEveryInterval() {
    final LocalDateTime first = START.plusHours(1);

    final Steppable result = scheduledRepeating(
      dateTime(first),
      hours(2),
      _ -> recordFiring,
      3
    ).get(scope);

    assertThat(result).isSameAs(recordFiring);
    schedule.stepUntil(simulation, START.plusHours(6));
    assertThat(firings).containsExactly(
      first,
      first.plusHours(2),
      first.plusHours(4)
    );
  }

  @Test
  void scheduledRepeatingFromStartStartsAtTheSimulationsStart() {
    final Steppable result = scheduledRepeatingFromStart(
      hours(1),
      _ -> recordFiring
    ).get(scope);

    assertThat(result).isSameAs(recordFiring);
    schedule.stepUntil(simulation, START.plusHours(3));
    assertThat(firings).containsExactly(
      START,
      START.plusHours(1),
      START.plusHours(2)
    );
  }

  @Test
  void scheduledRepeatingFromStartWithAnOrderingStartsAtTheStart() {
    final Steppable result = scheduledRepeatingFromStart(
      hours(1),
      _ -> recordFiring,
      3
    ).get(scope);

    assertThat(result).isSameAs(recordFiring);
    schedule.stepUntil(simulation, START.plusHours(3));
    assertThat(firings).containsExactly(
      START,
      START.plusHours(1),
      START.plusHours(2)
    );
  }

  @Test
  void steppableSequenceRunsTheResolvedSteppablesInOrder() {
    final List<String> runs = new ArrayList<>();
    final Steppable first = _ -> runs.add("first");
    final Steppable second = _ -> runs.add("second");

    final Steppable sequence = steppableSequence(_ -> first, _ -> second).get(
      scope
    );
    sequence.step(simulation);

    assertThat(sequence).isInstanceOf(Sequence.class);
    assertThat(runs).containsExactly("first", "second");
  }

  @Test
  void steppableRandomSequenceRunsEveryResolvedSteppable() {
    final List<String> runs = new ArrayList<>();
    final Steppable first = _ -> runs.add("first");
    final Steppable second = _ -> runs.add("second");

    final RandomSequence sequence = steppableRandomSequence(
      _ -> first,
      _ -> second
    ).get(scope);
    sequence.step(simulation);

    assertThat(runs).containsExactlyInAnyOrder("first", "second");
  }
}

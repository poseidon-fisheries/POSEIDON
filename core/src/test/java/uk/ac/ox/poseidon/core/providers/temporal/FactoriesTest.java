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

package uk.ac.ox.poseidon.core.providers.temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.clock;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dateOf;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dateTimeAfter;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dateTimeBefore;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dayOfWeek;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dayOfWeekOf;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.durationBetween;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.nextOccurrenceAfter;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.now;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.startingDateTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.thisYear;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.timeOfDay;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.timeOfDayOf;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.today;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.yearOf;
import static uk.ac.ox.poseidon.core.time.Factories.duration;
import static uk.ac.ox.poseidon.core.time.Factories.period;
import static uk.ac.ox.poseidon.core.time.Factories.time;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * Each entry point resolves to a value that reads the simulation's clock, or
 * does temporal arithmetic over one.
 */
class FactoriesTest {

  private static final LocalDateTime START = LocalDateTime.of(
    2000,
    1,
    1,
    0,
    0,
    0
  );
  private static final LocalDateTime REFERENCE = LocalDateTime.of(
    2025,
    6,
    1,
    7,
    0,
    0
  );
  private static final ValueFactory<
    Scope,
    Object,
    LocalDateTime
  > REFERENCE_FACTORY = constant(_ -> REFERENCE);

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation(42);
  private final SimulationScope scope = new SimulationScope(simulation);

  @Test
  void nowIsTheSimulationsClock() {
    assertThat(now().get(scope).apply(null)).isEqualTo(START);
  }

  @Test
  void todayIsTheClockDate() {
    assertThat(today().get(scope).apply(null)).isEqualTo(START.toLocalDate());
  }

  @Test
  void timeOfDayIsTheClockTimeOfDay() {
    assertThat(timeOfDay().get(scope).apply(null)).isEqualTo(
      START.toLocalTime()
    );
  }

  @Test
  void thisYearIsTheClockYear() {
    assertThat(thisYear().get(scope).apply(null)).isEqualTo(START.getYear());
  }

  @Test
  void dayOfWeekIsTheClockDayOfWeek() {
    assertThat(dayOfWeek().get(scope).apply(null)).isEqualTo(
      START.getDayOfWeek()
    );
  }

  @Test
  void startingDateTimeIsWhereTheClockStarts() {
    assertThat(startingDateTime().get(scope)).isEqualTo(START);
  }

  @Test
  void theClockIsTheSimulationsSchedule() {
    assertThat(clock().get(scope)).isSameAs(simulation.getTemporalSchedule());
  }

  @Test
  void theExtractorsTakeAnyDateTime() {
    assertThat(dateOf(REFERENCE_FACTORY).get(scope).apply(null)).isEqualTo(
      REFERENCE.toLocalDate()
    );
    assertThat(yearOf(REFERENCE_FACTORY).get(scope).apply(null)).isEqualTo(
      2025
    );
    assertThat(timeOfDayOf(REFERENCE_FACTORY).get(scope).apply(null)).isEqualTo(
      REFERENCE.toLocalTime()
    );
    assertThat(dayOfWeekOf(REFERENCE_FACTORY).get(scope).apply(null)).isEqualTo(
      REFERENCE.getDayOfWeek()
    );
  }

  @Test
  void theNextOccurrenceAfterStartsFromTheGivenReference() {
    final LocalDateTime lateEvening = LocalDateTime.of(2025, 6, 1, 23, 0);

    assertThat(
      nextOccurrenceAfter(
        constant(_ -> lateEvening),
        time(6, 0, 0),
        time(22, 0, 0)
      )
        .get(scope)
        .apply(null)
    ).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
  }

  @Test
  void durationBetweenIsTheTimeFromTheStartToTheEnd() {
    assertThat(
      durationBetween(now(), REFERENCE_FACTORY).get(scope).apply(null)
    ).isEqualTo(Duration.between(START, REFERENCE));
  }

  @Test
  void dateTimeAfterAddsAValueFactory() {
    assertThat(
      dateTimeAfter(REFERENCE_FACTORY, constant(_ -> Duration.ofHours(2)))
        .get(scope)
        .apply(null)
    ).isEqualTo(REFERENCE.plusHours(2));
  }

  @Test
  void dateTimeAfterAddsAConstantDuration() {
    assertThat(
      dateTimeAfter(REFERENCE_FACTORY, duration(0, 2, 0, 0))
        .get(scope)
        .apply(null)
    ).isEqualTo(REFERENCE.plusHours(2));
  }

  @Test
  void dateTimeAfterAddsAConstantPeriod() {
    assertThat(
      dateTimeAfter(REFERENCE_FACTORY, period("P1M")).get(scope).apply(null)
    ).isEqualTo(REFERENCE.plusMonths(1));
  }

  @Test
  void dateTimeAfterAddsAMappingAmountToAValueReference() {
    final MappingFactory<Scope, Object, Integer, Duration> travelHours =
      scope -> (_, hours) -> Duration.ofHours(hours);
    final var arrival = dateTimeAfter(REFERENCE_FACTORY, travelHours).get(
      scope
    );
    assertThat(arrival.apply(null, 2)).isEqualTo(REFERENCE.plusHours(2));
    assertThat(arrival.apply(null, 5)).isEqualTo(REFERENCE.plusHours(5));
  }

  @Test
  void dateTimeAfterAddsAMappingAmountToAMappingReference() {
    final MappingFactory<Scope, Object, Integer, LocalDateTime> stop = scope ->
      (_, hours) ->
        REFERENCE.plusHours(hours);
    final MappingFactory<Scope, Object, Integer, Duration> leg = scope ->
      (_, hours) ->
        Duration.ofMinutes(hours);
    assertThat(dateTimeAfter(stop, leg).get(scope).apply(null, 3)).isEqualTo(
      REFERENCE.plusHours(3).plusMinutes(3)
    );
  }

  @Test
  void dateTimeBeforeSubtractsAValueFactory() {
    assertThat(
      dateTimeBefore(REFERENCE_FACTORY, constant(_ -> Duration.ofHours(2)))
        .get(scope)
        .apply(null)
    ).isEqualTo(REFERENCE.minusHours(2));
  }

  @Test
  void dateTimeBeforeSubtractsAConstantDuration() {
    assertThat(
      dateTimeBefore(REFERENCE_FACTORY, duration(0, 2, 0, 0))
        .get(scope)
        .apply(null)
    ).isEqualTo(REFERENCE.minusHours(2));
  }

  @Test
  void dateTimeBeforeSubtractsAConstantPeriod() {
    assertThat(
      dateTimeBefore(REFERENCE_FACTORY, period("P1M")).get(scope).apply(null)
    ).isEqualTo(REFERENCE.minusMonths(1));
  }
}

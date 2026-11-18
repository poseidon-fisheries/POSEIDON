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
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDate;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDayOfWeek;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentYear;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dateTimeAfter;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.dateTimeBefore;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.durationUntil;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.hasDateTime;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.nextOccurrenceAfter;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.nextOccurrenceOf;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.startingDateTime;
import static uk.ac.ox.poseidon.core.time.Factories.duration;
import static uk.ac.ox.poseidon.core.time.Factories.period;
import static uk.ac.ox.poseidon.core.time.Factories.time;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
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
  > REFERENCE_FACTORY = constant(REFERENCE);

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation(42);
  private final SimulationScope scope = new SimulationScope(simulation);

  @Test
  void currentDateTimeIsTheSimulationsClock() {
    assertThat(currentDateTime().get(scope).apply(null)).isEqualTo(START);
  }

  @Test
  void currentDateIsTheClockDate() {
    assertThat(currentDate().get(scope).apply(null)).isEqualTo(
      START.toLocalDate()
    );
  }

  @Test
  void currentTimeIsTheClockTimeOfDay() {
    assertThat(currentTime().get(scope).apply(null)).isEqualTo(
      START.toLocalTime()
    );
  }

  @Test
  void currentYearIsTheClockYear() {
    assertThat(currentYear().get(scope).apply(null)).isEqualTo(START.getYear());
  }

  @Test
  void currentDayOfWeekIsTheClockDayOfWeek() {
    assertThat(currentDayOfWeek().get(scope).apply(null)).isEqualTo(
      START.getDayOfWeek()
    );
  }

  @Test
  void startingDateTimeIsWhereTheClockStarts() {
    assertThat(startingDateTime().get(scope)).isEqualTo(START);
  }

  @Test
  void hasDateTimeIsTrueWhileTheClockHasOne() {
    assertThat(hasDateTime().get(scope).apply(null)).isTrue();
  }

  @Test
  void theNextOccurrenceIsTheNextOfTheGivenTimesAfterTheClock() {
    assertThat(
      nextOccurrenceOf(time(6, 0, 0), time(22, 0, 0))
        .get(scope)
        .apply(null)
    ).isEqualTo(LocalDateTime.of(2000, 1, 1, 6, 0));
  }

  @Test
  void theNextOccurrenceAfterStartsFromTheGivenReference() {
    final LocalDateTime lateEvening = LocalDateTime.of(2025, 6, 1, 23, 0);

    assertThat(
      nextOccurrenceAfter(constant(lateEvening), time(6, 0, 0), time(22, 0, 0))
        .get(scope)
        .apply(null)
    ).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
  }

  @Test
  void durationUntilIsTheTimeFromTheClockToTheReference() {
    assertThat(
      durationUntil(REFERENCE_FACTORY).get(scope).apply(null)
    ).isEqualTo(Duration.between(START, REFERENCE));
  }

  @Test
  void dateTimeAfterAddsAValueFactory() {
    assertThat(
      dateTimeAfter(REFERENCE_FACTORY, constant(Duration.ofHours(2)))
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
  void dateTimeBeforeSubtractsAValueFactory() {
    assertThat(
      dateTimeBefore(REFERENCE_FACTORY, constant(Duration.ofHours(2)))
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

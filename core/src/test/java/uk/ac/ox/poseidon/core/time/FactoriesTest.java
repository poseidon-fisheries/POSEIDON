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

package uk.ac.ox.poseidon.core.time;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.time.Factories.date;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.time.Factories.dateTimeAfterStarting;
import static uk.ac.ox.poseidon.core.time.Factories.days;
import static uk.ac.ox.poseidon.core.time.Factories.duration;
import static uk.ac.ox.poseidon.core.time.Factories.exponentiallyDistributedDuration;
import static uk.ac.ox.poseidon.core.time.Factories.hours;
import static uk.ac.ox.poseidon.core.time.Factories.minutes;
import static uk.ac.ox.poseidon.core.time.Factories.period;
import static uk.ac.ox.poseidon.core.time.Factories.seconds;
import static uk.ac.ox.poseidon.core.time.Factories.startOf;
import static uk.ac.ox.poseidon.core.time.Factories.time;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;

/**
 * Each entry point builds the fixed value it names, or the relative value it
 * computes from the simulation's start.
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

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation(42);
  private final SimulationScope scope = new SimulationScope(simulation);

  @Test
  void aDurationIsTheSumOfItsParts() {
    assertThat(duration(1, 2, 3, 4).get(GLOBAL_SCOPE)).isEqualTo(
      Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4)
    );
  }

  @Test
  void aDurationParsesAnIso8601String() {
    assertThat(duration("PT1H30M").get(GLOBAL_SCOPE)).isEqualTo(
      Duration.ofMinutes(90)
    );
  }

  @Test
  void daysHoursMinutesAndSecondsAreWholeDurations() {
    assertThat(days(2).get(GLOBAL_SCOPE)).isEqualTo(Duration.ofDays(2));
    assertThat(hours(3).get(GLOBAL_SCOPE)).isEqualTo(Duration.ofHours(3));
    assertThat(minutes(4).get(GLOBAL_SCOPE)).isEqualTo(Duration.ofMinutes(4));
    assertThat(seconds(5).get(GLOBAL_SCOPE)).isEqualTo(Duration.ofSeconds(5));
  }

  @Test
  void aPeriodIsTheSumOfItsParts() {
    assertThat(period("P1Y2M3D").get(GLOBAL_SCOPE)).isEqualTo(
      Period.of(1, 2, 3)
    );
    assertThat(period(Period.of(4, 5, 6)).get(GLOBAL_SCOPE)).isEqualTo(
      Period.of(4, 5, 6)
    );
  }

  @Test
  void aDateTimeIsTheGivenDateAndTime() {
    final LocalDateTime dateTime = LocalDateTime.of(2025, 6, 1, 7, 8, 9);

    assertThat(dateTime(dateTime).get(GLOBAL_SCOPE)).isEqualTo(dateTime);
  }

  @Test
  void startOfIsMidnightOfTheGivenDate() {
    assertThat(startOf(LocalDate.of(2020, 2, 3)).get(GLOBAL_SCOPE)).isEqualTo(
      LocalDateTime.of(2020, 2, 3, 0, 0)
    );
  }

  @Test
  void aTimeIsTheGivenTimeOfDay() {
    assertThat(time(1, 2, 3).get(GLOBAL_SCOPE)).isEqualTo(
      LocalTime.of(1, 2, 3)
    );
  }

  @Test
  void aDateIsTheGivenDate() {
    assertThat(date(2020, 2, 3).get(GLOBAL_SCOPE)).isEqualTo(
      LocalDate.of(2020, 2, 3)
    );
    assertThat(date(LocalDate.of(2020, 2, 3)).get(GLOBAL_SCOPE)).isEqualTo(
      LocalDate.of(2020, 2, 3)
    );
  }

  @Test
  void dateTimeAfterStartingAddsTheAmountToTheSimulationsStart() {
    assertThat(
      dateTimeAfterStarting(duration(0, 5, 0, 0)).get(scope)
    ).isEqualTo(START.plusHours(5));
  }

  @Test
  void anExponentiallyDistributedDurationResolvesItsSupplier() {
    final ExponentiallyDistributedDuration supplier =
      exponentiallyDistributedDuration(seconds(2)).get(scope);

    assertThat(supplier.get()).isPositive();
  }
}

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

package uk.ac.ox.poseidon.core.predicates.temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.afterDate;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.afterDateTime;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.afterTime;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.dateIsBefore;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.dateTimeIsBefore;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.timeIsBefore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class FactoriesTest {

  private static final LocalDate DATE = LocalDate.of(2025, 6, 15);
  private static final LocalTime TIME = LocalTime.NOON;
  private static final LocalDateTime DATE_TIME = LocalDateTime.of(
    2025,
    6,
    15,
    12,
    0
  );

  private final Factory<Scope, LocalDate> dateFactory = scope -> DATE;
  private final Factory<Scope, LocalTime> timeFactory = scope -> TIME;
  private final Factory<Scope, LocalDateTime> dateTimeFactory = scope ->
    DATE_TIME;
  private final ValueFactory<Scope, Object, LocalDate> dateValue = scope ->
    context ->
      DATE;
  private final ValueFactory<Scope, Object, LocalTime> timeValue = scope ->
    context ->
      TIME;
  private final ValueFactory<Scope, Object, LocalDateTime> dateTimeValue =
    scope -> context -> DATE_TIME;

  @Test
  void afterTimeFromAConstantFactoryIsTrueOnlyAfterTheReference() {
    assertAfterTime(afterTime(timeFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void afterTimeFromAValueFactoryIsTrueOnlyAfterTheReference() {
    assertAfterTime(afterTime(timeValue).get(GLOBAL_SCOPE));
  }

  @Test
  void afterDateFromAConstantFactoryIsTrueOnlyAfterTheReference() {
    assertAfterDate(afterDate(dateFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void afterDateTimeFromAConstantFactoryIsTrueOnlyAfterTheReference() {
    assertAfterDateTime(afterDateTime(dateTimeFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void timeIsBeforeFromAConstantFactoryIsTrueOnlyBeforeTheReference() {
    assertTimeIsBefore(timeIsBefore(timeFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void timeIsBeforeFromAValueFactoryIsTrueOnlyBeforeTheReference() {
    assertTimeIsBefore(timeIsBefore(timeValue).get(GLOBAL_SCOPE));
  }

  @Test
  void dateIsBeforeFromAConstantFactoryIsTrueOnlyBeforeTheReference() {
    assertDateIsBefore(dateIsBefore(dateFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void dateIsBeforeFromAValueFactoryIsTrueOnlyBeforeTheReference() {
    assertDateIsBefore(dateIsBefore(dateValue).get(GLOBAL_SCOPE));
  }

  @Test
  void dateTimeIsBeforeFromAConstantFactoryIsTrueOnlyBeforeTheReference() {
    assertDateTimeIsBefore(dateTimeIsBefore(dateTimeFactory).get(GLOBAL_SCOPE));
  }

  @Test
  void dateTimeIsBeforeFromAValueFactoryIsTrueOnlyBeforeTheReference() {
    assertDateTimeIsBefore(dateTimeIsBefore(dateTimeValue).get(GLOBAL_SCOPE));
  }

  private static void assertAfterTime(
    final BiFunction<Object, Comparable<LocalTime>, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, TIME.plusSeconds(1))).isTrue();
    assertThat(predicate.apply(null, TIME)).isFalse();
  }

  private static void assertAfterDate(
    final BiFunction<Object, LocalDate, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, DATE.plusDays(1))).isTrue();
    assertThat(predicate.apply(null, DATE)).isFalse();
  }

  private static void assertAfterDateTime(
    final BiFunction<Object, LocalDateTime, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, DATE_TIME.plusSeconds(1))).isTrue();
    assertThat(predicate.apply(null, DATE_TIME)).isFalse();
  }

  private static void assertTimeIsBefore(
    final BiFunction<Object, Comparable<LocalTime>, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, TIME.minusSeconds(1))).isTrue();
    assertThat(predicate.apply(null, TIME)).isFalse();
  }

  private static void assertDateIsBefore(
    final BiFunction<Object, LocalDate, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, DATE.minusDays(1))).isTrue();
    assertThat(predicate.apply(null, DATE)).isFalse();
  }

  private static void assertDateTimeIsBefore(
    final BiFunction<Object, LocalDateTime, Boolean> predicate
  ) {
    assertThat(predicate.apply(null, DATE_TIME.minusSeconds(1))).isTrue();
    assertThat(predicate.apply(null, DATE_TIME)).isFalse();
  }
}

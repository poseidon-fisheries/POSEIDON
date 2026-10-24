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

import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.greaterThan;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.lessThan;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.predicates.comparable.GreaterThanFactory;
import uk.ac.ox.poseidon.core.predicates.comparable.LessThanFactory;

/**
 * Factories for yes/no mappings that compare a tested date, time, or date-time
 * against a reference read under the context. Unlike {@link LocalDate} and
 * {@link LocalDateTime}, {@link LocalTime} implements
 * {@code Comparable<LocalTime>} directly, so the {@code LocalTime}-based
 * predicates here are thin, same-named wrappers around
 * {@link uk.ac.ox.poseidon.core.predicates.comparable.Factories} rather than
 * bespoke implementations.
 */
@SuppressWarnings("overloads")
public final class Factories {

  private Factories() {}

  /**
   * @param referenceTime the time the resulting predicate tests against
   * @return the same as {@link #afterTime(ValueFactory)}, over a constant
   *         {@code referenceTime}
   */
  public static <S extends Scope> GreaterThanFactory<
    S,
    Object,
    LocalTime
  > afterTime(final Factory<? super S, ? extends LocalTime> referenceTime) {
    return afterTime(constant(referenceTime));
  }

  /**
   * @param referenceTime factory for the time the resulting predicate tests
   *                      against
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         predicate that is true iff the tested time is strictly after the
   *         resolved reference time. Delegates to
   *         {@link uk.ac.ox.poseidon.core.predicates.comparable.Factories#greaterThan(ValueFactory)}.
   */
  public static <S extends Scope, C> GreaterThanFactory<
    S,
    C,
    LocalTime
  > afterTime(
    final ValueFactory<? super S, ? super C, ? extends LocalTime> referenceTime
  ) {
    return greaterThan(referenceTime);
  }

  /**
   * @param referenceDate the date the resulting predicate tests against
   * @return the same as {@link #afterDate(ValueFactory)}, over a constant
   *         {@code referenceDate}
   */
  public static <S extends Scope> AfterDateFactory<S, Object> afterDate(
    final Factory<? super S, ? extends LocalDate> referenceDate
  ) {
    return afterDate(constant(referenceDate));
  }

  /**
   * @param referenceDate factory for the date the resulting predicate tests
   *                      against
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for an
   *         {@link AfterDate} predicate over the resolved reference date
   * @see AfterDate
   */
  public static <S extends Scope, C> AfterDateFactory<S, C> afterDate(
    final ValueFactory<? super S, ? super C, ? extends LocalDate> referenceDate
  ) {
    return new AfterDateFactory<>(referenceDate);
  }

  /**
   * @param referenceDateTime the date-time the resulting predicate tests against
   * @return the same as {@link #afterDateTime(ValueFactory)}, over a constant
   *         {@code referenceDateTime}
   */
  public static <S extends Scope> AfterDateTimeFactory<S, Object> afterDateTime(
    final Factory<? super S, ? extends LocalDateTime> referenceDateTime
  ) {
    return afterDateTime(constant(referenceDateTime));
  }

  /**
   * @param referenceDateTime factory for the date-time the resulting predicate
   *                          tests against
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for an
   *         {@link AfterDateTime} predicate over the resolved reference
   *         date-time
   * @see AfterDateTime
   */
  public static <S extends Scope, C> AfterDateTimeFactory<S, C> afterDateTime(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends LocalDateTime
    > referenceDateTime
  ) {
    return new AfterDateTimeFactory<>(referenceDateTime);
  }

  /**
   * @param referenceTime the time the resulting predicate tests against
   * @return the same as {@link #timeIsBefore(ValueFactory)}, over a constant
   *         {@code referenceTime}
   */
  public static <S extends Scope> LessThanFactory<
    S,
    Object,
    LocalTime
  > timeIsBefore(final Factory<? super S, ? extends LocalTime> referenceTime) {
    return timeIsBefore(constant(referenceTime));
  }

  /**
   * @param referenceTime factory for the time the resulting predicate tests
   *                      against
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         predicate that is true iff the tested time is strictly before the
   *         resolved reference time. Delegates to
   *         {@link uk.ac.ox.poseidon.core.predicates.comparable.Factories#lessThan(ValueFactory)}.
   */
  public static <S extends Scope, C> LessThanFactory<
    S,
    C,
    LocalTime
  > timeIsBefore(
    final ValueFactory<? super S, ? super C, ? extends LocalTime> referenceTime
  ) {
    return lessThan(referenceTime);
  }

  /**
   * @param referenceDate the date the resulting predicate tests against
   * @return the same as {@link #dateIsBefore(ValueFactory)}, over a constant
   *         {@code referenceDate}
   */
  public static <S extends Scope> DateIsBeforeFactory<S, Object> dateIsBefore(
    final Factory<? super S, ? extends LocalDate> referenceDate
  ) {
    return dateIsBefore(constant(referenceDate));
  }

  /**
   * @param referenceDate factory for the date the resulting predicate tests
   *                      against
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         {@link DateIsBefore} predicate over the resolved reference date
   * @see DateIsBefore
   */
  public static <S extends Scope, C> DateIsBeforeFactory<S, C> dateIsBefore(
    final ValueFactory<? super S, ? super C, ? extends LocalDate> referenceDate
  ) {
    return new DateIsBeforeFactory<>(referenceDate);
  }

  /**
   * @param referenceDateTime the date-time the resulting predicate tests against
   * @return the same as {@link #dateTimeIsBefore(ValueFactory)}, over a constant
   *         {@code referenceDateTime}
   */
  public static <S extends Scope> DateTimeIsBeforeFactory<
    S,
    Object
  > dateTimeIsBefore(
    final Factory<? super S, ? extends LocalDateTime> referenceDateTime
  ) {
    return dateTimeIsBefore(constant(referenceDateTime));
  }

  /**
   * @param referenceDateTime factory for the date-time the resulting predicate
   *                          tests against
   * @return a {@link uk.ac.ox.poseidon.core.MappingFactory} for a
   *         {@link DateTimeIsBefore} predicate over the resolved reference
   *         date-time
   * @see DateTimeIsBefore
   */
  public static <S extends Scope, C> DateTimeIsBeforeFactory<
    S,
    C
  > dateTimeIsBefore(
    final ValueFactory<
      ? super S,
      ? super C,
      ? extends LocalDateTime
    > referenceDateTime
  ) {
    return new DateTimeIsBeforeFactory<>(referenceDateTime);
  }
}

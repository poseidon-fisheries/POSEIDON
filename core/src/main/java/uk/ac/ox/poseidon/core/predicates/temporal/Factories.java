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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.predicates.comparable.GreaterThanFactory;
import uk.ac.ox.poseidon.core.predicates.comparable.LessThanFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Supplier;

import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.greaterThan;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.lessThan;

/**
 * Factories for {@link java.util.function.Predicate}s that compare a tested date, time, or
 * date-time against a reference. Unlike {@link LocalDate} and {@link LocalDateTime},
 * {@link LocalTime} implements {@code Comparable<LocalTime>} directly, so the
 * {@code LocalTime}-based predicates here are thin, same-named wrappers around
 * {@link uk.ac.ox.poseidon.core.predicates.comparable.Factories} rather than bespoke
 * implementations.
 */
public class Factories {
    private Factories() {}

    /**
     * @param referenceTime factory for the time the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a predicate that is true
     * iff the tested time is strictly after the resolved reference time. Delegates to
     * {@link uk.ac.ox.poseidon.core.predicates.comparable.Factories#greaterThan(Factory)}.
     */
    public static <S extends Scope> GreaterThanFactory<S, LocalTime> afterTime(
        final Factory<? super S, ? extends Supplier<? extends LocalTime>> referenceTime
    ) {
        return greaterThan(referenceTime);
    }

    /**
     * @param referenceDate factory for the date the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link AfterDate}
     * predicate over the resolved reference date
     * @see AfterDate
     */
    public static <S extends Scope> AfterDateFactory<S> afterDate(
        final Factory<? super S, ? extends Supplier<? extends LocalDate>> referenceDate
    ) {
        return new AfterDateFactory<>(referenceDate);
    }

    /**
     * @param referenceDateTime factory for the date-time the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link AfterDateTime}
     * predicate over the resolved reference date-time
     * @see AfterDateTime
     */
    public static <S extends Scope> AfterDateTimeFactory<S> afterDateTime(
        final Factory<? super S, ? extends Supplier<? extends LocalDateTime>> referenceDateTime
    ) {
        return new AfterDateTimeFactory<>(referenceDateTime);
    }

    /**
     * @param referenceTime factory for the time the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a predicate that is true
     * iff the tested time is strictly before the resolved reference time. Delegates to
     * {@link uk.ac.ox.poseidon.core.predicates.comparable.Factories#lessThan(Factory)}.
     */
    public static <S extends Scope> LessThanFactory<S, LocalTime> timeIsBefore(
        final Factory<? super S, ? extends Supplier<? extends LocalTime>> referenceTime
    ) {
        return lessThan(referenceTime);
    }

    /**
     * @param referenceDate factory for the date the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link DateIsBefore}
     * predicate over the resolved reference date
     * @see DateIsBefore
     */
    public static <S extends Scope> DateIsBeforeFactory<S> dateIsBefore(
        final Factory<? super S, ? extends Supplier<? extends LocalDate>> referenceDate
    ) {
        return new DateIsBeforeFactory<>(referenceDate);
    }

    /**
     * @param referenceDateTime factory for the date-time the resulting predicate tests against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link DateTimeIsBefore} predicate over the resolved reference date-time
     * @see DateTimeIsBefore
     */
    public static <S extends Scope> DateTimeIsBeforeFactory<S> dateTimeIsBefore(
        final Factory<? super S, ? extends Supplier<? extends LocalDateTime>> referenceDateTime
    ) {
        return new DateTimeIsBeforeFactory<>(referenceDateTime);
    }
}

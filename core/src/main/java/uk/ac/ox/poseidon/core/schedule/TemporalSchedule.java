/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.engine.*;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.*;

@Getter
@RequiredArgsConstructor
public class TemporalSchedule extends Schedule {

    public static final String BEFORE_SIMULATION_STRING = "At Start";
    public static final String AFTER_SIMULATION_STRING = "At End";
    private static final System.Logger logger = System.getLogger(TemporalSchedule.class.getName());
    @Serial private static final long serialVersionUID = 4197200009803943439L;

    private final LocalDateTime startingDateTime;

    @SuppressWarnings("WeakerAccess")
    public LocalDateTime getDateTime() {
        return toDateTime(getTime());
    }

    public LocalDate getDate() {
        return getDateTime().toLocalDate();
    }

    public String getTimestamp() {
        return getTimestamp(BEFORE_SIMULATION_STRING, AFTER_SIMULATION_STRING);
    }

    @Override
    public String getTimestamp(
        final double time,
        final String beforeSimulationString,
        final String afterSimulationString
    ) {
        if (time < EPOCH) {
            return beforeSimulationString;
        } else if (time >= Double.POSITIVE_INFINITY) {
            return afterSimulationString;
        } else {
            final LocalDateTime dateTime = toDateTime(time);
            return dateTime.toLocalDate().toString() + ' ' + dateTime.toLocalTime().toString();
        }
    }

    @SuppressWarnings("unused")
    public boolean scheduleOnceIn(
        final Duration delta,
        final Steppable event
    ) {
        return scheduleOnceIn(delta.getSeconds(), event);
    }

    @SuppressWarnings("unused")
    public boolean scheduleOnceIn(
        final Duration delta,
        final Steppable event,
        final int ordering
    ) {
        return scheduleOnceIn(delta.getSeconds(), event, ordering);
    }

    @SuppressWarnings("unused")
    public boolean scheduleOnce(
        final Temporal dateTime,
        final Steppable event
    ) {
        return scheduleOnce(toTime(dateTime), event);
    }

    /**
     * Converts a Java {@link Temporal} (most likely a {@link LocalDateTime}) to internal MASON
     * schedule time by calculating the interval in seconds between that object and the starting
     * date-time of the simulation.
     *
     * @param dateTime the {@link Temporal} object to convert.
     * @return the internal MASON schedule time corresponding to {@code dateTime}.
     */
    @SuppressWarnings("WeakerAccess")
    public double toTime(final Temporal dateTime) {
        return Duration.between(startingDateTime, dateTime).getSeconds();
    }

    @SuppressWarnings("WeakerAccess")
    public LocalDateTime toDateTime(final double time) {
        return startingDateTime.plusSeconds((long) time);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean scheduleOnce(
        final Temporal dateTime,
        final int ordering,
        final Steppable event
    ) {
        return scheduleOnce(toTime(dateTime), ordering, event);
    }

    /**
     * Schedules a collection of steppable entries at specified date-times. Entries with date-times
     * before the current simulation time are immediately scheduled all at once, while others are
     * scheduled at their respective specified date-times.
     *
     * @param steppablesByDateTime a collection of entries mapping {@link LocalDateTime} to
     *                             {@link Steppable}. Each entry specifies a steppable and the
     *                             date-time at which it should be scheduled.
     */
    public void scheduleByDateTime(
        final Collection<? extends Entry<LocalDateTime, ? extends Steppable>> steppablesByDateTime
    ) {
        final LocalDateTime minimumDateTime = time < EPOCH ? toDateTime(EPOCH) : getDateTime();
        final Map<Boolean, List<Entry<LocalDateTime, Sequence>>> entriesBeforeAndAfter =
            steppablesByDateTime
                .stream()
                .collect(groupingBy(
                    Entry::getKey,
                    mapping(Entry::getValue, collectingAndThen(toList(), Sequence::new))
                ))
                .entrySet()
                .stream()
                .collect(groupingBy(
                    entry -> entry.getKey().isBefore(minimumDateTime))
                );

        // Schedule entries that are before the current date-time all at once, right away
        Optional.ofNullable(entriesBeforeAndAfter.get(true))
            .map(entriesBefore ->
                entriesBefore
                    .stream()
                    .sorted(comparingByKey())
                    .map(Entry::getValue)
                    .collect(collectingAndThen(toList(), Sequence::new))
            ).ifPresent(this::scheduleOnce);

        // Schedule other entries at their respective date-times
        Optional.ofNullable(entriesBeforeAndAfter.get(false))
            .ifPresent(entriesAfter ->
                entriesAfter.forEach(entry ->
                    scheduleOnce(entry.getKey(), entry.getValue())
                )
            );
    }

    @SuppressWarnings("unused")
    public TemporalRepeat scheduleRepeating(
        final Steppable event,
        final TemporalAmount interval
    ) {
        return this.scheduleRepeating(getDateTime().plus(interval), 0, event, interval);
    }

    @SuppressWarnings("unused")
    public TemporalRepeat scheduleRepeating(
        final Steppable event,
        final int ordering,
        final TemporalAmount interval
    ) {
        return scheduleRepeating(getDateTime().plus(interval), ordering, event, interval);
    }

    @SuppressWarnings("unused")
    public TemporalRepeat scheduleRepeating(
        final Temporal dateTime,
        final Steppable event,
        final TemporalAmount interval
    ) {
        return this.scheduleRepeating(dateTime, 0, event, interval);
    }

    public TemporalRepeat scheduleRepeating(
        final Temporal dateTime,
        final int ordering,
        final Steppable event,
        final TemporalAmount interval
    ) {
        final TemporalRepeat r = new TemporalRepeat(event, ordering, interval);
        return scheduleOnce(dateTime, ordering, r) ? r : null;
    }

    public void stepFor(
        final SimState simState,
        final TemporalAmount temporalAmount
    ) {
        this.stepUntil(simState, getDateTime().plus(temporalAmount));
    }

    public void stepUntil(
        final SimState simState,
        final LocalDateTime dateTime
    ) {
        final AtomicBoolean done = new AtomicBoolean(false);
        this.scheduleOnce(
            toTime(dateTime),
            Integer.MAX_VALUE,
            (Steppable) sim -> done.set(true)
        );
        while (!done.get()) {
            step(simState);
        }
    }

    @Override
    public String toString() {
        return "TemporalSchedule[" + toDateTime(time) + "]";
    }

    public class TemporalRepeat extends Repeat {

        @Serial private static final long serialVersionUID = -3948718709942805794L;
        private final TemporalAmount interval;

        TemporalRepeat(
            final Steppable event,
            final int ordering,
            final TemporalAmount interval
        ) {
            super(event, ordering);
            this.interval = interval;
        }

        @Override
        protected double getNextTime(
            final SimState simState,
            final double currentTime
        ) {
            return toTime(toDateTime(currentTime).plus(interval));
        }

    }
}

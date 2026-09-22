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

import com.google.common.collect.Streams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.engine.*;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.*;

/**
 * MASON's {@link Schedule}, extended to let callers schedule {@link Steppable}s by
 * {@link LocalDateTime}/{@link Temporal} and {@link TemporalAmount} intervals instead of raw
 * {@code double} simulation time — converting to and from MASON's internal time via the seconds
 * elapsed since {@link #startingDateTime}. Owned by {@link uk.ac.ox.poseidon.core.Simulation}; the
 * {@code schedule*} factories in this package are the usual way scenario-built components get
 * onto it.
 */
@SuppressWarnings("rawtypes")
@Getter
@RequiredArgsConstructor
public class TemporalSchedule extends Schedule {

    /** The timestamp label used for events before the simulation has started. */
    public static final String BEFORE_SIMULATION_STRING = "At Start";
    /** The timestamp label used for events after the simulation has ended. */
    public static final String AFTER_SIMULATION_STRING = "At End";
    /** The schedule ordering used when none is specified. */
    public static final int DEFAULT_ORDERING = 0;
    private static final System.Logger logger = System.getLogger(TemporalSchedule.class.getName());
    @Serial private static final long serialVersionUID = 4197200009803943439L;

    private final LocalDateTime startingDateTime;

    /** @return the current simulation time as a {@link LocalDateTime} */
    @SuppressWarnings("WeakerAccess")
    public LocalDateTime getDateTime() {
        return toDateTime(getTime());
    }

    /** @return the current simulation time as a {@link LocalDate} */
    public LocalDate getDate() {
        return getDateTime().toLocalDate();
    }

    /**
     * @return the current simulation time as a human-readable timestamp, using
     * {@link #BEFORE_SIMULATION_STRING}/{@link #AFTER_SIMULATION_STRING} for the boundary cases
     */
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

    /**
     * @param delta the amount of time from now to schedule the event at
     * @param event the event to schedule
     * @return whether the event was successfully scheduled
     */
    @SuppressWarnings("unused")
    public boolean scheduleOnceIn(
        final Duration delta,
        final Steppable event
    ) {
        return scheduleOnceIn(delta.getSeconds(), event);
    }

    /**
     * @param delta    the amount of time from now to schedule the event at
     * @param event    the event to schedule
     * @param ordering the schedule ordering to use
     * @return whether the event was successfully scheduled
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public boolean scheduleOnceIn(
        final Duration delta,
        final Steppable event,
        final int ordering
    ) {
        return scheduleOnceIn(delta.getSeconds(), event, ordering);
    }

    /**
     * @param dateTime the date-time to schedule the event at
     * @param event    the event to schedule
     * @return whether the event was successfully scheduled
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
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

    /**
     * @param time internal MASON schedule time, as seconds elapsed since {@link #startingDateTime}
     * @return the {@link LocalDateTime} corresponding to {@code time}
     */
    @SuppressWarnings("WeakerAccess")
    public LocalDateTime toDateTime(final double time) {
        return startingDateTime.plusSeconds((long) time);
    }

    /**
     * @param dateTime the date-time to schedule the event at
     * @param ordering the schedule ordering to use
     * @param event    the event to schedule
     * @return whether the event was successfully scheduled
     */
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

    /**
     * @param event    the event to schedule repeatedly
     * @param interval the recurring interval, also used to compute the first firing (now plus
     *                 this interval)
     * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
     */
    @SuppressWarnings("unused")
    public TemporalRepeat scheduleRepeating(
        final Steppable event,
        final TemporalAmount interval
    ) {
        return this.scheduleRepeating(getDateTime().plus(interval), 0, event, interval);
    }

    /**
     * @param event    the event to schedule repeatedly
     * @param ordering the schedule ordering to use
     * @param interval the recurring interval, also used to compute the first firing (now plus
     *                 this interval)
     * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
     */
    @SuppressWarnings("unused")
    public TemporalRepeat scheduleRepeating(
        final Steppable event,
        final int ordering,
        final TemporalAmount interval
    ) {
        return scheduleRepeating(getDateTime().plus(interval), ordering, event, interval);
    }

    /**
     * @param dateTime the date-time of the first firing
     * @param event    the event to schedule repeatedly
     * @param interval the recurring interval
     * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
     */
    @SuppressWarnings("unused")
    public TemporalRepeat scheduleRepeating(
        final Temporal dateTime,
        final Steppable event,
        final TemporalAmount interval
    ) {
        return this.scheduleRepeating(dateTime, 0, event, interval);
    }

    /**
     * @param dateTime the date-time of the first firing
     * @param ordering the schedule ordering to use
     * @param event    the event to schedule repeatedly
     * @param interval the recurring interval
     * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
     */
    public TemporalRepeat scheduleRepeating(
        final Temporal dateTime,
        final int ordering,
        final Steppable event,
        final TemporalAmount interval
    ) {
        final TemporalRepeat r = new TemporalRepeat(event, ordering, interval);
        return scheduleOnce(dateTime, ordering, r) ? r : null;
    }

    /**
     * Runs the simulation forward from the current time by a fixed amount, stepping synchronously
     * until that point is reached.
     *
     * @param simState        the simulation to step
     * @param temporalAmount the amount of time to advance by
     */
    public void stepFor(
        final SimState simState,
        final TemporalAmount temporalAmount
    ) {
        this.stepUntil(simState, getDateTime().plus(temporalAmount));
    }

    /**
     * Runs the simulation forward, stepping synchronously until the given date-time is reached.
     *
     * @param simState the simulation to step
     * @param dateTime the date-time to step until
     */
    public void stepUntil(
        final SimState simState,
        final LocalDateTime dateTime
    ) {
        final AtomicBoolean done = new AtomicBoolean(false);
        this.scheduleOnce(
            toTime(dateTime),
            Integer.MIN_VALUE,
            (Steppable) sim -> done.set(true)
        );
        while (!done.get()) {
            step(simState);
        }
    }

    @Override
    public String toString() {
        return "TemporalSchedule[" + getTimestamp() + "]";
    }

    /** A {@link Repeat} whose next firing time is computed by adding {@link #interval} to the
     * current {@link LocalDateTime}, rather than a fixed {@code double} step. */
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

    /**
     * This is a very ugly method meant to package the schedule in a slightly more easily
     * interpretable way for human _debugging_ and should not under any circumstances be used in
     * actual simulation code. -- NP 2026-02-03.
     */
    private List<?> asMap() {
        record K(
            LocalDateTime dateTime, int ordering, int sequence, Object steppable
        ) {}
        final Comparable[] keys = queue.getKeys();
        final Object[] objects = queue.getObjects();
        return IntStream
            .range(0, this.queue.size())
            .mapToObj(i -> {
                final Schedule.Key key = (Key) keys[i];
                final LocalDateTime dateTime = toDateTime(key.getTime());
                final int ordering = key.getOrdering();
                final Object obj = objects[i];
                return switch (obj) {
                    case final Sequence s -> Streams.mapWithIndex(
                        sequenceSteps(s).stream(),
                        (from, index) -> new K(dateTime, ordering, (int) index, from)
                    );
                    case final Object s -> Stream.of(new K(dateTime, ordering, 0, obj));
                };
            })
            .flatMap(x -> x)
            .sorted((o1, o2) -> Comparator
                .comparing(K::dateTime)
                .thenComparingInt(K::ordering)
                .thenComparingInt(K::sequence)
                .compare(o1, o2)
            )
            .toList();
    }

    private static List<Object> sequenceSteps(final Sequence sequence) {
        try {
            final var field = Sequence.class.getDeclaredField("steps");
            field.setAccessible(true);
            final Object raw = field.get(sequence);
            if (raw instanceof final Object[] array) {
                return Arrays.stream(array).toList();
            }
            return List.of(raw);
        } catch (final ReflectiveOperationException | SecurityException e) {
            return List.of(sequence);
        }
    }

}

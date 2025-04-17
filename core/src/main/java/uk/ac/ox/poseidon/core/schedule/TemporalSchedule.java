/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.engine.Repeat;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@RequiredArgsConstructor
public class TemporalSchedule extends Schedule {

    public static final String BEFORE_SIMULATION_STRING = "At Start";
    public static final String AFTER_SIMULATION_STRING = "At End";
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
        if (time < 0.0) {
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
            dateTime,
            Integer.MIN_VALUE,
            (Steppable) __ -> done.set(true)
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

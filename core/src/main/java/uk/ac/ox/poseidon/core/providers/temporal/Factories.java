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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Supplier;

/**
 * Factories for {@link uk.ac.ox.poseidon.core.providers.Provider}s that read the simulation's
 * current or upcoming date/time from its {@link uk.ac.ox.poseidon.core.schedule.TemporalSchedule}.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentDateProvider}
     * @see CurrentDateProvider
     */
    public static CurrentDateProviderFactory currentDate() {
        return new CurrentDateProviderFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentYearProvider}
     * @see CurrentYearProvider
     */
    public static CurrentYearProviderFactory currentYear() {
        return new CurrentYearProviderFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentDateTimeProvider}
     * @see CurrentDateTimeProvider
     */
    public static CurrentDateTimeProviderFactory currentDateTime() {
        return new CurrentDateTimeProviderFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentDayOfWeekProvider}
     * @see CurrentDayOfWeekProvider
     */
    public static CurrentDayOfWeekProviderFactory currentDayOfWeek() {
        return new CurrentDayOfWeekProviderFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentTimeProvider}
     * @see CurrentTimeProvider
     */
    public static CurrentTimeProviderFactory currentTime() {
        return new CurrentTimeProviderFactory();
    }

    /**
     * @param time the time of day the resulting provider will resolve to on the following day
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link NextDayAtTimeProvider}
     * @see NextDayAtTimeProvider
     */
    public static NextDayAtTimeProviderFactory nextDayAtTime(
        final Factory<? super SimulationScope, ? extends LocalTime> time
    ) {
        return new NextDayAtTimeProviderFactory(time);
    }

    /**
     * @param times the candidate times of day the resulting provider picks the next of
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link NextTimeAtProvider}
     * @see NextTimeAtProvider
     */
    @SafeVarargs
    public static NextTimeAtProviderFactory nextTimeAt(
        final Factory<? super SimulationScope, ? extends LocalTime>... times
    ) {
        return new NextTimeAtProviderFactory(List.of(times));
    }

    /**
     * @param referenceDateTime factory for the date-time the resulting provider computes the
     *                          duration until; must resolve to a value after the current time
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link DurationUntilProvider}
     * @see DurationUntilProvider
     */
    public static DurationUntilProviderFactory durationUntil(
        final Factory<? super SimulationScope, ? extends Supplier<LocalDateTime>> referenceDateTime
    ) {
        return new DurationUntilProviderFactory(referenceDateTime);
    }
}

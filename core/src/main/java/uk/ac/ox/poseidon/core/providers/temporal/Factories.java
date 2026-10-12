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
     * a {@link CurrentDate}
     * @see CurrentDate
     */
    public static CurrentDateFactory currentDate() {
        return new CurrentDateFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentYear}
     * @see CurrentYear
     */
    public static CurrentYearFactory currentYear() {
        return new CurrentYearFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentDateTime}
     * @see CurrentDateTime
     */
    public static CurrentDateTimeFactory currentDateTime() {
        return new CurrentDateTimeFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentDayOfWeek}
     * @see CurrentDayOfWeek
     */
    public static CurrentDayOfWeekFactory currentDayOfWeek() {
        return new CurrentDayOfWeekFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for
     * a {@link CurrentTime}
     * @see CurrentTime
     */
    public static CurrentTimeFactory currentTime() {
        return new CurrentTimeFactory();
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
     * a {@link DurationUntil}
     * @see DurationUntil
     */
    public static DurationUntilFactory durationUntil(
        final Factory<? super SimulationScope, ? extends Supplier<LocalDateTime>> referenceDateTime
    ) {
        return new DurationUntilFactory(referenceDateTime);
    }
}

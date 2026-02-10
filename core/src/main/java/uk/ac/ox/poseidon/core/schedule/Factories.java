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

package uk.ac.ox.poseidon.core.schedule;

import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

import static uk.ac.ox.poseidon.core.schedule.TemporalSchedule.DEFAULT_ORDERING;

public class Factories {

    private Factories() {
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledOnce(
        final Factory<? super SimulationScope, ? extends Temporal> dateTime,
        final Factory<? super SimulationScope, ? extends C> steppable
    ) {
        return scheduledOnce(dateTime, steppable, DEFAULT_ORDERING);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledOnce(
        final Factory<? super SimulationScope, ? extends Temporal> dateTime,
        final Factory<? super SimulationScope, ? extends C> steppable,
        final int ordering
    ) {
        return new ScheduledOnceFactory<>(dateTime, steppable, ordering);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledOnceAtStart(
        final Factory<? super SimulationScope, ? extends C> steppable
    ) {
        return scheduledOnceAtStart(steppable, DEFAULT_ORDERING);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledOnceAtStart(
        final Factory<? super SimulationScope, ? extends C> steppable,
        final int ordering
    ) {
        return new ScheduledOnceAtStartFactory<>(steppable, ordering);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledRepeating(
        final Factory<? super SimulationScope, ? extends Temporal> dateTime,
        final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
        final Factory<? super SimulationScope, ? extends C> steppable
    ) {
        return scheduledRepeating(dateTime, interval, steppable, DEFAULT_ORDERING);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledRepeating(
        final Factory<? super SimulationScope, ? extends Temporal> dateTime,
        final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
        final Factory<? super SimulationScope, ? extends C> steppable,
        final int ordering
    ) {
        return new ScheduledRepeatingFactory<>(dateTime, interval, steppable, ordering);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledRepeatingFromStart(
        final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
        final Factory<? super SimulationScope, ? extends C> steppable
    ) {
        return scheduledRepeatingFromStart(interval, steppable, DEFAULT_ORDERING);
    }

    public static <C extends Steppable> SimulationScopeFactory<C> scheduledRepeatingFromStart(
        final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
        final Factory<? super SimulationScope, ? extends C> steppable,
        final int ordering
    ) {
        return new ScheduledRepeatingFromStartFactory<>(interval, steppable, ordering);
    }

}

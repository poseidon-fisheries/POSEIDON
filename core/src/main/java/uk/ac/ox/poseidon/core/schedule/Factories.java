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

import static uk.ac.ox.poseidon.core.schedule.TemporalSchedule.DEFAULT_ORDERING;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map.Entry;
import sim.engine.RandomSequence;
import sim.engine.Sequence;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SchedulingFactory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

/**
 * Factories for {@link Steppable}s that schedule themselves onto the
 * simulation's {@link TemporalSchedule} when built, and for MASON step-grouping
 * constructs (e.g. {@link Sequence}) over a resolved list of steppables.
 */
@SuppressWarnings("varargs")
public final class Factories {

  private Factories() {}

  /**
   * @param steppablesByDateTime factory for the steppables to schedule, each
   *                             with its own date-time
   * @return a {@link SchedulingFactory} for the resolved dated steppables,
   *         each scheduled once at its own date-time
   * @see ScheduledByDateTimeFactory
   */
  public static <P extends Steppable> ScheduledByDateTimeFactory<P> scheduledByDateTime(
    final Factory<
      ? super SimulationScope,
      ? extends List<Entry<LocalDateTime, P>>
    > steppablesByDateTime
  ) {
    return new ScheduledByDateTimeFactory<>(steppablesByDateTime);
  }

  /**
   * @param dateTime  factory for the date-time to schedule the steppable at
   * @param steppable factory for the steppable to schedule
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled once
   * @see ScheduledOnceFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledOnce(
    final Factory<? super SimulationScope, ? extends Temporal> dateTime,
    final Factory<? super SimulationScope, ? extends P> steppable
  ) {
    return scheduledOnce(dateTime, steppable, DEFAULT_ORDERING);
  }

  /**
   * @param dateTime  factory for the date-time to schedule the steppable at
   * @param steppable factory for the steppable to schedule
   * @param ordering  the schedule ordering to use
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled once
   * @see ScheduledOnceFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledOnce(
    final Factory<? super SimulationScope, ? extends Temporal> dateTime,
    final Factory<? super SimulationScope, ? extends P> steppable,
    final int ordering
  ) {
    return new ScheduledOnceFactory<>(dateTime, steppable, ordering);
  }

  /**
   * @param steppable factory for the steppable to schedule
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled once at the start of the simulation
   * @see ScheduledOnceAtStartFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledOnceAtStart(
    final Factory<? super SimulationScope, ? extends P> steppable
  ) {
    return scheduledOnceAtStart(steppable, DEFAULT_ORDERING);
  }

  /**
   * @param steppable factory for the steppable to schedule
   * @param ordering  the schedule ordering to use
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled once at the start of the simulation
   * @see ScheduledOnceAtStartFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledOnceAtStart(
    final Factory<? super SimulationScope, ? extends P> steppable,
    final int ordering
  ) {
    return new ScheduledOnceAtStartFactory<>(steppable, ordering);
  }

  /**
   * @param dateTime  factory for the date-time to start scheduling at
   * @param interval  factory for the recurring interval
   * @param steppable factory for the steppable to schedule
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled repeatedly
   * @see ScheduledRepeatingFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledRepeating(
    final Factory<? super SimulationScope, ? extends Temporal> dateTime,
    final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
    final Factory<? super SimulationScope, ? extends P> steppable
  ) {
    return scheduledRepeating(dateTime, interval, steppable, DEFAULT_ORDERING);
  }

  /**
   * @param dateTime  factory for the date-time to start scheduling at
   * @param interval  factory for the recurring interval
   * @param steppable factory for the steppable to schedule
   * @param ordering  the schedule ordering to use
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled repeatedly
   * @see ScheduledRepeatingFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledRepeating(
    final Factory<? super SimulationScope, ? extends Temporal> dateTime,
    final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
    final Factory<? super SimulationScope, ? extends P> steppable,
    final int ordering
  ) {
    return new ScheduledRepeatingFactory<>(
      dateTime,
      interval,
      steppable,
      ordering
    );
  }

  /**
   * @param interval  factory for the recurring interval
   * @param steppable factory for the steppable to schedule
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled repeatedly starting at the simulation's starting
   *         date-time
   * @see ScheduledRepeatingFromStartFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledRepeatingFromStart(
    final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
    final Factory<? super SimulationScope, ? extends P> steppable
  ) {
    return scheduledRepeatingFromStart(interval, steppable, DEFAULT_ORDERING);
  }

  /**
   * @param interval  factory for the recurring interval
   * @param steppable factory for the steppable to schedule
   * @param ordering  the schedule ordering to use
   * @return a {@link SchedulingFactory} for the resolved steppable,
   *         scheduled repeatedly starting at the simulation's starting
   *         date-time
   * @see ScheduledRepeatingFromStartFactory
   */
  public static <P extends Steppable> SchedulingFactory<P> scheduledRepeatingFromStart(
    final Factory<? super SimulationScope, ? extends TemporalAmount> interval,
    final Factory<? super SimulationScope, ? extends P> steppable,
    final int ordering
  ) {
    return new ScheduledRepeatingFromStartFactory<>(
      interval,
      steppable,
      ordering
    );
  }

  /**
   * @param steppables factories for the steppables to run in order
   * @return a {@link SimulationScopeFactory} for a {@link Sequence} over the
   *         resolved steppables
   * @see SteppableSequenceFactory
   */
  @SafeVarargs
  public static SteppableSequenceFactory steppableSequence(
    final Factory<? super SimulationScope, ? extends Steppable>... steppables
  ) {
    return new SteppableSequenceFactory(ImmutableList.copyOf(steppables));
  }

  /**
   * @param steppables factories for the steppables to run in random order
   * @return a {@link SimulationScopeFactory} for a {@link RandomSequence} over
   *         the resolved steppables
   * @see SteppableRandomSequenceFactory
   */
  @SafeVarargs
  public static SteppableRandomSequenceFactory steppableRandomSequence(
    final Factory<? super SimulationScope, ? extends Steppable>... steppables
  ) {
    return new SteppableRandomSequenceFactory(ImmutableList.copyOf(steppables));
  }
}

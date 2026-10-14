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

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.*;

import com.google.common.collect.Streams;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serial;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sim.engine.*;

/**
 * MASON's {@link Schedule}, extended to let callers schedule {@link Steppable}s by
 * {@link LocalDateTime}/{@link Temporal} and {@link TemporalAmount} intervals instead of raw
 * {@code double} simulation time — converting to and from MASON's internal time via the seconds
 * elapsed since {@link #startingDateTime}. Owned by {@link uk.ac.ox.poseidon.core.Simulation}; the
 * {@code schedule*} factories in this package are the usual way scenario-built components get
 * onto it.
 * <p>
 * The schedule follows seven rules:
 * <ol>
 *     <li><b>The clock reads how far the schedule has run</b> ({@link #getTime()},
 *     {@link #getDateTime()}): during an event, that event's time; between calls, where the last
 *     {@link #stepUntil} stopped; before the first step, the start.</li>
 *     <li><b>{@link #stepUntil stepUntil(t)} runs every event dated before {@code t}</b>; events
 *     dated {@code t} run in the next call. {@link #stepFor stepFor(d)} is
 *     {@code stepUntil(clock + d)}.</li>
 *     <li><b>Nothing can be scheduled before the clock</b>: trying throws
 *     {@link IllegalArgumentException}. Relative times count from the clock, for MASON's relative
 *     methods ({@code scheduleOnce(Steppable)}'s "+1", {@code scheduleOnceIn},
 *     {@code scheduleRepeating(Steppable, ...)}) as for this class's.</li>
 *     <li><b>An event scheduled for the current time runs after the event that scheduled it, at
 *     the same time</b>, as in MASON. Between calls, an event scheduled at the clock joins the
 *     events already queued there, ordered with them by its ordering.</li>
 *     <li><b>Date-times are whole seconds</b>: conversions between date-times and MASON time
 *     floor fractions of a second, and a date-time in the clock's current second means now.
 *     (MASON places an event scheduled for the current time a fraction of a second after it, so
 *     the clock can be just past the second {@link #getDateTime()} shows.)</li>
 *     <li><b>One advance at a time</b>: {@link #stepUntil}, {@link #stepFor} and {@link #step}
 *     are synchronised on the schedule, and advancing from inside an event throws
 *     {@link IllegalStateException}.</li>
 *     <li><b>A finished schedule cannot be advanced</b>: {@link #stepUntil} and {@link #stepFor}
 *     throw {@link IllegalStateException}, also when the schedule is killed during the advance,
 *     while {@link #step} returns {@code false}, as in MASON. Either way, the clock stays where the
 *     last step left it.</li>
 * </ol>
 * Events with the same time and ordering run in random order (MASON's shuffling). Callers needing
 * an order use distinct orderings or a {@link Sequence}.
 * <p>
 * MASON's own time, the time of the last event that ran, stays internal: it can be earlier than
 * the clock between calls. Code in this class reads the clock through {@link #getTime()}, never
 * through MASON's {@code time} field. {@link #merge} is not supported.
 */
@SuppressWarnings("rawtypes")
@SuppressFBWarnings(
  value = "USO_UNSAFE_OBJECT_SYNCHRONIZATION",
  justification = "Synchronises on MASON's own Schedule.lock on purpose: MASON's scheduling " +
    "methods take the same lock, which keeps the clock and the queue consistent with them."
)
@Getter
@RequiredArgsConstructor
public class TemporalSchedule extends Schedule {

  /** The timestamp label used for times before the starting date-time. */
  public static final String BEFORE_SIMULATION_STRING = "At Start";
  /** The timestamp label used for events after the simulation has ended. */
  public static final String AFTER_SIMULATION_STRING = "At End";
  /** The schedule ordering used when none is specified. */
  public static final int DEFAULT_ORDERING = 0;
  private static final System.Logger logger = System.getLogger(
    TemporalSchedule.class.getName()
  );

  @Serial
  private static final long serialVersionUID = 4197200009803943439L;

  private final LocalDateTime startingDateTime;
  // The last time converted by toDateTime, which is called with the same time many times in a
  // step: converting it is costly, its result never changes.
  private transient Conversion lastConversion;

  // Where the last stepUntil stopped: the clock between calls. Guarded by lock.
  @Getter(AccessLevel.NONE)
  private double boundary = EPOCH;

  // Whether step() is running events, to refuse advancing from inside one. Guarded by this.
  @Getter(AccessLevel.NONE)
  private transient boolean running;

  /**
   * @return the clock, in seconds since {@link #startingDateTime}: the time of the event being
   * run; otherwise where the last {@link #stepUntil} stopped, or 0 before the first step; or
   * {@link #AFTER_SIMULATION} once MASON has run out of events
   */
  @Override
  public double getTime() {
    synchronized (lock) {
      return Math.max(time, boundary);
    }
  }

  /**
   * @return the clock as a {@link LocalDateTime}
   * @throws IllegalStateException once MASON has run out of events, as the clock then has no
   *                               date-time
   */
  @SuppressWarnings("WeakerAccess")
  public LocalDateTime getDateTime() {
    final double clock = getTime();
    if (!Double.isFinite(clock)) {
      throw new IllegalStateException(
        "The schedule has run out of events: no date-time."
      );
    }
    return toDateTime(clock);
  }

  /** @return the clock as a {@link LocalDate} */
  public LocalDate getDate() {
    return getDateTime().toLocalDate();
  }

  /**
   * @return the clock as a human-readable timestamp, or {@link #AFTER_SIMULATION_STRING} once
   * MASON has run out of events
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
      return (
        dateTime.toLocalDate().toString() +
        ' ' +
        dateTime.toLocalTime().toString()
      );
    }
  }

  /**
   * @param delta the amount of time from the clock to schedule the event at, floored to whole
   *              seconds
   * @param event the event to schedule
   * @return whether the event was successfully scheduled
   */
  @SuppressWarnings("unused")
  public boolean scheduleOnceIn(final Duration delta, final Steppable event) {
    return scheduleOnceIn(delta.getSeconds(), event);
  }

  /**
   * @param delta    the amount of time from the clock to schedule the event at, floored to
   *                 whole seconds
   * @param event    the event to schedule
   * @param ordering the schedule ordering to use
   * @return whether the event was successfully scheduled
   */
  @SuppressWarnings({ "unused", "UnusedReturnValue" })
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
  @SuppressWarnings({ "unused", "UnusedReturnValue" })
  public boolean scheduleOnce(final Temporal dateTime, final Steppable event) {
    synchronized (lock) {
      return scheduleOnce(schedulingTime(dateTime), event);
    }
  }

  /**
   * Converts a Java {@link Temporal} (most likely a {@link LocalDateTime}) to internal MASON
   * schedule time by calculating the interval in seconds between that object and the starting
   * date-time of the simulation, flooring fractions of a second.
   *
   * @param dateTime the {@link Temporal} object to convert.
   * @return the internal MASON schedule time corresponding to {@code dateTime}.
   */
  @SuppressWarnings("WeakerAccess")
  public double toTime(final Temporal dateTime) {
    return Duration.between(startingDateTime, dateTime).getSeconds();
  }

  /**
   * @return the MASON time to schedule or step to for {@code dateTime}: its whole second, from
   * {@link #toTime}, or the clock itself if that second is the clock's current second, since a
   * date-time in the current second means now
   */
  private double schedulingTime(final Temporal dateTime) {
    synchronized (lock) {
      final double time = toTime(dateTime);
      final double clock = getTime();
      return time < clock && time == Math.floor(clock) ? clock : time;
    }
  }

  /**
   * @param time internal MASON schedule time, as seconds elapsed since {@link #startingDateTime}
   * @return the {@link LocalDateTime} corresponding to {@code time}, with fractions of a second
   * floored
   */
  @SuppressWarnings("WeakerAccess")
  public LocalDateTime toDateTime(final double time) {
    final long seconds = (long) Math.floor(time);
    final Conversion conversion = lastConversion;
    if (
      conversion != null && conversion.seconds() == seconds
    ) return conversion.dateTime();
    final LocalDateTime dateTime = startingDateTime.plusSeconds(seconds);
    lastConversion = new Conversion(seconds, dateTime);
    return dateTime;
  }

  private record Conversion(long seconds, LocalDateTime dateTime) {}

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
    synchronized (lock) {
      return scheduleOnce(schedulingTime(dateTime), ordering, event);
    }
  }

  /**
   * Schedules a collection of steppable entries at specified date-times. Entries dated before the
   * clock are scheduled together at the clock, in date order, while others are scheduled at their
   * respective specified date-times.
   *
   * @param steppablesByDateTime a collection of entries mapping {@link LocalDateTime} to
   *                             {@link Steppable}. Each entry specifies a steppable and the
   *                             date-time at which it should be scheduled.
   */
  public void scheduleByDateTime(
    final Collection<
      ? extends Entry<LocalDateTime, ? extends Steppable>
    > steppablesByDateTime
  ) {
    final LocalDateTime clock = getDateTime();
    final Map<
      Boolean,
      List<Entry<LocalDateTime, Sequence>>
    > entriesBeforeAndAfter = steppablesByDateTime
      .stream()
      .collect(
        groupingBy(
          Entry::getKey,
          mapping(Entry::getValue, collectingAndThen(toList(), Sequence::new))
        )
      )
      .entrySet()
      .stream()
      .collect(groupingBy(entry -> entry.getKey().isBefore(clock)));

    // Schedule entries that are before the clock all at once, at the clock
    Optional.ofNullable(entriesBeforeAndAfter.get(true))
      .map(entriesBefore ->
        entriesBefore
          .stream()
          .sorted(comparingByKey())
          .map(Entry::getValue)
          .collect(collectingAndThen(toList(), Sequence::new))
      )
      .ifPresent(entries -> scheduleOnce(getTime(), DEFAULT_ORDERING, entries));

    // Schedule other entries at their respective date-times
    Optional.ofNullable(entriesBeforeAndAfter.get(false)).ifPresent(
      entriesAfter ->
        entriesAfter.forEach(entry ->
          scheduleOnce(entry.getKey(), entry.getValue())
        )
    );
  }

  /**
   * @param event    the event to schedule repeatedly
   * @param interval the recurring interval, also used to compute the first firing (the clock
   *                 plus this interval)
   * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
   */
  @SuppressWarnings("unused")
  public TemporalRepeat scheduleRepeating(
    final Steppable event,
    final TemporalAmount interval
  ) {
    return this.scheduleRepeating(
      getDateTime().plus(interval),
      0,
      event,
      interval
    );
  }

  /**
   * @param event    the event to schedule repeatedly
   * @param ordering the schedule ordering to use
   * @param interval the recurring interval, also used to compute the first firing (the clock
   *                 plus this interval)
   * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
   */
  @SuppressWarnings("unused")
  public TemporalRepeat scheduleRepeating(
    final Steppable event,
    final int ordering,
    final TemporalAmount interval
  ) {
    return scheduleRepeating(
      getDateTime().plus(interval),
      ordering,
      event,
      interval
    );
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
   * @param interval the recurring interval, a {@link Period} or a {@link Duration}
   * @return the {@link TemporalRepeat} handle, or {@code null} if scheduling failed
   * @throws IllegalArgumentException if {@code interval} is neither a {@link Period} nor a
   *                                  {@link Duration}, or does not move time forward
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
   * Steps until the clock plus {@code temporalAmount}: {@code stepUntil(clock + temporalAmount)},
   * with the clock read and the advance made as one operation.
   *
   * @param simState       the simulation to step
   * @param temporalAmount the amount of time to advance by
   * @throws IllegalStateException as {@link #stepUntil}, or if MASON has run out of events
   */
  public synchronized void stepFor(
    final SimState simState,
    final TemporalAmount temporalAmount
  ) {
    this.stepUntil(simState, getDateTime().plus(temporalAmount));
  }

  /**
   * Runs every event dated before {@code dateTime}, in time and ordering order, then leaves the
   * clock at {@code dateTime}: events dated {@code dateTime} run in the next call. If
   * {@code dateTime} is in the clock's current second, nothing runs.
   *
   * @param simState the simulation to step
   * @param dateTime the date-time to step until, excluded
   * @throws IllegalArgumentException if {@code dateTime} is before the clock
   * @throws IllegalStateException    if the schedule is finished or is killed during the advance,
   *                                  or if called from inside an event
   */
  public synchronized void stepUntil(
    final SimState simState,
    final LocalDateTime dateTime
  ) {
    checkNotRunningEvents();
    final double target;
    synchronized (lock) {
      target = schedulingTime(dateTime);
      if (target < getTime()) {
        throw new IllegalArgumentException(
          "Cannot step until " +
            dateTime +
            ", before the clock (" +
            getTimestamp() +
            ")."
        );
      }
    }
    while (true) {
      // The last look at the queue and the boundary update are one locked block, so that no
      // event can be queued before the target in between.
      synchronized (lock) {
        if (sealed) {
          throw new IllegalStateException("The schedule is finished.");
        }
        if (queue.isEmpty() || ((Key) queue.getMinKey()).getTime() >= target) {
          boundary = target;
          return;
        }
      }
      checkpoint();
      step(simState);
    }
  }

  /**
   * Does nothing. Called by {@link #stepUntil} between looking at the queue and stepping, so that
   * tests can pause an advance there.
   */
  void checkpoint() {}

  /**
   * Runs the events at the earliest queued time, as MASON's {@link Schedule#step} does.
   *
   * @return {@code false}, without moving the clock, if the schedule is finished; otherwise as
   * MASON's {@link Schedule#step}
   * @throws IllegalStateException if called from inside an event
   */
  @Override
  public synchronized boolean step(final SimState simState) {
    checkNotRunningEvents();
    synchronized (lock) {
      if (sealed) {
        return false;
      }
    }
    running = true;
    try {
      return super.step(simState);
    } finally {
      running = false;
    }
  }

  private void checkNotRunningEvents() {
    if (running) {
      throw new IllegalStateException(
        "Cannot advance the schedule from inside an event."
      );
    }
  }

  /**
   * Refuses a time before the clock: MASON only refuses times before its own time, the time of
   * the last event that ran, which can be earlier than the clock between calls.
   */
  @Override
  protected boolean _scheduleOnce(final Key key, final Steppable event) {
    synchronized (lock) {
      if (key.getTime() < boundary) {
        throw new IllegalArgumentException(
          "Cannot schedule an event at time " +
            key.getTime() +
            ", before the clock (" +
            boundary +
            ")."
        );
      }
      return super._scheduleOnce(key, event);
    }
  }

  // MASON's relative scheduling methods count from MASON's own time, which can be earlier than
  // the clock between calls: these count from the clock.

  @Override
  public boolean scheduleOnce(final Steppable event) {
    return scheduleOnceIn(1.0, event, DEFAULT_ORDERING);
  }

  @Override
  public boolean scheduleOnce(final Steppable event, final int ordering) {
    return scheduleOnceIn(1.0, event, ordering);
  }

  @Override
  public boolean scheduleOnceIn(final double delta, final Steppable event) {
    return scheduleOnceIn(delta, event, DEFAULT_ORDERING);
  }

  @Override
  public boolean scheduleOnceIn(
    final double delta,
    final Steppable event,
    final int ordering
  ) {
    synchronized (lock) {
      return scheduleOnce(getTime() + delta, ordering, event);
    }
  }

  @Override
  public IterativeRepeat scheduleRepeating(final Steppable event) {
    return scheduleRepeating(event, DEFAULT_ORDERING, 1.0);
  }

  @Override
  public IterativeRepeat scheduleRepeating(
    final Steppable event,
    final double interval
  ) {
    return scheduleRepeating(event, DEFAULT_ORDERING, interval);
  }

  @Override
  public IterativeRepeat scheduleRepeating(
    final Steppable event,
    final int ordering,
    final double interval
  ) {
    synchronized (lock) {
      return scheduleRepeating(getTime() + interval, ordering, event, interval);
    }
  }

  /** Also puts the clock back to the start. */
  @Override
  public void reset() {
    synchronized (lock) {
      super.reset();
      boundary = EPOCH;
    }
  }

  /**
   * Not supported: MASON's merge also refuses events at the clock, which this schedule's rules
   * allow.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public void merge(final Schedule other) {
    throw new UnsupportedOperationException(
      "TemporalSchedule does not support merging."
    );
  }

  @Override
  public String toString() {
    return "TemporalSchedule[" + getTimestamp() + "]";
  }

  /**
   * Refuses a repeat interval that does not move time forward: zero, which would fire forever
   * within the same second, or with a negative part, whose firings would be refused as past.
   */
  private static void checkMovesTimeForward(final TemporalAmount interval) {
    final List<TemporalUnit> units = interval.getUnits();
    if (
      !units.stream().allMatch(unit -> interval.get(unit) >= 0) ||
      units.stream().noneMatch(unit -> interval.get(unit) > 0)
    ) {
      throw new IllegalArgumentException(
        "A repeat's interval must move time forward, not " + interval + "."
      );
    }
  }

  /**
   * A {@link Repeat} anchored to its first firing: its n-th firing after the first is the first
   * plus n times {@link #interval}, floored to the second like any date-time, never the previous
   * firing plus the interval. So neither calendar clamping nor flooring accumulates. Monthly from
   * 31 January, it fires on 31 January, 28 February, 31 March, 30 April, and so on; every 1.5
   * seconds from 0, it fires at 0, 1, 3, 4, 6 seconds, and so on.
   */
  public class TemporalRepeat extends Repeat {

    @Serial
    private static final long serialVersionUID = -3948718709942805794L;

    private final TemporalAmount interval;
    // The anchor of the later firings, set at the first firing.
    private LocalDateTime firstFiring;
    // The number of firings computed from the anchor so far.
    private long firingsAfterTheFirst;

    /**
     * @throws IllegalArgumentException if {@code interval} is neither a {@link Period} nor a
     *                                  {@link Duration}, the amounts that can be multiplied, or
     *                                  does not move time forward
     */
    TemporalRepeat(
      final Steppable event,
      final int ordering,
      final TemporalAmount interval
    ) {
      super(event, ordering);
      if (!(interval instanceof Period) && !(interval instanceof Duration)) {
        throw new IllegalArgumentException(
          "A repeat's interval must be a Period or a Duration, not " +
            interval +
            "."
        );
      }
      checkMovesTimeForward(interval);
      this.interval = interval;
    }

    @Override
    protected double getNextTime(
      final SimState simState,
      final double currentTime
    ) {
      if (firstFiring == null) {
        firstFiring = toDateTime(currentTime);
      }
      firingsAfterTheFirst++;
      return schedulingTime(
        firstFiring.plus(intervalTimes(firingsAfterTheFirst))
      );
    }

    private TemporalAmount intervalTimes(final long n) {
      return switch (interval) {
        case final Period period -> period.multipliedBy(Math.toIntExact(n));
        case final Duration duration -> duration.multipliedBy(n);
        default -> throw new IllegalStateException(
          "Unexpected interval: " + interval
        );
      };
    }
  }

  /**
   * This is a very ugly method meant to package the schedule in a slightly more easily
   * interpretable way for human _debugging_ and should not under any circumstances be used in
   * actual simulation code. -- NP 2026-02-03.
   */
  private List<?> asMap() {
    record K(
      LocalDateTime dateTime,
      int ordering,
      int sequence,
      Object steppable
    ) {}
    final Comparable[] keys = queue.getKeys();
    final Object[] objects = queue.getObjects();
    return IntStream.range(0, this.queue.size())
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
      .sorted((o1, o2) ->
        Comparator.comparing(K::dateTime)
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

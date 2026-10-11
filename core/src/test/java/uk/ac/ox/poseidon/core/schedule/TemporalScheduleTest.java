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

import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.chrono.MinguoChronology;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Map.entry;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemporalScheduleTest {

    private final LocalDateTime start = LocalDateTime.of(2013, 1, 1, 0, 0);
    private final TemporalSchedule schedule = new TemporalSchedule(start);
    private final SimState simState = new SimState(0, schedule) {};
    private final List<LocalDateTime> firings = new ArrayList<>();
    private final Steppable recordFiring = _ -> firings.add(schedule.getDateTime());
    private final List<String> runs = new ArrayList<>();

    private static LocalDateTime dateTime(
        final int year,
        final int month,
        final int day
    ) {
        return LocalDate.of(year, month, day).atStartOfDay();
    }

    // --- Conversions ---

    @Test
    void givesTheDateTimeOfEachTimeConverted() {
        assertThat(schedule.toDateTime(3600)).isEqualTo(start.plusHours(1));
        assertThat(schedule.toDateTime(7200)).isEqualTo(start.plusHours(2));
        assertThat(schedule.toDateTime(3600)).isEqualTo(start.plusHours(1));
    }

    @Test
    void convertsDateTimesToSecondsSinceTheStart() {
        assertThat(schedule.toTime(start)).isEqualTo(0.0);
        assertThat(schedule.toTime(start.plusDays(1))).isEqualTo(86_400.0);
        assertThat(schedule.toTime(start.minusHours(1))).isEqualTo(-3_600.0);
    }

    @Test
    void convertsDateTimesToTimesAndBack() {
        final LocalDateTime dateTime = LocalDateTime.of(2024, 2, 29, 13, 45, 10);
        assertThat(schedule.toDateTime(schedule.toTime(dateTime))).isEqualTo(dateTime);
    }

    @Test
    void dropsFractionsOfASecond() {
        assertThat(schedule.toDateTime(3_600.9)).isEqualTo(start.plusHours(1));
        assertThat(schedule.toTime(start.plusSeconds(1).plusNanos(500_000_000))).isEqualTo(1.0);
    }

    @Test
    void floorsFractionsOfASecondBeforeTheStartInBothDirections() {
        assertThat(schedule.toDateTime(-0.5)).isEqualTo(start.minusSeconds(1));
        assertThat(schedule.toTime(start.minusNanos(500_000_000))).isEqualTo(-1.0);
    }

    // --- The clock ---

    @Test
    void givesTheStartingDateTimeBeforeTheFirstStep() {
        assertThat(schedule.getDateTime()).isEqualTo(start);
        assertThat(schedule.getTime()).isEqualTo(0.0);
    }

    @Test
    void givesTheDateTimeAndDateOfTheEventBeingRun() {
        final LocalDateTime dateTime = LocalDateTime.of(2013, 3, 15, 6, 30);
        schedule.scheduleOnce(dateTime, recordFiring);
        schedule.step(simState);
        assertThat(firings).containsExactly(dateTime);
        assertThat(schedule.getDate()).isEqualTo(dateTime.toLocalDate());
    }

    @Test
    void readsWhereTheLastStepUntilStoppedThroughMasonsRawTimeAndTheTimestamp() {
        schedule.scheduleOnce(start.plusHours(1), _ -> {});
        schedule.stepUntil(simState, start.plusDays(1));
        assertThat(schedule.getTime()).isEqualTo(86_400.0);
        assertThat(schedule.time()).isEqualTo(86_400.0);
        assertThat(schedule.getTimestamp()).isEqualTo("2013-01-02 00:00");
    }

    @Test
    void labelsTimestampsBeforeAndAfterTheSimulation() {
        assertThat(schedule.getTimestamp()).isEqualTo("2013-01-01 00:00");
        assertThat(schedule.getTimestamp(-1, "before", "after")).isEqualTo("before");
        assertThat(schedule.getTimestamp(Schedule.AFTER_SIMULATION, "before", "after"))
            .isEqualTo("after");
        assertThat(schedule.getTimestamp(3_600, "before", "after"))
            .isEqualTo("2013-01-01 01:00");
    }

    @Test
    void hasNoDateTimeOnceMasonHasRunOutOfEvents() {
        schedule.step(simState);
        assertThat(schedule.getTime()).isEqualTo(Schedule.AFTER_SIMULATION);
        assertThat(schedule.getTimestamp()).isEqualTo(TemporalSchedule.AFTER_SIMULATION_STRING);
        assertThatThrownBy(schedule::getDateTime).isInstanceOf(IllegalStateException.class);
    }

    // --- Stepping ---

    @Test
    void stepsUntilADateTimeWithoutRunningTheEventsAtThatDateTime() {
        final LocalDateTime until = start.plusDays(1);
        schedule.scheduleOnce(until.minusSeconds(1), recordFiring);
        schedule.scheduleOnce(until, recordFiring);
        schedule.stepUntil(simState, until);
        assertThat(firings).containsExactly(until.minusSeconds(1));
        assertThat(schedule.getDateTime()).isEqualTo(until);
        schedule.stepUntil(simState, until.plusDays(1));
        assertThat(firings).containsExactly(until.minusSeconds(1), until);
    }

    @Test
    void stepsForAnAmountOfTimeFromTheCurrentDateTime() {
        schedule.stepUntil(simState, start.plusDays(10));
        schedule.stepFor(simState, Period.ofMonths(1));
        assertThat(schedule.getDateTime()).isEqualTo(start.plusDays(10).plusMonths(1));
    }

    @Test
    void stepsForAnAmountOfTimeFromTheStartBeforeTheFirstStep() {
        schedule.stepFor(simState, Period.ofMonths(1));
        assertThat(schedule.getDateTime()).isEqualTo(start.plusMonths(1));
    }

    @Test
    void stepsMonthByMonthToTheFirstOfEachMonth() {
        final List<LocalDateTime> ends = new ArrayList<>();
        final List<LocalDateTime> expected = new ArrayList<>();
        for (int month = 1; month <= 14; month++) {
            schedule.stepFor(simState, Period.ofMonths(1));
            ends.add(schedule.getDateTime());
            expected.add(start.plusMonths(month));
        }
        assertThat(ends).isEqualTo(expected);
    }

    @Test
    void keepsEveryDayOfEachMonthForReadsBeforeAMonthlyClearing() {
        final List<LocalDateTime> accumulated = new ArrayList<>();
        schedule.scheduleRepeating(
            start.plusHours(12),
            (Steppable) _ -> accumulated.add(schedule.getDateTime()),
            Duration.ofDays(1)
        );
        schedule.scheduleRepeating(
            dateTime(2013, 2, 1),
            -2,
            _ -> accumulated.clear(),
            Period.ofMonths(1)
        );
        final List<Integer> read = new ArrayList<>();
        for (int month = 0; month < 4; month++) {
            schedule.stepFor(simState, Period.ofMonths(1));
            read.add(accumulated.size());
        }
        assertThat(read).containsExactly(31, 28, 31, 30);
    }

    @Test
    void doesNothingWhenSteppingUntilTheClock() {
        schedule.scheduleOnce(start, recordFiring);
        schedule.stepUntil(simState, start);
        assertThat(firings).isEmpty();
        schedule.scheduleOnce(start.plusSeconds(1), recordFiring);
        schedule.stepUntil(simState, start.plusSeconds(1));
        schedule.stepUntil(simState, start.plusSeconds(1));
        assertThat(firings).containsExactly(start);
        assertThat(schedule.getDateTime()).isEqualTo(start.plusSeconds(1));
    }

    @Test
    void reachesTheTargetOfAnIntervalWithNoEventsWithoutStepping() {
        schedule.stepUntil(simState, start.plusDays(3));
        assertThat(schedule.getDateTime()).isEqualTo(start.plusDays(3));
        schedule.scheduleOnce(start.plusDays(10), recordFiring);
        schedule.stepUntil(simState, start.plusDays(5));
        assertThat(schedule.getDateTime()).isEqualTo(start.plusDays(5));
        assertThat(schedule.getSteps()).isZero();
    }

    @Test
    void refusesToStepUntilADateTimeInThePast() {
        schedule.stepUntil(simState, start.plusDays(1));
        assertThatThrownBy(() -> schedule.stepUntil(simState, start))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void doesNothingWhenSteppingUntilTheDisplayedDateTimeJustAfterADirectStep() {
        schedule.scheduleOnce(start.plusSeconds(1), _ ->
            schedule.scheduleOnceIn(0.0, (Steppable) _ -> schedule.scheduleOnceIn(0.0, recordFiring))
        );
        schedule.step(simState);
        schedule.step(simState);
        assertThat(schedule.getTime()).isGreaterThan(1.0);
        assertThat(schedule.getDateTime()).isEqualTo(start.plusSeconds(1));
        assertThatCode(() -> schedule.stepUntil(simState, schedule.getDateTime()))
            .doesNotThrowAnyException();
        assertThat(firings).isEmpty();
        schedule.stepUntil(simState, start.plusSeconds(2));
        assertThat(firings).containsExactly(start.plusSeconds(1));
    }

    @Test
    void runsTheNextEventsOnADirectStepAfterAPauseAndTheClockFollows() {
        final LocalDateTime until = start.plusDays(1);
        schedule.scheduleOnce(until.plusHours(1), recordFiring);
        schedule.stepUntil(simState, until);
        schedule.step(simState);
        assertThat(firings).containsExactly(until.plusHours(1));
        assertThat(schedule.getDateTime()).isEqualTo(until.plusHours(1));
        assertThatThrownBy(() -> schedule.stepUntil(simState, until.plusMinutes(30)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // --- Scheduling ---

    @Test
    void schedulesOnceAfterADurationFromNow() {
        schedule.scheduleOnce(
            start.plusHours(1),
            _ -> schedule.scheduleOnceIn(Duration.ofMinutes(30), recordFiring)
        );
        schedule.stepUntil(simState, start.plusDays(1));
        assertThat(firings).containsExactly(start.plusMinutes(90));
    }

    @Test
    void ordersEventsScheduledAtThePausedClockWithThoseAlreadyThere() {
        final LocalDateTime until = start.plusDays(1);
        schedule.scheduleOnce(until, 0, _ -> runs.add("queued earlier, ordering 0"));
        schedule.stepUntil(simState, until);
        schedule.scheduleOnce(until, -2, _ -> runs.add("queued while paused, ordering -2"));
        schedule.stepUntil(simState, until.plusSeconds(1));
        assertThat(runs).containsExactly(
            "queued while paused, ordering -2",
            "queued earlier, ordering 0"
        );
    }

    @Test
    void countsRelativeTimesFromThePausedClock() {
        final LocalDateTime until = start.plusDays(1);
        schedule.stepUntil(simState, until);
        schedule.scheduleOnceIn(Duration.ofHours(1), recordFiring, 0);
        schedule.scheduleOnce(recordFiring);
        schedule.scheduleOnceIn(10.0, recordFiring);
        schedule.stepUntil(simState, until.plusDays(1));
        assertThat(firings).containsExactly(
            until.plusSeconds(1),
            until.plusSeconds(10),
            until.plusHours(1)
        );
    }

    @Test
    void countsRelativeTimesFromThePausedClockNotFromTheLastEvent() {
        final LocalDateTime until = dateTime(2013, 2, 1);
        schedule.scheduleOnce(dateTime(2013, 1, 31).plusHours(12), _ -> {});
        schedule.stepUntil(simState, until);
        schedule.scheduleOnce(recordFiring, 0);
        schedule.stepUntil(simState, until.plusDays(1));
        assertThat(firings).containsExactly(until.plusSeconds(1));
    }

    @Test
    void doesNotRunARelativeEventAtTheTargetOfTheNextStep() {
        final LocalDateTime until = start.plusDays(1);
        schedule.stepUntil(simState, until);
        schedule.scheduleOnceIn(10.0, recordFiring);
        schedule.stepUntil(simState, until.plusSeconds(10));
        assertThat(firings).isEmpty();
        schedule.stepUntil(simState, until.plusSeconds(11));
        assertThat(firings).containsExactly(until.plusSeconds(10));
    }

    @Test
    void countsRelativeTimesFromTheStartBeforeTheFirstStep() {
        schedule.scheduleOnceIn(Duration.ofHours(1), recordFiring);
        schedule.scheduleOnceIn(Duration.ofHours(1), recordFiring, 0);
        schedule.scheduleOnceIn(3_600.0, recordFiring);
        schedule.scheduleOnce(recordFiring);
        schedule.scheduleRepeating(recordFiring, 7_200.0);
        schedule.stepUntil(simState, start.plusHours(5));
        assertThat(firings).containsExactly(
            start.plusSeconds(1),
            start.plusHours(1), start.plusHours(1), start.plusHours(1),
            start.plusHours(2),
            start.plusHours(4)
        );
    }

    @Test
    void runsAChainOfEventsScheduledForNowAtTheSameTimeInOrder() {
        schedule.scheduleOnce(start.plusSeconds(1), _ -> {
            runs.add("first");
            schedule.scheduleOnceIn(0.0, (Steppable) _ -> {
                runs.add("second");
                schedule.scheduleOnceIn(0.0, (Steppable) _ -> {
                    runs.add("third");
                    firings.add(schedule.getDateTime());
                });
            });
        });
        schedule.stepUntil(simState, start.plusSeconds(2));
        assertThat(runs).containsExactly("first", "second", "third");
        assertThat(firings).containsExactly(start.plusSeconds(1));
    }

    @Test
    void schedulesAtTheDisplayedDateTimeFromAnEventJustPastIt() {
        schedule.scheduleOnce(start.plusSeconds(1), _ -> {
            runs.add("first");
            schedule.scheduleOnceIn(0.0, (Steppable) _ -> {
                runs.add("second");
                schedule.scheduleOnce(schedule.getDateTime(), _ -> {
                    runs.add("third");
                    firings.add(schedule.getDateTime());
                });
            });
        });
        schedule.stepUntil(simState, start.plusSeconds(2));
        assertThat(runs).containsExactly("first", "second", "third");
        assertThat(firings).containsExactly(start.plusSeconds(1));
    }

    @Test
    void schedulesByDateTimeAtTheDisplayedDateTimeFromAnEventJustPastIt() {
        schedule.scheduleOnce(start.plusSeconds(1), _ ->
            schedule.scheduleOnceIn(0.0, (Steppable) _ ->
                schedule.scheduleByDateTime(List.of(entry(schedule.getDateTime(), recordFiring)))
            )
        );
        schedule.stepUntil(simState, start.plusSeconds(2));
        assertThat(firings).containsExactly(start.plusSeconds(1));
    }

    @Test
    void refusesToScheduleBeforeTheClock() {
        schedule.stepUntil(simState, start.plusSeconds(10));
        assertThatThrownBy(() -> schedule.scheduleOnce(start.plusSeconds(5), recordFiring))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesToScheduleBetweenTheLastEventAndThePausedClock() {
        schedule.scheduleOnce(dateTime(2013, 1, 31).plusHours(12), recordFiring);
        schedule.stepUntil(simState, dateTime(2013, 2, 1));
        assertThat(firings).containsExactly(dateTime(2013, 1, 31).plusHours(12));
        assertThatThrownBy(() ->
            schedule.scheduleOnce(dateTime(2013, 1, 31).plusHours(18), recordFiring)
        ).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
            schedule.scheduleRepeating(schedule.toTime(dateTime(2013, 1, 31)), 0, recordFiring, 60.0)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesToMergeSchedules() {
        assertThatThrownBy(() -> schedule.merge(new Schedule()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // --- Repeats ---

    @Test
    void repeatsMonthlyOnTheSameDayOfTheMonth() {
        schedule.scheduleRepeating(start, recordFiring, Period.ofMonths(1));
        schedule.stepUntil(simState, dateTime(2013, 5, 2));
        assertThat(firings).containsExactly(
            dateTime(2013, 1, 1),
            dateTime(2013, 2, 1),
            dateTime(2013, 3, 1),
            dateTime(2013, 4, 1),
            dateTime(2013, 5, 1)
        );
    }

    @Test
    void repeatsMonthlyOnTheLastDaysOfLongMonths() {
        schedule.scheduleRepeating(dateTime(2013, 1, 31), recordFiring, Period.ofMonths(1));
        schedule.stepUntil(simState, dateTime(2013, 5, 1));
        assertThat(firings).containsExactly(
            dateTime(2013, 1, 31),
            dateTime(2013, 2, 28),
            dateTime(2013, 3, 31),
            dateTime(2013, 4, 30)
        );
    }

    @Test
    void repeatsAFractionalDurationAtItsAveragePace() {
        schedule.scheduleRepeating(start, recordFiring, Duration.ofMillis(1_500));
        schedule.stepUntil(simState, start.plusSeconds(7));
        assertThat(firings).containsExactly(
            start,
            start.plusSeconds(1),
            start.plusSeconds(3),
            start.plusSeconds(4),
            start.plusSeconds(6)
        );
    }

    @Test
    void repeatsYearlyFromALeapDayOnEachLeapDay() {
        schedule.scheduleRepeating(dateTime(2016, 2, 29), recordFiring, Period.ofYears(1));
        schedule.stepUntil(simState, dateTime(2020, 3, 1));
        assertThat(firings).containsExactly(
            dateTime(2016, 2, 29),
            dateTime(2017, 2, 28),
            dateTime(2018, 2, 28),
            dateTime(2019, 2, 28),
            dateTime(2020, 2, 29)
        );
    }

    @Test
    void refusesARepeatIntervalThatCannotBeMultiplied() {
        final TemporalAmount minguoMonth = MinguoChronology.INSTANCE.period(0, 1, 0);
        assertThatThrownBy(() -> schedule.scheduleRepeating(start, recordFiring, minguoMonth))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesARepeatIntervalThatDoesNotMoveTimeForward() {
        final List<TemporalAmount> intervals = List.of(
            Duration.ZERO,
            Period.ZERO,
            Duration.ofSeconds(-1),
            Period.ofDays(-1),
            Period.of(0, 1, -1)
        );
        for (final TemporalAmount interval : intervals) {
            assertThatThrownBy(() -> schedule.scheduleRepeating(start, recordFiring, interval))
                .as("interval %s", interval)
                .isInstanceOf(IllegalArgumentException.class);
        }
        schedule.stepUntil(simState, start.plusSeconds(1));
        assertThat(firings).isEmpty();
    }

    @Test
    void repeatsYearlyOnTheSameDateAcrossLeapYears() {
        schedule.scheduleRepeating(dateTime(2015, 3, 1), recordFiring, Period.ofYears(1));
        schedule.stepUntil(simState, dateTime(2018, 3, 2));
        assertThat(firings).containsExactly(
            dateTime(2015, 3, 1),
            dateTime(2016, 3, 1),
            dateTime(2017, 3, 1),
            dateTime(2018, 3, 1)
        );
    }

    @Test
    void repeatsFromNowPlusTheInterval() {
        schedule.scheduleOnce(
            start.plusDays(1),
            _ -> schedule.scheduleRepeating(recordFiring, Duration.ofHours(12))
        );
        schedule.stepUntil(simState, start.plusDays(2).plusHours(1));
        assertThat(firings).containsExactly(
            start.plusDays(1).plusHours(12),
            start.plusDays(2)
        );
    }

    @Test
    void keepsTheRepeatsPaceAcrossPauses() {
        final List<LocalDateTime> temporal = new ArrayList<>();
        final List<LocalDateTime> mason = new ArrayList<>();
        schedule.scheduleRepeating(
            start.plusHours(12),
            (Steppable) _ -> temporal.add(schedule.getDateTime()),
            Duration.ofDays(1)
        );
        schedule.scheduleRepeating(
            3_600.0,
            0,
            (Steppable) _ -> mason.add(schedule.getDateTime()),
            7 * 3_600.0
        );
        final Duration pause = Duration.ofMinutes(10 * 60 + 17);
        for (int i = 0; i < 9; i++) {
            schedule.stepFor(simState, pause);
        }
        final LocalDateTime end = start.plus(pause.multipliedBy(9));
        final List<LocalDateTime> expectedTemporal = new ArrayList<>();
        for (LocalDateTime t = start.plusHours(12); t.isBefore(end); t = t.plusDays(1)) {
            expectedTemporal.add(t);
        }
        final List<LocalDateTime> expectedMason = new ArrayList<>();
        for (LocalDateTime t = start.plusHours(1); t.isBefore(end); t = t.plusHours(7)) {
            expectedMason.add(t);
        }
        assertThat(temporal).isEqualTo(expectedTemporal);
        assertThat(mason).isEqualTo(expectedMason);
    }

    // --- Scheduling by date-time ---

    @Test
    void runsEntriesDatedBeforeNowRightAwayInDateOrder() {
        schedule.stepUntil(simState, start.plusDays(10));
        schedule.scheduleByDateTime(List.of(
            entry(start.plusDays(5), _ -> runs.add("day 5")),
            entry(start.plusDays(2), _ -> runs.add("day 2")),
            entry(start.plusDays(12), _ -> runs.add("day 12"))
        ));
        schedule.step(simState);
        assertThat(runs).containsExactly("day 2", "day 5");
        schedule.stepUntil(simState, start.plusDays(13));
        assertThat(runs).containsExactly("day 2", "day 5", "day 12");
    }

    @Test
    void runsEntriesDatedBeforeTheStartAtTheStart() {
        schedule.scheduleByDateTime(List.of(
            entry(start.minusYears(1), recordFiring),
            entry(start.plusDays(1), recordFiring)
        ));
        schedule.stepUntil(simState, start.plusDays(2));
        assertThat(firings).containsExactly(start, start.plusDays(1));
    }

    @Test
    void runsEntriesWithTheSameDateTimeInTheirGivenOrder() {
        schedule.scheduleByDateTime(List.of(
            entry(start.plusDays(1), _ -> runs.add("first")),
            entry(start.plusDays(1), _ -> runs.add("second"))
        ));
        schedule.stepUntil(simState, start.plusDays(2));
        assertThat(runs).containsExactly("first", "second");
    }

    // --- Finishing, killing and resetting ---

    @Test
    void refusesToStepAFinishedSchedule() {
        schedule.stepUntil(simState, start.plusDays(1));
        simState.finish();
        assertThatThrownBy(() -> schedule.stepUntil(simState, start.plusDays(2)))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> schedule.stepUntil(simState, start.plusDays(1)))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> schedule.stepFor(simState, Duration.ZERO))
            .isInstanceOf(IllegalStateException.class);
        assertThat(schedule.getDateTime()).isEqualTo(start.plusDays(1));
    }

    @Test
    void refusesToStepPastAKillAndKeepsItsTime() {
        schedule.scheduleOnce(start.plusHours(1), _ -> simState.kill());
        assertThatThrownBy(() -> schedule.stepUntil(simState, start.plusDays(1)))
            .isInstanceOf(IllegalStateException.class);
        assertThat(schedule.getDateTime()).isEqualTo(start.plusHours(1));
    }

    @Test
    void refusesToStepPastAKillInABeforeHook() {
        schedule.scheduleOnce(start.plusSeconds(5), _ -> {});
        schedule.addBefore(sim -> sim.kill());
        assertThatThrownBy(() -> schedule.stepUntil(simState, start.plusSeconds(10)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesToStepPastAKillInAnAfterHook() {
        final LocalDateTime eventDateTime = start.plusSeconds(5);
        schedule.scheduleOnce(eventDateTime, _ -> {});
        schedule.addAfter(sim -> {
            if (schedule.getDateTime().equals(eventDateTime)) {
                sim.kill();
            }
        });
        assertThatThrownBy(() -> schedule.stepUntil(simState, start.plusSeconds(10)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsFalseOnADirectStepOfAFinishedScheduleWithoutMovingTheClock() {
        schedule.stepUntil(simState, start.plusDays(1));
        simState.finish();
        assertThat(schedule.step(simState)).isFalse();
        assertThat(schedule.getDateTime()).isEqualTo(start.plusDays(1));
    }

    @Test
    void returnsNormallyFromADirectStepThatKillsTheScheduleThenFalse() {
        schedule.scheduleOnce(start.plusHours(1), _ -> simState.kill());
        assertThatCode(() -> schedule.step(simState)).doesNotThrowAnyException();
        assertThat(schedule.step(simState)).isFalse();
        assertThat(schedule.getDateTime()).isEqualTo(start.plusHours(1));
    }

    @Test
    void bringsTheClockBackToTheStartOnReset() {
        schedule.stepUntil(simState, start.plusDays(1));
        schedule.reset();
        assertThat(schedule.getDateTime()).isEqualTo(start);
        schedule.scheduleOnce(start, recordFiring);
        schedule.stepUntil(simState, start.plusSeconds(1));
        assertThat(firings).containsExactly(start);
    }

    // --- One advance at a time ---

    @Test
    void refusesToAdvanceFromInsideAnEvent() {
        final List<RuntimeException> refused = new ArrayList<>();
        schedule.scheduleOnce(start.plusSeconds(1), 0, _ -> {
            try {
                schedule.stepUntil(simState, start.plusSeconds(10));
            } catch (final IllegalStateException e) {
                refused.add(e);
            }
            try {
                schedule.step(simState);
            } catch (final IllegalStateException e) {
                refused.add(e);
            }
        });
        schedule.scheduleOnce(start.plusSeconds(1), 1, recordFiring);
        schedule.scheduleOnce(start.plusSeconds(5), recordFiring);
        schedule.stepUntil(simState, start.plusSeconds(2));
        assertThat(refused).hasSize(2);
        assertThat(firings).containsExactly(start.plusSeconds(1));
        assertThat(schedule.getDateTime()).isEqualTo(start.plusSeconds(2));
        schedule.stepUntil(simState, start.plusSeconds(10));
        assertThat(firings).containsExactly(start.plusSeconds(1), start.plusSeconds(5));
    }

    @Test
    void keepsAnotherStepFromTakingAnEventInTheMiddleOfAnAdvance() throws InterruptedException {
        final CountDownLatch paused = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final TemporalSchedule pausing = pausingAtTheFirstCheckpoint(paused, release);
        final SimState state = new SimState(0, pausing) {};
        final Map<String, String> ranOn = new ConcurrentHashMap<>();
        pausing.scheduleOnce(
            start.plusSeconds(5),
            _ -> ranOn.put("5 s", Thread.currentThread().getName())
        );
        pausing.scheduleOnce(
            start.plusSeconds(20),
            _ -> ranOn.put("20 s", Thread.currentThread().getName())
        );
        runWhileTheFirstIsPaused(
            paused,
            release,
            new Thread(() -> pausing.stepUntil(state, start.plusSeconds(10)), "stepUntil"),
            new Thread(() -> pausing.step(state), "step")
        );
        assertThat(ranOn).containsExactlyInAnyOrderEntriesOf(
            Map.of("5 s", "stepUntil", "20 s", "step")
        );
    }

    @Test
    void advancesForEachOfTwoConcurrentStepFors() throws InterruptedException {
        final CountDownLatch paused = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final TemporalSchedule pausing = pausingAtTheFirstCheckpoint(paused, release);
        final SimState state = new SimState(0, pausing) {};
        pausing.scheduleOnce(start.plusSeconds(5), _ -> {});
        runWhileTheFirstIsPaused(
            paused,
            release,
            new Thread(() -> pausing.stepFor(state, Duration.ofSeconds(10)), "first"),
            new Thread(() -> pausing.stepFor(state, Duration.ofSeconds(10)), "second")
        );
        assertThat(pausing.getDateTime()).isEqualTo(start.plusSeconds(20));
    }

    /** A schedule whose first advance pauses between looking at the queue and stepping. */
    private TemporalSchedule pausingAtTheFirstCheckpoint(
        final CountDownLatch paused,
        final CountDownLatch release
    ) {
        final AtomicBoolean first = new AtomicBoolean(true);
        return new TemporalSchedule(start) {
            @Override
            void checkpoint() {
                if (first.getAndSet(false)) {
                    paused.countDown();
                    try {
                        release.await();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
    }

    /**
     * Starts {@code first} and waits until it pauses at its checkpoint, then starts
     * {@code second} and waits until it is blocked on the schedule's monitor (or has finished,
     * if nothing blocks it), then releases {@code first} and waits for both.
     */
    private static void runWhileTheFirstIsPaused(
        final CountDownLatch paused,
        final CountDownLatch release,
        final Thread first,
        final Thread second
    ) throws InterruptedException {
        first.start();
        assertThat(paused.await(10, SECONDS)).as("first thread paused").isTrue();
        second.start();
        final long deadline = System.nanoTime() + SECONDS.toNanos(10);
        while (second.getState() != Thread.State.BLOCKED &&
            second.getState() != Thread.State.TERMINATED) {
            assertThat(System.nanoTime()).as("second thread blocked or finished").isLessThan(deadline);
            Thread.onSpinWait();
        }
        release.countDown();
        first.join();
        second.join();
    }
}

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

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NextTimeAtOneOfProviderTest {

    @Test
    void picksNextTimeLaterToday() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 2, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(22, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 1, 6, 0));
    }

    @Test
    void picksLaterTimeOnSameDay() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 7, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(22, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 1, 22, 0));
    }

    @Test
    void wrapsToNextDayWhenAllTimesPassed() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 23, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(22, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
    }

    @Test
    void picksSecondTimeWhenCurrentTimeExactlyMatchesFirst() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 6, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(22, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 1, 22, 0));
    }

    @Test
    void wrapsToNextDayWhenCurrentTimeExactlyMatchesLast() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 22, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(22, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
    }

    @Test
    void worksWithSingleTime() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 11, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(12, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 1, 12, 0));
    }

    @Test
    void wrapsToNextDayWithSingleTime() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 13, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(12, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 2, 12, 0));
    }

    @Test
    void handlesUnsortedTimes() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 2, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(22, 0), LocalTime.of(6, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 1, 6, 0));
    }

    @Test
    void wrapsToFirstTimeNextDayWithUnsortedInput() {
        TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2025, 6, 1, 23, 0));
        when(schedule.getDate()).thenReturn(LocalDate.of(2025, 6, 1));

        NextTimeAtOneOfProvider provider = new NextTimeAtOneOfProvider(
            schedule,
            new LocalTime[]{LocalTime.of(22, 0), LocalTime.of(6, 0)}
        );

        assertThat(provider.get()).isEqualTo(LocalDateTime.of(2025, 6, 2, 6, 0));
    }
}

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

/**
 * Whether the clock has a date-time follows the schedule, which has none once
 * it has run out of events.
 */
class HasDateTimeTest {

  @Test
  void saysWhetherTheClockHasADateTime() {
    final TemporalSchedule schedule = mock(TemporalSchedule.class);
    final HasDateTime hasDateTime = new HasDateTime(schedule);

    when(schedule.hasDateTime()).thenReturn(true);
    assertThat(hasDateTime.apply(null)).isTrue();

    when(schedule.hasDateTime()).thenReturn(false);
    assertThat(hasDateTime.apply(null)).isFalse();
  }
}

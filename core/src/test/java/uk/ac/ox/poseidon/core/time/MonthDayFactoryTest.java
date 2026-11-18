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

package uk.ac.ox.poseidon.core.time;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;

import java.time.Month;
import java.time.MonthDay;
import org.junit.jupiter.api.Test;

/**
 * A month-day can be built from its parts or parsed from its usual text form.
 */
class MonthDayFactoryTest {

  @Test
  void parsesAMonthDayString() {
    assertThat(MonthDayFactory.parse("--12-03").get(GLOBAL_SCOPE)).isEqualTo(
      MonthDay.of(12, 3)
    );
  }

  @Test
  void buildsFromAMonthAndADayOfMonth() {
    assertThat(
      new MonthDayFactory(Month.DECEMBER, 3).get(GLOBAL_SCOPE)
    ).isEqualTo(MonthDay.of(Month.DECEMBER, 3));
  }
}

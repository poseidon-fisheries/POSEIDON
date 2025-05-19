/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.regulations.core.conditions;

import org.junit.jupiter.api.Test;

import java.time.MonthDay;

import static java.time.Month.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetweenYearlyDatesTest {

    @Test
    void whenNotYearSpanning() {
        final BetweenYearlyDates betweenYearlyDates =
            new BetweenYearlyDates(
                MonthDay.of(OCTOBER, 1),
                MonthDay.of(NOVEMBER, 13)
            );
        assertFalse(betweenYearlyDates.isYearSpanning());
        assertFalse(betweenYearlyDates.test(MonthDay.of(SEPTEMBER, 30)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(OCTOBER, 1)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(OCTOBER, 2)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 12)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 13)));
        assertFalse(betweenYearlyDates.test(MonthDay.of(NOVEMBER, 14)));
    }

    @Test
    void whenYearSpanning() {
        final BetweenYearlyDates betweenYearlyDates =
            new BetweenYearlyDates(
                MonthDay.of(DECEMBER, 15),
                MonthDay.of(JANUARY, 15)
            );
        assertTrue(betweenYearlyDates.isYearSpanning());
        assertFalse(betweenYearlyDates.test(MonthDay.of(DECEMBER, 14)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(DECEMBER, 15)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(DECEMBER, 16)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(JANUARY, 14)));
        assertTrue(betweenYearlyDates.test(MonthDay.of(JANUARY, 15)));
        assertFalse(betweenYearlyDates.test(MonthDay.of(JANUARY, 16)));
    }

}

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

import java.time.LocalDate;

import static java.time.Month.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BetweenDatesTest {
    @Test
    void test() {
        final BetweenDates betweenDates =
            new BetweenDates(
                LocalDate.of(2017, OCTOBER, 1),
                LocalDate.of(2018, NOVEMBER, 13)
            );
        assertFalse(betweenDates.test(LocalDate.of(2017, SEPTEMBER, 30)));
        assertTrue(betweenDates.test(LocalDate.of(2017, OCTOBER, 1)));
        assertTrue(betweenDates.test(LocalDate.of(2017, OCTOBER, 2)));
        assertTrue(betweenDates.test(LocalDate.of(2018, NOVEMBER, 12)));
        assertTrue(betweenDates.test(LocalDate.of(2018, NOVEMBER, 13)));
        assertFalse(betweenDates.test(LocalDate.of(2018, NOVEMBER, 14)));
    }
}

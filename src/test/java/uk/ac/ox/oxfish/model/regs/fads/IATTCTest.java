/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.regs.fads;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class IATTCTest {

    @Test
    public void capacityClass() {
        assertThrows(IllegalArgumentException.class, () -> IATTC.capacityClass(0));
        assertEquals(1, IATTC.capacityClass(45000));
        assertEquals(2, IATTC.capacityClass(46000));
        assertEquals(2, IATTC.capacityClass(91000));
        assertEquals(3, IATTC.capacityClass(92000));
        assertEquals(3, IATTC.capacityClass(181000));
        assertEquals(4, IATTC.capacityClass(182000));
        assertEquals(4, IATTC.capacityClass(272000));
        assertEquals(5, IATTC.capacityClass(273000));
        assertEquals(5, IATTC.capacityClass(363000));
        assertEquals(6, IATTC.capacityClass(364000));
        assertEquals(6, IATTC.capacityClass(Double.MAX_VALUE));
    }
}
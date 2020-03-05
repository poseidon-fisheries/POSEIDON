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

import static org.junit.Assert.*;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;

public class IATTCTest {

    @Test
    public void capacityClass() {
        assertEquals(1, IATTC.capacityClass(getQuantity(0, CUBIC_METRE)));
        assertEquals(1, IATTC.capacityClass(getQuantity(53, CUBIC_METRE)));
        assertEquals(2, IATTC.capacityClass(getQuantity(54, CUBIC_METRE)));
        assertEquals(2, IATTC.capacityClass(getQuantity(107, CUBIC_METRE)));
        assertEquals(3, IATTC.capacityClass(getQuantity(108, CUBIC_METRE)));
        assertEquals(3, IATTC.capacityClass(getQuantity(212, CUBIC_METRE)));
        assertEquals(4, IATTC.capacityClass(getQuantity(213, CUBIC_METRE)));
        assertEquals(4, IATTC.capacityClass(getQuantity(318, CUBIC_METRE)));
        assertEquals(5, IATTC.capacityClass(getQuantity(319, CUBIC_METRE)));
        assertEquals(5, IATTC.capacityClass(getQuantity(425, CUBIC_METRE)));
        assertEquals(6, IATTC.capacityClass(getQuantity(436, CUBIC_METRE)));
        assertEquals(6, IATTC.capacityClass(getQuantity(Double.MAX_VALUE, CUBIC_METRE)));
    }
}
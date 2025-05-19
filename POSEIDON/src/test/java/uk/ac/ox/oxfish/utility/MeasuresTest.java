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

package uk.ac.ox.oxfish.utility;

import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class MeasuresTest {
    @Test
    public void testUnits() {
        final Quantity<Mass> oneTonne = getQuantity(1, TONNE);
        Log.info("Make sure that our definition of the metric tonne is equal to 1000 kg.");
        Assertions.assertEquals(
            oneTonne.to(KILOGRAM).getValue().doubleValue(),
            getQuantity(1000, KILOGRAM).getValue().doubleValue(),
            0
        );
        Log.info("Test that the same holds when using our asDouble convenience method.");
        Assertions.assertEquals(asDouble(oneTonne, KILOGRAM), 1000, 0);
    }
}

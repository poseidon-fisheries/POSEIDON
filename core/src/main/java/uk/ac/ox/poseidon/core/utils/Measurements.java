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

package uk.ac.ox.poseidon.core.utils;

import tech.units.indriya.format.SimpleUnitFormat;

import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static tech.units.indriya.unit.UnitDimension.MASS;

public class Measurements {
    private Measurements() {
    }

    public static <Q extends Quantity<Q>> Unit<Q> parseUnit(
        final String unitString,
        final Class<Q> quantityClass,
        final Dimension dimension
    ) {
        final Unit<?> unit = SimpleUnitFormat.getInstance().parse(unitString);
        return unit.getDimension() == dimension
            ? unit.asType(quantityClass)
            : null;
    }

    public static Unit<Mass> parseMassUnit(final String unitString) {
        return parseUnit(unitString, Mass.class, MASS);
    }
}

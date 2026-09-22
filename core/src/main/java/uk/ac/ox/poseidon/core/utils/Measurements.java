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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static tech.units.indriya.unit.UnitDimension.MASS;

/** Helpers for parsing and validating JSR-385 {@link Unit}s. */
public class Measurements {
    private Measurements() {
    }

    /**
     * @param unitString    the unit string to parse
     * @param quantityClass the quantity type to type the parsed unit as
     * @param dimension     the dimension the parsed unit must have
     * @return the parsed unit typed as {@code Q}, or {@code null} if its dimension doesn't match
     * {@code dimension}
     */
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

    /**
     * @param unitString a non-null, non-empty unit string
     * @return the parsed unit, typed as {@link Mass}, or {@code null} if its dimension isn't mass
     */
    public static Unit<Mass> parseMassUnit(final String unitString) {
        checkNotNull(unitString, "Trying to parse null as a unit of mass");
        checkArgument(!unitString.isEmpty(), "Trying to parse an empty string as a unit of mass");
        return parseUnit(unitString, Mass.class, MASS);
    }
}

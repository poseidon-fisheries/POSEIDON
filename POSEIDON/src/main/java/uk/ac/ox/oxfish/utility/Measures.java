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

import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.BaseUnit;
import tech.units.indriya.unit.TransformedUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Time;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.METRE;

public class Measures {

    public static final Unit<Money> DOLLAR = new BaseUnit<>("$");

    // we could use KILO(METRE) for pure conversions, but we need a new unit to specify the "km" symbol.
    public static final Unit<Length> KILOMETRE = new TransformedUnit<>("km", METRE, MultiplyConverter.of(1000));

    /**
     * @param t A Time duration
     * @return that duration as an integer number of hours, rounded up
     */
    public static int toHours(Quantity<Time> t) {
        return (int) Math.ceil(asDouble(t, HOUR));
    }

    /**
     * a convenience method to convert and quantity and get it's value as double in one go.
     */
    public static <Q extends Quantity<Q>> double asDouble(Quantity<Q> quantity, Unit<Q> unit) {
        return quantity.to(unit).getValue().doubleValue();
    }

    /**
     * Converts a source value from one unit of measure to another.
     * Useful for being explicit about conversions, but not to be used in tight loops.
     */
    public static <Q extends Quantity<Q>> double convert(double sourceValue, Unit<Q> sourceUnit, Unit<Q> targetUnit) {
        return asDouble(getQuantity(sourceValue, sourceUnit), targetUnit);
    }

    public interface Money extends Quantity<Money> {
    }

}

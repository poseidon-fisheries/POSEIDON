package uk.ac.ox.oxfish.utility;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Time;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class Measures {

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



}
package uk.ac.ox.oxfish.utility;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Time;

import static tech.units.indriya.unit.Units.HOUR;

public class Measures {

    /**
     * @param t A Time duration
     * @return that duration as an integer number of hours, rounded up
     */
    public static int toHours(Quantity<Time> t) { return (int) Math.ceil(asDouble(t, HOUR)); }

    /**
     * a convenience method to convert and quantity and get it's value as double in one go.
     */
    public static <Q extends Quantity<Q>> double asDouble(Quantity<Q> quantity, Unit<Q> unit) {
        return quantity.to(unit).getValue().doubleValue();
    }

}
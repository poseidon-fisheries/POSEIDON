package uk.ac.ox.oxfish.utility;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Time;

import static org.apache.sis.measure.Units.HOUR;
import static org.apache.sis.measure.Units.KILOGRAM;

public class Measures {

    /**
     * the definition of metric tonnes as a javax.measure
     */
    public static final Unit<Mass> TONNE = KILOGRAM.multiply(1000); // 1 t = 1000 kg

    /**
     * @param t A Time duration
     * @return that duration as an integer number of hours, rounded up
     */
    public static int toHours(Time t) { return (int) Math.ceil(asDouble(t, HOUR)); }

    /**
     * a convenience method to convert and quantity and get it's value as double in one go.
     */
    public static <Q extends Quantity<Q>> double asDouble(Quantity<Q> quantity, Unit<Q> unit) {
        return quantity.to(unit).getValue().doubleValue();
    }

}
package uk.ac.ox.oxfish.utility;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static org.apache.sis.measure.Units.KILOGRAM;

/**
 * This class currently just adds the definition of metric tonnes as a javax.measure and
 * provides a convenience method to convert and quantity and get it's value as double in one go.
 */
public class Measures {
    public static final Unit<Mass> TONNE = KILOGRAM.multiply(1000); // 1 t = 1000 kg

    public static <Q extends Quantity<Q>> double asDouble(Quantity<Q> quantity, Unit<Q> unit) {
        return quantity.to(unit).getValue().doubleValue();
    }
}
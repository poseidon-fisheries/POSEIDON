package uk.ac.ox.oxfish.utility;

import org.jfree.util.Log;
import org.junit.Test;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static org.junit.Assert.assertEquals;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class MeasuresTest {
    @Test
    public void testUnits() {
        final Quantity<Mass> oneTonne = getQuantity(1, TONNE);
        Log.info("Make sure that our definition of the metric tonne is equal to 1000 kg.");
        assertEquals(
            oneTonne.to(KILOGRAM).getValue().doubleValue(),
            getQuantity(1000, KILOGRAM).getValue().doubleValue(),
            0
        );
        Log.info("Test that the same holds when using our asDouble convenience method.");
        assertEquals(asDouble(oneTonne, KILOGRAM), 1000, 0);
    }
}
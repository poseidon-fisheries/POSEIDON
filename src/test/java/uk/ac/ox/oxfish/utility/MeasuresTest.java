package uk.ac.ox.oxfish.utility;

import org.apache.sis.measure.Quantities;
import org.jfree.util.Log;
import org.junit.Test;

import javax.measure.quantity.Mass;

import static org.apache.sis.measure.Units.KILOGRAM;
import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.utility.Measures.TONNE;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class MeasuresTest {
    @Test
    public void testUnits() {
        final Mass oneTonne = Quantities.create(1, TONNE);
        Log.info("Make sure that our definition of the metric tonne is equal to 1000 kg.");
        assertEquals(
            oneTonne.to(KILOGRAM).getValue().doubleValue(),
            Quantities.create(1000, KILOGRAM).getValue().doubleValue(),
            0
        );
        Log.info("Test that the same holds when using our asDouble convenience method.");
        assertEquals(asDouble(oneTonne, KILOGRAM), 1000, 0);
    }
}
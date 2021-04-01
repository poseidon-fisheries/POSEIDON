package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Envelope;
import junit.framework.TestCase;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

public class MapExtentTest extends TestCase {

    /**
     * Check that MapExtent's mapping of coordinates to grid cell matches that of GeomGridField
     */
    public void testCoords() {
        // Note: this would be a good candidate for property based testing
        Envelope envelope = new Envelope(-5, 5, -5, 5);
        MapExtent mapExtent = new MapExtent(10, 10, envelope);
        GeomGridField geomGridField = new GeomGridField(new ObjectGrid2D(10, 10));
        geomGridField.setMBR(envelope);

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                assertEquals(geomGridField.toXCoord(x), mapExtent.toGridX(x));
                assertEquals(geomGridField.toYCoord(y), mapExtent.toGridY(y));
            }
        }

    }
}
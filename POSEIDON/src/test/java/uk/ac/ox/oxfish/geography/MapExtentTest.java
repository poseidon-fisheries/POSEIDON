package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import junit.framework.TestCase;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;
import sim.util.Double2D;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class MapExtentTest extends TestCase {

    /**
     * Check that MapExtent's mapping of coordinates to grid cell matches that of GeomGridField
     */
    public void testCoords() {
        // Note: this would be a good candidate for property based testing
        Envelope envelope = new Envelope(-5, 5, -5, 5);
        MapExtent mapExtent = MapExtent.from(10, 10, envelope);
        GeomGridField geomGridField = new GeomGridField(new ObjectGrid2D(10, 10));
        geomGridField.setMBR(envelope);

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                assertEquals(geomGridField.toXCoord(x), mapExtent.toGridX(x));
                assertEquals(geomGridField.toYCoord(y), mapExtent.toGridY(y));
            }
        }

    }

    public void testCoordToXyAndBack() {
        final MapExtent mapExtent = EpoScenario.DEFAULT_MAP_EXTENT;
        final Envelope envelope = mapExtent.getEnvelope();
        final List<Coordinate> coordinates =
            range((int) envelope.getMinX(), (int) envelope.getMaxX() - 1)
                .mapToObj(x -> x + 0.5)
                .flatMap(x ->
                    range((int) envelope.getMinY(), (int) envelope.getMaxY() - 1)
                        .mapToObj(y -> y + 0.5)
                        .map(y -> new Coordinate(x, y))
                ).collect(toList());

        coordinates.forEach(coordinate -> {
            final Double2D xy = mapExtent.coordinateToXY(coordinate);
            assertEquals(coordinate.toString(), coordinate, mapExtent.getCoordinates((int) xy.x, (int) xy.y));
        });
    }
}
package uk.ac.ox.oxfish.model.data.jsonexport;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.NauticalMap;

import static java.lang.Math.abs;

public class JsonExportUtils {

    static double seaTileWidth(NauticalMap map) {
        final Coordinate c0 = map.getCoordinates(0, 0);
        final Coordinate c1 = map.getCoordinates(1, 0);
        return abs(c1.x - c0.x);
    }

    static double seaTileHeight(NauticalMap map) {
        final Coordinate c0 = map.getCoordinates(0, 0);
        final Coordinate c1 = map.getCoordinates(0, 1);
        return abs(c1.y - c0.y);
    }
}

package uk.ac.ox.oxfish.geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

public class CoordinateField {

    private final GeomGridField geomGridField;
    private final ObjectGrid2D objectGrid2D;

    public CoordinateField(MapExtent mapExtent) {
        this.objectGrid2D = new ObjectGrid2D(mapExtent.getGridWidth(), mapExtent.getGridHeight());
        this.geomGridField = new GeomGridField(objectGrid2D);
        geomGridField.setMBR(mapExtent.getEnvelope());
    }

    public Coordinate getCoordinate(int gridX, int gridY) {
        Coordinate coordinate = (Coordinate) objectGrid2D.get(gridX, gridY);
        if (coordinate == null) {
            coordinate = geomGridField.toPoint(gridX, gridY).getCoordinate();
            objectGrid2D.set(gridX, gridY, coordinate);
        }
        return coordinate;
    }

    public Point toPoint(int gridX, int gridY) {
        return geomGridField.toPoint(gridX, gridY);
    }
}

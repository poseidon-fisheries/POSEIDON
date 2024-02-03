package uk.ac.ox.poseidon.common.core.geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

public class CoordinateField {

    private final GeomGridField geomGridField;
    private final ObjectGrid2D objectGrid2D;

    public CoordinateField(final MapExtent mapExtent) {
        this.objectGrid2D = new ObjectGrid2D(mapExtent.getGridWidth(), mapExtent.getGridHeight());
        this.geomGridField = new GeomGridField(objectGrid2D);
        geomGridField.setMBR(mapExtent.getEnvelope());
    }

    public Coordinate getCoordinate(
        final int gridX,
        final int gridY
    ) {
        Coordinate coordinate = (Coordinate) objectGrid2D.get(gridX, gridY);
        if (coordinate == null) {
            coordinate = geomGridField.toPoint(gridX, gridY).getCoordinate();
            objectGrid2D.set(gridX, gridY, coordinate);
        }
        return coordinate;
    }

    public Point toPoint(
        final int gridX,
        final int gridY
    ) {
        return geomGridField.toPoint(gridX, gridY);
    }
}

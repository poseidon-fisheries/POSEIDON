package uk.ac.ox.poseidon.common.core.geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

class CoordinateField {

    private final GeomGridField geomGridField;
    private final ObjectGrid2D objectGrid2D;

    CoordinateField(
        final int gridWidth,
        final int gridHeight,
        final Envelope envelope
    ) {
        this.objectGrid2D = new ObjectGrid2D(gridWidth, gridHeight);
        this.geomGridField = new GeomGridField(objectGrid2D);
        geomGridField.setMBR(envelope);
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

    Point toPoint(
        final int gridX,
        final int gridY
    ) {
        return geomGridField.toPoint(gridX, gridY);
    }
}

package uk.ac.ox.poseidon.regulations.core.conditions;

import com.vividsolutions.jts.geom.Coordinate;
import sim.field.geo.GeomVectorField;

public class InVectorField extends CachedCoordinateCondition {

    private final GeomVectorField vectorField;

    public InVectorField(final GeomVectorField vectorField) {
        this.vectorField = vectorField;
    }

    @Override
    public boolean test(final Coordinate coordinate) {
        return vectorField.isCovered(coordinate);
    }

}

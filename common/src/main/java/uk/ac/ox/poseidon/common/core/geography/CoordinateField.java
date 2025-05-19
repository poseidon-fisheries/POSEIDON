/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

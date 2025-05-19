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

package uk.ac.ox.poseidon.geography;

import lombok.Data;
import lombok.NonNull;

import java.awt.geom.Point2D;

/**
 * Represents a geographic coordinate specified by latitude and longitude. This class provides
 * methods for conversion to and from instances of {@code com.vividsolutions.jts.geom.Coordinate},
 * which we don't want to use directly because it is mutable and therefore unsuitable for
 * concurrency or usage as a hash key.
 */
@Data
public final class Coordinate {
    public final double lon;
    public final double lat;

    public static Coordinate fromPoint2D(
        @NonNull final Point2D point
    ) {
        return new Coordinate(point.getX(), point.getY());
    }

    public static Coordinate fromJTS(
        @NonNull final org.locationtech.jts.geom.Coordinate jtsCoordinate
    ) {
        return new Coordinate(jtsCoordinate.x, jtsCoordinate.y);
    }

    public org.locationtech.jts.geom.Coordinate toJTS() {
        return new org.locationtech.jts.geom.Coordinate(lon, lat);
    }

    @Override
    public String toString() {
        return "(" + this.lon + ", " + this.lat + ")";
    }
}

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

package uk.ac.ox.oxfish.model.regs.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.function.BiPredicate;

public class SpecificProtectedAreaFromCoordinatesFactory extends SpecificProtectedAreaFactory {

    private double northLatitude;
    private double westLongitude;
    private double southLatitude;
    private double eastLongitude;

    public SpecificProtectedAreaFromCoordinatesFactory(
        final String name,
        final double northLatitude,
        final double westLongitude,
        final double southLatitude,
        final double eastLongitude
    ) {
        setName(name);
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;
    }

    @SuppressWarnings("unused")
    public SpecificProtectedAreaFromCoordinatesFactory() {
        this(1, 1, 1, 1);
    }

    public SpecificProtectedAreaFromCoordinatesFactory(
        final double northLatitude,
        final double westLongitude,
        final double southLatitude,
        final double eastLongitude
    ) {
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.southLatitude = southLatitude;
        this.eastLongitude = eastLongitude;
    }

    @Override
    BiPredicate<Integer, Integer> inAreaPredicate(final MapExtent mapExtent) {
        final Envelope envelope = new Envelope(
            new Coordinate(getWestLongitude(), getNorthLatitude()),
            new Coordinate(getEastLongitude(), getSouthLatitude())
        );
        return (x, y) -> envelope.covers(mapExtent.getCoordinates(x, y));
    }

    public double getWestLongitude() {
        return westLongitude;
    }

    public double getNorthLatitude() {
        return northLatitude;
    }

    @SuppressWarnings("unused")
    public void setNorthLatitude(final double northLatitude) {
        this.northLatitude = northLatitude;
    }

    public double getEastLongitude() {
        return eastLongitude;
    }

    public double getSouthLatitude() {
        return southLatitude;
    }

    @SuppressWarnings("unused")
    public void setSouthLatitude(final double southLatitude) {
        this.southLatitude = southLatitude;
    }

    @SuppressWarnings("unused")
    public void setEastLongitude(final double eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    @SuppressWarnings("unused")
    public void setWestLongitude(final double westLongitude) {
        this.westLongitude = westLongitude;
    }
}

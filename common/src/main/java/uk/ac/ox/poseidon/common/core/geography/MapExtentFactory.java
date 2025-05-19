/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.poseidon.common.api.ComponentSupplier;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class MapExtentFactory extends ComponentSupplier<MapExtent> {

    private IntegerParameter gridWidth;
    private IntegerParameter gridHeight;
    private IntegerParameter westLongitude;
    private IntegerParameter eastLongitude;
    private IntegerParameter southLatitude;
    private IntegerParameter northLatitude;

    public MapExtentFactory(
        final int gridWidth,
        final int gridHeight,
        final int westLongitude,
        final int eastLongitude,
        final int southLatitude,
        final int northLatitude
    ) {
        this.gridWidth = new IntegerParameter(gridWidth);
        this.gridHeight = new IntegerParameter(gridHeight);
        this.westLongitude = new IntegerParameter(westLongitude);
        this.eastLongitude = new IntegerParameter(eastLongitude);
        this.southLatitude = new IntegerParameter(southLatitude);
        this.northLatitude = new IntegerParameter(northLatitude);
    }

    public MapExtentFactory(
        final IntegerParameter gridWidth,
        final IntegerParameter gridHeight,
        final IntegerParameter westLongitude,
        final IntegerParameter eastLongitude,
        final IntegerParameter southLatitude,
        final IntegerParameter northLatitude
    ) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.westLongitude = westLongitude;
        this.eastLongitude = eastLongitude;
        this.southLatitude = southLatitude;
        this.northLatitude = northLatitude;
    }

    public MapExtentFactory() {

    }

    public IntegerParameter getWestLongitude() {
        return westLongitude;
    }

    public void setWestLongitude(final IntegerParameter westLongitude) {
        this.westLongitude = westLongitude;
    }

    public IntegerParameter getEastLongitude() {
        return eastLongitude;
    }

    public void setEastLongitude(final IntegerParameter eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    public IntegerParameter getSouthLatitude() {
        return southLatitude;
    }

    public void setSouthLatitude(final IntegerParameter southLatitude) {
        this.southLatitude = southLatitude;
    }

    public IntegerParameter getNorthLatitude() {
        return northLatitude;
    }

    public void setNorthLatitude(final IntegerParameter northLatitude) {
        this.northLatitude = northLatitude;
    }

    public IntegerParameter getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(final IntegerParameter gridWidth) {
        this.gridWidth = gridWidth;
    }

    public IntegerParameter getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(final IntegerParameter gridHeight) {
        this.gridHeight = gridHeight;
    }

    @Override
    public MapExtent get() {
        return MapExtent.from(
            gridWidth.getValue(),
            gridHeight.getValue(),
            new Envelope(
                westLongitude.getValue(),
                eastLongitude.getValue(),
                southLatitude.getValue(),
                northLatitude.getValue()
            )
        );
    }
}


/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography;

/**
 * Simple grid distance. Pythagorean
 */
public class CartesianDistance extends BaseDistance {


    final private double gridCellSizeInKm;

    public CartesianDistance(double gridCellSizeInKm) {
        this.gridCellSizeInKm = gridCellSizeInKm;
    }

    /**
     * the distance (in km) between the cell at (startXGrid,startYGrid) and the cell at (endXGrid,endYGrid)
     *
     * @param startXGrid the starting x grid coordinate
     * @param startYGrid the starting y grid coordinate
     * @param endXGrid   the ending x grid coordinate
     * @param endYGrid   the ending y grid coordinate
     * @return kilometers between the two points
     */
    @Override
    public double distance(int startXGrid, int startYGrid, int endXGrid, int endYGrid) {
        return gridCellSizeInKm * Math.sqrt(Math.pow(endXGrid - startXGrid, 2) + Math.pow(endYGrid - startYGrid, 2));
    }
}

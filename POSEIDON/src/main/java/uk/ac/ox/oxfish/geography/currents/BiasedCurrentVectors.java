/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.geography.currents;

import sim.util.Double2D;
import sim.util.Int2D;


/**
 * in an horizontal strip of the area defined by gridYMinimum and gridYMaximum, the current is whatever the delegate says
 * plus biasX,biasY;
 * otherwise just return what the delegate says
 */
public class BiasedCurrentVectors implements CurrentVectors {

    private final CurrentVectors delegate;

    private final double biasY;

    private final double biasX;

    private final int gridYMinimum;

    private final int gridYMaximum;


    public BiasedCurrentVectors(
        CurrentVectors delegate, double biasY, double biasX, int gridYMinimum, int gridYMaximum
    ) {
        this.delegate = delegate;
        this.biasY = biasY;
        this.biasX = biasX;
        this.gridYMinimum = gridYMinimum;
        this.gridYMaximum = gridYMaximum;
    }

    @Override
    public int getGridHeight() {
        return delegate.getGridHeight();
    }

    @Override
    public int getGridWidth() {
        return delegate.getGridWidth();
    }

    @Override
    public Double2D getVector(int step, Int2D location) {
        Double2D vector = delegate.getVector(step, location);
        if (location.y >= gridYMinimum && location.y <= gridYMaximum)
            return new Double2D(vector.x + biasX, vector.y + biasY);
        return vector;
    }

    public double getBiasY() {
        return biasY;
    }

    public double getBiasX() {
        return biasX;
    }

    public int getGridYMinimum() {
        return gridYMinimum;
    }

    public int getGridYMaximum() {
        return gridYMaximum;
    }
}

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

package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.Collection;
import java.util.List;

public class TwoByTwoRegionalDivision extends RegionalDivision {

    private final List<Region> regions;

    public TwoByTwoRegionalDivision(
        final Coordinate middleCoordinate,
        final MapExtent mapExtent
    ) {
        this(mapExtent.coordinateToXY(middleCoordinate), mapExtent);
    }

    public TwoByTwoRegionalDivision(
        final Double2D middleGridXY,
        final MapExtent mapExtent
    ) {
        super(mapExtent);
        final int w = mapExtent.getGridWidth();
        final int h = mapExtent.getGridHeight();
        final int x = (int) middleGridXY.x;
        final int y = (int) middleGridXY.y;
        this.regions = ImmutableList.of(
            new Region("Northwest", 0, x, 0, y),
            new Region("Northeast", x + 1, w, 0, y),
            new Region("Southwest", 0, x, y + 1, h),
            new Region("Southeast", x + 1, w, y + 1, h)
        );
    }

    @Override
    public Collection<Region> getRegions() {
        return regions;
    }

}

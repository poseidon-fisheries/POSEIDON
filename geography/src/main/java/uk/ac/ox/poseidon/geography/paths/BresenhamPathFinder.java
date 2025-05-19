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

package uk.ac.ox.poseidon.geography.paths;

import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.google.common.collect.ImmutableList;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.Optional;

public class BresenhamPathFinder extends AbstractGridPathFinder {

    public BresenhamPathFinder(
        final BathymetricGrid bathymetricGrid,
        final PortGrid portGrid
    ) {
        super(bathymetricGrid, portGrid);
    }

    @Override
    public Optional<ImmutableList<Int2D>> getPath(
        final Int2D start,
        final Int2D end
    ) {
        if (!(isNavigable(start) && isNavigable(end))) return Optional.empty();
        final Array<GridPoint2> linePoints = new Bresenham2().line(start.x, start.y, end.x, end.y);
        final ImmutableList.Builder<Int2D> pathBuilder = ImmutableList.builder();
        for (final GridPoint2 point : linePoints) {
            final Int2D cell = new Int2D(point.x, point.y);
            // break early if we bump into land
            if (!isNavigable(cell)) return Optional.empty();
            pathBuilder.add(cell);
        }
        return Optional.of(pathBuilder.build());
    }

}

/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;

public abstract class RegionalDivision {

    private final MapExtent mapExtent;
    private final CachePerTile<Region> cache;

    public RegionalDivision(final MapExtent mapExtent) {
        this.mapExtent = mapExtent;
        this.cache = new CachePerTile<>(
            mapExtent,
            gridXY ->
                getRegions()
                    .stream()
                    .filter(region -> region.contains(gridXY))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Grid point (" + mapExtent.getCoordinates(gridXY) + ", " +
                            gridXY + ") not in any defined regions."
                    ))
        );
    }

    public abstract Collection<Region> getRegions();

    public Region getRegion(final SeaTile seaTile) {
        return cache.get(seaTile.getGridLocation());
    }

    public Region getRegion(final Coordinate coordinate) {
        return getRegion(getMapExtent().coordinateToXY(coordinate));
    }

    public Region getRegion(final Double2D gridXY) {
        return getRegion(new Int2D((int) gridXY.x, (int) gridXY.y));
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public Region getRegion(final Int2D gridXY) {
        return cache.get(gridXY);
    }

    public static class Region {

        private final String name;
        private final int minX;
        private final int maxX;
        private final int minY;
        private final int maxY;

        public Region(
            final String name,
            final int minX,
            final int maxX,
            final int minY,
            final int maxY
        ) {
            this.name = name;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        @SuppressWarnings("unused")
        public int getMinX() {
            return minX;
        }

        @SuppressWarnings("unused")
        public int getMaxX() {
            return maxX;
        }

        @SuppressWarnings("unused")
        public int getMinY() {
            return minY;
        }

        @SuppressWarnings("unused")
        public int getMaxY() {
            return maxY;
        }

        @Override
        public String toString() {
            return getName();
        }

        public String getName() {
            return name;
        }

        public boolean contains(final Int2D gridXY) {
            return contains(gridXY.x, gridXY.y);
        }

        public boolean contains(final int gridX, final int gridY) {
            return gridX >= minX & gridX <= maxX & gridY >= minY & gridY <= maxY;
        }

        public boolean contains(final Coordinate coordinate, final MapExtent mapExtent) {
            return contains(mapExtent.coordinateToXY(coordinate));
        }

        public boolean contains(final Double2D gridXY) {
            return contains((int) gridXY.x, (int) gridXY.y);
        }
    }

}
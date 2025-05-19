/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

import sim.util.Int2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.floor;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.IntStream.range;

abstract class SquareGridRegionalDivision extends RegionalDivision {

    private final int numberOfDivisions;
    private final List<Region> regions;

    SquareGridRegionalDivision(
        final MapExtent mapExtent,
        final int numberOfDivisions,
        final List<String> regionNames
    ) {
        super(mapExtent);
        final int numberOfRegions = numberOfDivisions * numberOfDivisions;
        checkArgument(regionNames.size() == numberOfRegions);
        this.numberOfDivisions = numberOfDivisions;

        // This is not the most efficient implementation, but it was an easy way of reusing the logic that
        // we had before the regional divisions refactoring, and shouldn't be noticeable anyway...
        // noinspection OptionalGetWithoutIsPresent
        this.regions =
            range(0, mapExtent.getGridWidth())
                .boxed()
                .flatMap(x -> range(0, mapExtent.getGridHeight()).mapToObj(y -> new Int2D(x, y)))
                .collect(groupingBy(this::getRegionIndex))
                .entrySet()
                .stream()
                .map(entry ->
                    new Region(
                        regionNames.get(entry.getKey()),
                        entry.getValue().stream().mapToInt(Int2D::getX).min().getAsInt(),
                        entry.getValue().stream().mapToInt(Int2D::getX).max().getAsInt(),
                        entry.getValue().stream().mapToInt(Int2D::getY).min().getAsInt(),
                        entry.getValue().stream().mapToInt(Int2D::getY).max().getAsInt()
                    )
                )
                .collect(toImmutableList());

    }

    private int getRegionIndex(final Int2D gridXY) {
        final int n = numberOfDivisions;
        final double regionWidth = (double) getMapExtent().getGridWidth() / n;
        final double regionHeight = (double) getMapExtent().getGridHeight() / n;
        final int x = (int) floor(gridXY.x / regionWidth);
        final int y = (int) floor(gridXY.y / regionHeight);
        return (x + y * n);
    }

    @Override
    public Collection<Region> getRegions() {
        return regions;
    }

}

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

import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.floor;

abstract class SquareGridRegionalDivision implements RegionalDivision {

    private final NauticalMap map;
    private final int numberOfDivisions;
    private final List<Region> regions;

    @SuppressWarnings("SameParameterValue") SquareGridRegionalDivision(
        NauticalMap map,
        int numberOfDivisions,
        List<String> regionNames
    ) {
        final int numberOfRegions = numberOfDivisions * numberOfDivisions;
        checkArgument(regionNames.size() == numberOfRegions);
        this.regions = IntStream
            .range(0, numberOfRegions)
            .mapToObj(n -> new Region(n, regionNames.get(n)))
            .collect(toImmutableList());
        this.map = map;
        this.numberOfDivisions = numberOfDivisions;
    }

    @Override
    public MapExtent getMapExtent() {
        return map.getMapExtent();
    }

    @Override public Collection<Region> getRegions() { return regions; }

    @Override public Region getRegion(int gridX, int gridY) {
        final int n = numberOfDivisions;
        final double regionWidth = (double) map.getWidth() / n;
        final double regionHeight = (double) map.getHeight() / n;
        final int x = (int) floor(gridX / regionWidth);
        final int y = (int) floor(gridY / regionHeight);
        return regions.get(x + y * n);
    }

}

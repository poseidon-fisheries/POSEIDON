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

import com.google.common.collect.ImmutableList;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Common interface for all distance measures over a nautical chart
 * Created by carrknight on 4/10/15.
 */
public interface Distance {

    default List<Entry<SeaTile, Double>> cumulativeTravelTimeAlongRouteInHours(
        Deque<SeaTile> route,
        NauticalMap map,
        double speedInKph
    ) {
        return cumulativeDistanceAlongRouteInKm(route, map).stream()
            .map(pair -> entry(pair.getKey(), pair.getValue() / speedInKph))
            .collect(toImmutableList());
    }

    default List<Entry<SeaTile, Double>> cumulativeDistanceAlongRouteInKm(
        Deque<SeaTile> route,
        NauticalMap map
    ) {
        checkArgument(!route.isEmpty());
        double cumulativeDistance = 0.0;
        final ImmutableList.Builder<Entry<SeaTile, Double>> builder = ImmutableList.builder();
        SeaTile start = route.peek();
        final Iterator<SeaTile> iterator = route.iterator();
        do {
            SeaTile end = iterator.next();
            cumulativeDistance += distance(start, end, map);
            builder.add(entry(end, cumulativeDistance));
            start = end;
        } while (iterator.hasNext());
        return builder.build();
    }

    /**
     * the distance between two sea-tiles
     *
     * @param start starting sea-tile
     * @param end   ending sea-tile
     * @param map   the nautical map
     * @return kilometers between the two
     */
    double distance(SeaTile start, SeaTile end, NauticalMap map);

}

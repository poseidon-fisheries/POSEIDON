/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.geography;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.zip;

/**
 * Common interface for all distance measures over a nautical chart
 * Created by carrknight on 4/10/15.
 */
public interface Distance {

    /**
     * the distance between two sea-tiles
     *
     * @param start starting sea-tile
     * @param end   ending sea-tile
     * @param map   the nautical map
     * @return kilometers between the two
     */
    double distance(SeaTile start, SeaTile end, NauticalMap map);

    default ImmutableList<Pair<SeaTile, Double>> segmentLengthsAlongRoute(Collection<SeaTile> route, NauticalMap map) {
        return zip(
            Stream.concat(route.stream().limit(1), route.stream()), // duplicate first tile, so first distance will be 0
            route.stream(),
            (start, end) -> new Pair<>(end, distance(start, end, map))
        ).collect(toImmutableList());
    }

    default ImmutableList<Pair<SeaTile, Double>> cumulativeDistanceAlongRoute(Collection<SeaTile> route, NauticalMap map) {
        double cumulativeDistance = 0.0;
        final ImmutableList.Builder<Pair<SeaTile, Double>> builder = new ImmutableList.Builder<>();
        for (Pair<SeaTile, Double> pair : segmentLengthsAlongRoute(route, map)) {
            cumulativeDistance += pair.getSecond();
            builder.add(new Pair<>(pair.getFirst(), cumulativeDistance));
        }
        return builder.build();
    }

    /**
     * Return the distance along a route of sea tiles
     *
     * @param route the route along which to calculate the distance
     * @param map   the nautical map
     * @return the total distance, in kilometres, along the sea tiles on the route
     */
    default double totalRouteDistance(Collection<SeaTile> route, NauticalMap map) {
        return segmentLengthsAlongRoute(route, map).stream().mapToDouble(Pair::getSecond).sum();
    }

}

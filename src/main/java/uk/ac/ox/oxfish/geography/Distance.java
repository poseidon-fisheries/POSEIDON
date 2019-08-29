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

import java.util.Collection;

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

    /**
     * Return the distance along a path of sea tiles
     *
     * @param path the path along which to calculate the distance
     * @param map  the nautical map
     * @return the total distance, in kilometers, along the sea tiles on the path
     */
    default double distanceAlongPath(Collection<SeaTile> path, NauticalMap map) {
        return zip(path.stream(), path.stream().skip(1), (start, end) -> distance(start, end, map))
            .mapToDouble(Double::doubleValue)
            .sum();
    }

}

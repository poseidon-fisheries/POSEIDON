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

/**
 * Common interface for all distance measures over a nautical chart
 * Created by carrknight on 4/10/15.
 */
public interface Distance
{

    /**
     * the distance between two sea-tiles
     * @param start starting sea-tile
     * @param end ending sea-tile
     * @param map
     * @return kilometers between the two
     */
    double distance(SeaTile start, SeaTile end, NauticalMap map);




}

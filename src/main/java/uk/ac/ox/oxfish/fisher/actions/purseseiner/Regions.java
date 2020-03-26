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

package uk.ac.ox.oxfish.fisher.actions.purseseiner;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;

import static java.lang.Math.floor;

public final class Regions {

    public static final Map<Integer, String> REGION_NAMES = ImmutableMap.<Integer, String>builder()
        .put(11, "Northwest")
        .put(21, "North")
        .put(31, "Northeast")
        .put(12, "West")
        .put(22, "Central")
        .put(32, "East")
        .put(13, "Southwest")
        .put(23, "South")
        .put(33, "Southeast")
        .build();

    private Regions() {} // prevent instantiation

    public static int getRegionNumber(NauticalMap map, SeaTile seaTile) {
        final double divisions = 3.0;
        final double regionWidth = map.getWidth() / divisions;
        final double regionHeight = map.getHeight() / divisions;
        return (int) ((1 + floor(seaTile.getGridX() / regionWidth)) * 10) +
            (int) (1 + floor(seaTile.getGridY() / regionHeight));
    }

}

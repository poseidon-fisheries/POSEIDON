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

package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the map in a set of rectangles, each containing a set of cells
 * Created by carrknight on 11/9/16.
 */
public class SquaresMapDiscretizer extends AbstractMapDiscretizer {

    /**
     * number of ticks on the y axis
     */
    private final int ySplits;

    /**
     * number of ticks on the x axis
     */
    private final int xSplits;


    public SquaresMapDiscretizer(final int ySplits, final int xSplits) {
        this.ySplits = ySplits;
        this.xSplits = xSplits;
    }

    /**
     * return groups but only for seatiles in the tiles list (which is all the seatiles we consider valid)
     *
     * @param map           the nautical map
     * @param validSeatiles the list of valid seatiles
     * @return groups
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<SeaTile>[] discretize(final NauticalMap map, final List<SeaTile> validSeatiles) {
        final List<SeaTile>[] groups = new List[(ySplits + 1) * (xSplits + 1)];
        for (int i = 0; i < groups.length; i++)
            groups[i] = new ArrayList<>();


        //start splitting
        final int groupWidth = (int) Math.ceil(map.getWidth() / (xSplits + 1d));
        final int groupHeight = (int) Math.ceil(map.getHeight() / (ySplits + 1d));
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++) {
                //integer division is what we want here!
                final int height = y / groupHeight;
                final int width = x / groupWidth;
                final int group = height * (xSplits + 1) + width;
                final SeaTile tile = map.getSeaTile(x, y);
                if (validSeatiles.contains(tile))
                    groups[group].add(tile);
            }

        return groups;
    }
}

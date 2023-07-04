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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.List;

/**
 * 1 to 1 discretizer, that is each tile is in its own group
 * Created by carrknight on 2/6/17.
 */
public class IdentityDiscretizer extends AbstractMapDiscretizer {


    /**
     * return groups but only for seatiles in the tiles list (which is all the seatiles we consider valid)
     *
     * @param map   the nautical map
     * @param tiles the list of valid seatiles
     * @return groups
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<SeaTile>[] discretize(final NauticalMap map, final List<SeaTile> tiles) {

        tiles.sort((o1, o2) -> {
            //sort by x, and if that fails by y
            final int x = Integer.compare(o1.getGridX(), o2.getGridX());
            if (x != 0)
                return x;
            else
                return Integer.compare(o1.getGridY(), o2.getGridY());

        });

        final List<SeaTile>[] groups = new List[tiles.size()];

        for (int i = 0; i < tiles.size(); i++) {
            groups[i] = ImmutableList.of(tiles.get(i));
        }


        return groups;
    }


}

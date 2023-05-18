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

package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Created by carrknight on 6/27/16.
 */
public interface MarkovBelief {


    /**
     * returns the belief at this location
     *
     * @return the belief (a number)
     */
    public double getBelief(int x, int y);

    /**
     * returns the belief on this tile
     *
     * @param tile tile
     * @return the belief (a number)
     */
    public double getBelief(SeaTile tile);

    /**
     * returns the whole grid. Probably not safe for modification
     */
    public DoubleGrid2D representBeliefs();
}

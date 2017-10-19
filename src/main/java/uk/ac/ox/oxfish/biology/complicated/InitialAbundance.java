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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;

/**
 * Just a renaming of a matrix[2][maxAge+1] representing
 * the initial abundance of fish
 * Created by carrknight on 7/8/17.
 */
public class InitialAbundance {


    private final int[][] abundance;


    public InitialAbundance(int[][] abundance) {
        this.abundance = abundance;
        Preconditions.checkArgument(abundance.length == 2); //male and female!
    }

    /**
     * Getter for property 'abundance'.
     *
     * @return Value for property 'abundance'.
     */
    public int[][] getAbundance() {
        return abundance;
    }
}

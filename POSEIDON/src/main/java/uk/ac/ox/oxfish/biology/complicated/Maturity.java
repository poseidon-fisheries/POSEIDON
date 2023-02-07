/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.biology.Species;

/**
 * computes maturity value for fish
 */
public interface Maturity {

    /**
     * computes the maturity % (any number between 0 and 1) of fish
     * @param species the species of fish
     * @param subdivision its subdivision (usually by sex but often there are no subdivisions)
     * @param bin the bin (usually age or length)
     * @return a number between 0 and 1
     */
    public double getMaturity(Species species,
                              int subdivision,
                              int bin);

}

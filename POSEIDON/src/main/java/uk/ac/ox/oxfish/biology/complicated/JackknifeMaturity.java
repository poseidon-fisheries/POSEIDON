/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;

/**
 * very easy maturity scheme: above "lengthAtMaturity" maturity is 1, below is 0
 */
public class JackknifeMaturity implements Maturity {

    private final double lengthAtMaturity;

    public JackknifeMaturity(double lengthAtMaturity) {
        this.lengthAtMaturity = lengthAtMaturity;
    }

    /**
     * computes the maturity % (any number between 0 and 1) of fish
     *
     * @param species     the species of fish
     * @param subdivision its subdivision (usually by sex but often there are no subdivisions)
     * @param bin         the bin (usually age or length)
     * @return a number between 0 and 1
     */
    @Override
    public double getMaturity(Species species, int subdivision, int bin) {
        return species.getLength(subdivision, bin) >= lengthAtMaturity ? 1d : 0d;
    }
}

/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;

/**
 * the initial abundance is just a fixed amount in a specific bin
 */
public class OneBinInitialAbundance implements InitialAbundance {

    /**
     * which bin to fill
     */
    private final int bin;

    /**
     * how much to fill the initial bin by
     */
    private final double initialAbundance;

    /**
     * the cohort to fill (<0 means all of them)
     */
    private final int subdivision;

    private Species species;

    public OneBinInitialAbundance(int bin, double initialAbundance, int subdivision) {
        Preconditions.checkArgument(bin >= 0);
        Preconditions.checkArgument(initialAbundance >= 0);
        this.bin = bin;
        this.initialAbundance = initialAbundance;
        this.subdivision = subdivision;
    }

    /**
     * called before being asked for initial abundance
     *
     * @param species
     */
    @Override
    public void initialize(Species species) {
        Preconditions.checkState(this.species == null);
        this.species = species;
    }

    /**
     * returns the abundance matrix; call after initialize()
     *
     * @return
     */
    @Override
    public double[][] getInitialAbundance() {


        double[][] abundance = new double[species.getNumberOfSubdivisions()]
            [species.getNumberOfBins()];
        if (subdivision < 0) {
            for (int i = 0; i < species.getNumberOfSubdivisions(); i++) {
                abundance[i][bin] = initialAbundance;
            }
        } else {
            abundance[subdivision][bin] = initialAbundance;
        }
        return abundance;
    }
}

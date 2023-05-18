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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;

import java.util.Arrays;

/**
 * another simple initial abundance, in this case it is supplied an array and will copy it
 * for each subdivision
 */
public class RepeatingInitialAbundance implements InitialAbundance {


    private final double[] cohort;


    private double[][] abundance;

    public RepeatingInitialAbundance(double[] cohort) {
        this.cohort = cohort;
    }

    /**
     * called before being asked for initial abundance
     *
     * @param species
     */
    @Override
    public void initialize(Species species) {
        abundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        Preconditions.checkArgument(
            species.getNumberOfBins() == cohort.length,
            "The number of boxes provided as inputs don't match the number of boxes the biology initializer expects! " + species.getNumberOfBins() + "," + cohort.length
        );

        for (int i = 0; i < species.getNumberOfSubdivisions(); i++)
            abundance[i] = Arrays.copyOf(cohort, cohort.length);
    }

    /**
     * returns the abundance matrix; call after initialize()
     *
     * @return
     */
    @Override
    public double[][] getInitialAbundance() {
        return abundance;
    }

    @VisibleForTesting
    public double[] peekCohort() {
        return cohort;
    }
}

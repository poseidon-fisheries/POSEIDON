/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;

public class CatchAtLength {

    private final double[] catchAtLength;

    private final double totalCount;


    public CatchAtLength(
        StructuredAbundance abundance,
        Species species, double lengthBinCm, int numberOfBins
    ) {
        int currentCount = 0;
        catchAtLength = new double[numberOfBins];
        for (int bin = 0; bin < abundance.getBins(); bin++) {
            for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++) {

                double abundanceHere = abundance.getAbundance(subdivision, bin);
                if (Double.isFinite(abundanceHere) && abundanceHere > 0) {
                    final double lengthHere = species.getLength(subdivision, bin);
                    int countBin = (int) Math.floor((lengthHere) / lengthBinCm);
                    if (countBin >= catchAtLength.length) {
                        //we could be using a bad or simplified lengthInfinity
                        // assert species.getLength(subdivision, bin) >= lengthInfinity;
                        countBin = catchAtLength.length - 1;
                    }
                    catchAtLength[countBin] +=
                        abundanceHere;
                    currentCount += abundanceHere;
                }

            }
        }
        totalCount = currentCount;
    }


    public double[] getCatchAtLength() {
        return catchAtLength;
    }

    public double getTotalCount() {
        return totalCount;
    }
}

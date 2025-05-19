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

public class LbSPRFormula implements SPRFormula {
    @Override
    public double computeSPR(SPRAgent sprAgent, StructuredAbundance abundance) {

        final Species species = sprAgent.getSpecies();

        double maxLength = species.getLengthAtAge(Integer.MAX_VALUE, 0);
        final double binWidthInCm = sprAgent.getAssumedLengthBinCm();
        int bins = (int) Math.ceil(maxLength / binWidthInCm) + 1;
        CatchAtLength catchAtLength = new CatchAtLength(
            abundance,
            species,
            binWidthInCm,
            bins
        );

        final double[] catchAtLengthArray = catchAtLength.getCatchAtLength();
        double binMids[] = new double[catchAtLengthArray.length];
        double maturityPerBin[] = new double[catchAtLengthArray.length];
        //in the LBSPR computations I have seen maturity is 0.5 in the bin where length at maturity actually occurs
        double maturityLength = sprAgent.getAssumedLenghtAtMaturity();
        for (int bin = 0; bin < catchAtLengthArray.length; bin++) {
            final double edge = bin * binWidthInCm;
            binMids[bin] = edge + binWidthInCm / 2;
            if (maturityLength > edge) {
                if (maturityLength < edge + binWidthInCm)
                    maturityPerBin[bin] = 0.5;
                else
                    maturityPerBin[bin] = 0;
            } else {
                maturityPerBin[bin] = 1;

            }
        }

        return LbSprEstimation.computeSPR(
            catchAtLengthArray,
            sprAgent.getAssumedLinf(),
            .1,
            binMids,
            sprAgent.getAssumedNaturalMortality() / sprAgent.getAssumedKParameter(),
            maturityPerBin,
            sprAgent.getAssumedVarB()

        ).getSpr();

    }
}

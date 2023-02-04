package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;

public class LbSPRFormula implements SPRFormula {
    @Override
    public double computeSPR(SPRAgent sprAgent, StructuredAbundance abundance) {

        final Species species = sprAgent.getSpecies();

        double maxLength =  species.getLengthAtAge(Integer.MAX_VALUE,0);
        final double binWidthInCm = sprAgent.getAssumedLengthBinCm();
        int bins =  (int)Math.ceil(maxLength / binWidthInCm) + 1;
        CatchAtLength catchAtLength = new CatchAtLength(abundance,
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
            binMids[bin] = edge + binWidthInCm/2;
            if(maturityLength>edge) {
                if (maturityLength < edge + binWidthInCm)
                    maturityPerBin[bin] = 0.5;
                else
                    maturityPerBin[bin] = 0;
            }
            else {
                    maturityPerBin[bin] = 1;

            }
        }

        return LbSprEstimation.computeSPR(
                catchAtLengthArray,
                sprAgent.getAssumedLinf(),
                .1,
                binMids,
                sprAgent.getAssumedNaturalMortality()/sprAgent.getAssumedKParameter(),
                maturityPerBin,
                sprAgent.getAssumedVarB()

                ).getSpr();

    }
}

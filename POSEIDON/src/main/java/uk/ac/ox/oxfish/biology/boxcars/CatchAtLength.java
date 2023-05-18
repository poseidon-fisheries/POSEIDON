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

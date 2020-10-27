package uk.ac.ox.oxfish.biology.boxcars;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.checkerframework.checker.units.qual.A;

import java.util.Arrays;

/**
 * class to compute Length-based SPR the way Hordyk does it
 */
public class LBSPR {


    /**
     * @param binMids vector including the mid poitns of all bins
     * @param mkRatio the ratio between natural mortality and growth coefficnet
     * @param Linf length at infinity
     * @param coefficientVariationLinf coefficient of variation assumed for Linf
     * @param maximumAge the maximum age we want to build the key for
     * @return two object: the age-->key matrix and the relative length at age vector
     */
    static public final AgeToLength buildAgeToLengthKey(
            double[] binMids,
            double mkRatio,
            double Linf,
            double coefficientVariationLinf,
            int maximumAge
    ){
        //this is the mystical "Prob" matrix in LBSPR_ in the DLM toolkit
        double ageToLengthKey[][] = new double[maximumAge+1][binMids.length];


        double[] relativeLengthAtAge = new double[maximumAge+1];
        for (int age = 0; age < maximumAge+1; age++) {
            double xs =  age/((double)maximumAge);
            relativeLengthAtAge[age] = 1 - Math.pow(0.01,xs/mkRatio);
            final double mean = relativeLengthAtAge[age] * Linf;
            final double sd = mean * coefficientVariationLinf;

            if(sd > 0) {
                NormalDistribution density =  new NormalDistribution(
                        mean,
                        sd
                ) ;
                double limit = density.density(
                        mean + sd * 2.5
                );
                double sum = 0;
                for (int lengthIndex = 0; lengthIndex < ageToLengthKey[age].length; lengthIndex++) {
                    final double densityAtThisLengthBin = density.density(binMids[lengthIndex]);
                    ageToLengthKey[age][lengthIndex] = densityAtThisLengthBin < limit ? 0 : densityAtThisLengthBin;
                    sum += ageToLengthKey[age][lengthIndex];

                }
                //now normalize to 1!
                if(sum>0)
                    for (int lengthIndex = 0; lengthIndex < ageToLengthKey[age].length; lengthIndex++) {
                        ageToLengthKey[age][lengthIndex] = ageToLengthKey[age][lengthIndex] / sum;

                    }
            }
            else
                Arrays.fill(ageToLengthKey[age],0d);

        }

        return new AgeToLength(ageToLengthKey,relativeLengthAtAge);

    }


    public static class AgeToLength{

        private final double ageToLengthKey[][];

        private final double relativeLengthAtAge[];

        public AgeToLength(double[][] ageToLengthKey, double[] relativeLengthAtAge) {
            this.ageToLengthKey = ageToLengthKey;
            this.relativeLengthAtAge = relativeLengthAtAge;
        }

        public double[][] getAgeToLengthKey() {
            return ageToLengthKey;
        }

        public double[] getRelativeLengthAtAge() {
            return relativeLengthAtAge;
        }
    }

}

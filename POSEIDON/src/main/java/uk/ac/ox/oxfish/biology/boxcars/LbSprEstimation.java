package uk.ac.ox.oxfish.biology.boxcars;

import com.google.common.base.MoreObjects;
import eva2.OptimizerFactory;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.operator.terminators.CombinedTerminator;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.operator.terminators.FitnessConvergenceTerminator;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.problems.SimpleProblemWrapper;
import eva2.problems.simple.SimpleProblemDouble;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * class to compute Length-based SPR the way Hordyk does it (maximize log-likelihood, changing mortality and selectivity)
 */
public class LbSprEstimation extends SimpleProblemDouble {

    private static final long serialVersionUID = 6828223878478770329L;
    private final double[] catchAtLengthObserved;
    private final double Linf;
    private final double coefficientVariationLinf;
    private final double[] binMids;
    private final double mkRatio;
    private final double[] maturityPerBin; //todo check that L50-L95 make sense when it's jacknife
    private final double bVariableLengthToWeightConversion;


    public LbSprEstimation(
        final double[] catchAtLengthObserved, final double linf,
        final double coefficientVariationLinf, final double[] binMids,
        final double mkRatio, final double[] maturityPerBin,
        final double bVariableLengthToWeightConversion
    ) {
        this.catchAtLengthObserved = catchAtLengthObserved;
        Linf = linf;
        this.coefficientVariationLinf = coefficientVariationLinf;
        this.binMids = binMids;
        this.mkRatio = mkRatio;
        this.maturityPerBin = maturityPerBin;
        this.bVariableLengthToWeightConversion = bVariableLengthToWeightConversion;
    }


    public static LBSPREstimate computeSPR(
        final double[] catchAtLengthObserved, final double linf,
        final double coefficientVariationLinf, final double[] binMids,
        final double mkRatio, final double[] maturityPerBin,
        final double bVariableLengthToWeightConversion
    ) {
        final LbSprEstimation problem = new LbSprEstimation(
            catchAtLengthObserved,
            linf,
            coefficientVariationLinf,
            binMids,
            mkRatio,
            maturityPerBin,
            bVariableLengthToWeightConversion
        );

        final SimpleProblemWrapper wrapper = new SimpleProblemWrapper();
        wrapper.setSimpleProblem(problem);
        wrapper.setParallelThreads(1);
        wrapper.setDefaultRange(1);
        final NelderMeadSimplex nelderMeadSimplex = NelderMeadSimplex.createNelderMeadSimplex(
            wrapper
            , null);
        nelderMeadSimplex.setCheckRange(false);
        final OptimizationParameters params = OptimizerFactory.makeParams(
            nelderMeadSimplex,
            15, wrapper

        );
        params.setTerminator(new CombinedTerminator(
            new EvaluationTerminator(500),
            new FitnessConvergenceTerminator(),
            false
        ));
        //kill the error printer which EVA abuses
        final PrintStream _err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(final int b) {
            }
        }));

        final double[] bestMultiplier = OptimizerFactory.optimizeToDouble(
            params
        );
        System.setErr(_err);
//        System.out.println("best is: " +
//                        Arrays.toString(problem.evaluate(bestMultiplier))
//                );
//        System.out.println("theoretical best is: " +
//                Arrays.toString(problem.evaluate(new double[]{
//                        -0.3605028,-0.5001816 ,0.9556854
//                }))
//        );

        final double lengthForSelectivityAt50cm = Math.exp(bestMultiplier[0]) * problem.Linf;
        final double selectivitySlope = Math.exp(bestMultiplier[1]);
        final double lengthForSelectivityAt95cm = lengthForSelectivityAt50cm + lengthForSelectivityAt50cm * selectivitySlope;
        final double fishingMortalityToNaturalMortalityRatio = Math.exp(bestMultiplier[2]);

        final TheoreticalSPR bestSPR = sprFormula(
            lengthForSelectivityAt50cm,
            lengthForSelectivityAt95cm,
            fishingMortalityToNaturalMortalityRatio,
            100,
            linf,
            coefficientVariationLinf,
            binMids,
            mkRatio,
            maturityPerBin,
            bVariableLengthToWeightConversion
        );

//        SL50 <- exp(runOpt$par[1]) * Linf
//        dSL50 <- exp(runOpt$par[2])
//        SL95 <- SL50 + dSL50 * SL50
//        FM <- exp(runOpt$par[3])

        return new LBSPREstimate(
            bestSPR.getSpr(),
            fishingMortalityToNaturalMortalityRatio,
            lengthForSelectivityAt50cm,
            lengthForSelectivityAt95cm,
            problem.evaluate(bestMultiplier)[0]
        );
    }

    //LBSPRgen in the Rcpp file
    static public final TheoreticalSPR sprFormula(
        final double selectivityCmAt50Percent,
        final double selectivityCmAt95Percent,
        final double ratioFishingToNaturalMortality,
        final int maximumAge,
        final double Linf,
        final double coefficientVariationLinf,
        final double[] binMids,
        final double mkRatio,
        final double[] maturityPerBin, //todo check that L50-L95 make sense when it's jacknife
        final double bVariableLengthToWeightConversion
    ) {

        //build age-to-length key
        final AgeToLength ageToLength =
            buildAgeToLengthKey(binMids, mkRatio, Linf, coefficientVariationLinf, maximumAge);

        return sprFormula(
            selectivityCmAt50Percent,
            selectivityCmAt95Percent,
            ratioFishingToNaturalMortality,
            maximumAge,
            binMids,
            mkRatio,
            maturityPerBin,
            bVariableLengthToWeightConversion,
            ageToLength
        );


    }

    @Override
    public double[] evaluate(final double[] x) {


        return
            new double[]{lbsprDistance(
                catchAtLengthObserved,
                x[0],
                x[1],
                x[2],
                100,
                Linf,
                coefficientVariationLinf,
                binMids,
                mkRatio,
                maturityPerBin,
                bVariableLengthToWeightConversion
            )};


    }

    /**
     * @param binMids                  vector including the mid poitns of all bins
     * @param mkRatio                  the ratio between natural mortality and growth coefficnet
     * @param Linf                     length at infinity
     * @param coefficientVariationLinf coefficient of variation assumed for Linf
     * @param maximumAge               the maximum age we want to build the key for
     * @return two object: the age-->key matrix and the relative length at age vector
     */
    static public final AgeToLength buildAgeToLengthKey(
        final double[] binMids,
        final double mkRatio,
        final double Linf,
        final double coefficientVariationLinf,
        final int maximumAge
    ) {
        //this is the mystical "Prob" matrix in LBSPR_ in the DLM toolkit
        final double[][] ageToLengthKey = new double[maximumAge + 1][binMids.length];


        final double[] relativeLengthAtAge = new double[maximumAge + 1];
        for (int age = 0; age < maximumAge + 1; age++) {
            final double xs = age / ((double) maximumAge);
            relativeLengthAtAge[age] = 1 - Math.pow(0.01, xs / mkRatio);
            final double mean = relativeLengthAtAge[age] * Linf;
            final double sd = mean * coefficientVariationLinf;

            if (sd > 0) {
                final NormalDistribution density = new NormalDistribution(
                    mean,
                    sd
                );
                final double limit = density.density(
                    mean + sd * 2.5
                );
                double sum = 0;
                for (int lengthIndex = 0; lengthIndex < ageToLengthKey[age].length; lengthIndex++) {
                    final double densityAtThisLengthBin = density.density(binMids[lengthIndex]);
                    ageToLengthKey[age][lengthIndex] = densityAtThisLengthBin < limit ? 0 : densityAtThisLengthBin;
                    sum += ageToLengthKey[age][lengthIndex];

                }
                //now normalize to 1!
                if (sum > 0)
                    for (int lengthIndex = 0; lengthIndex < ageToLengthKey[age].length; lengthIndex++) {
                        ageToLengthKey[age][lengthIndex] = ageToLengthKey[age][lengthIndex] / sum;

                    }
            } else
                Arrays.fill(ageToLengthKey[age], 0d);

        }

        return new AgeToLength(ageToLengthKey, relativeLengthAtAge);

    }


    //LBSPRgen in the Rcpp file
    static public final TheoreticalSPR sprFormula(
        final double selectivityCmAt50Percent,
        final double selectivityCmAt95Percent,
        final double ratioFishingToNaturalMortality,
        final int maximumAge,
        final double[] binMids,
        final double mkRatio,
        final double[] maturityPerBin, //todo check that L50-L95 make sense when it's jacknife
        final double bVariableLengthToWeightConversion,
        final AgeToLength ageToLength
    ) {
        //logistic selectivity here
        final double[] selectivityAtLength = new double[binMids.length];
        for (int bin = 0; bin < selectivityAtLength.length; bin++) {
            selectivityAtLength[bin] = 1d / (
                1 + Math.exp(-Math.log(19.0) * (binMids[bin] - selectivityCmAt50Percent) / (selectivityCmAt95Percent - selectivityCmAt50Percent))
            );
        }

        final double[][] catchesMatrix = new double[maximumAge + 1][binMids.length];
        final double[] survivorsAtAge = new double[maximumAge + 1]; // this is Ns
        final double[] unfishedNumberAtAge = new double[maximumAge + 1]; //this is N0
        final double[] maturityAtAge = new double[maximumAge + 1]; //this is Ma

        double cumulativeSelexAtAge = 0; //sum(Sx) in the original code
        for (int age = 0; age < catchesMatrix.length; age++) {

            for (int lengthBin = 0; lengthBin < binMids.length; lengthBin++) {
                cumulativeSelexAtAge += selectivityAtLength[lengthBin] * ageToLength.getAgeToLengthKey()[age][lengthBin];
                catchesMatrix[age][lengthBin] = ageToLength.getAgeToLengthKey()[age][lengthBin] * selectivityAtLength[lengthBin];

                maturityAtAge[age] += ageToLength.getAgeToLengthKey()[age][lengthBin] * maturityPerBin[lengthBin];

            }

            final double ratioedSelex = cumulativeSelexAtAge / (age + 1); //MSX
            survivorsAtAge[age] = Math.pow(
                (1 - ageToLength.getRelativeLengthAtAge()[age]),
                mkRatio + (mkRatio * ratioFishingToNaturalMortality) * ratioedSelex
            );
            unfishedNumberAtAge[age] = Math.pow(1 - ageToLength.relativeLengthAtAge[age], mkRatio);
        }

        //compute and normalize catch at length
        final double[] catchAtLength = new double[binMids.length];
        double sum = 0;
        for (int lengthBin = 0; lengthBin < binMids.length; lengthBin++) {
            for (int age = 0; age < catchesMatrix.length; age++) {
                catchAtLength[lengthBin] += survivorsAtAge[age] * catchesMatrix[age][lengthBin];
            }
            sum += catchAtLength[lengthBin];
        }
        for (int i = 0; i < catchAtLength.length; i++) {
            catchAtLength[i] = catchAtLength[i] / sum;
        }


        //SPR
        double fishedEggs = 0;
        double unfishedEggs = 0;
        for (int age = 0; age < ageToLength.getRelativeLengthAtAge().length; age++) {

            assert unfishedNumberAtAge[age] >= survivorsAtAge[age] : age;

            fishedEggs += maturityAtAge[age] * survivorsAtAge[age] * Math.pow(
                ageToLength.relativeLengthAtAge[age],
                bVariableLengthToWeightConversion
            );

            unfishedEggs += maturityAtAge[age] * unfishedNumberAtAge[age] * Math.pow(
                ageToLength.relativeLengthAtAge[age],
                bVariableLengthToWeightConversion
            );

        }

        assert fishedEggs <= unfishedEggs;

        return new TheoreticalSPR(
            fishedEggs / unfishedEggs,
            catchAtLength
        );


    }

    //LBSPRopt in the original cpp code
    static public double lbsprDistance(
        final double[] catchAtLengthObserved,
        //these three are the variables of the model, they are logged because I think it helps with optim in R
        final double logSelectivityCmAt50PercentAsPercentageOfLinf,
        final double logSelectivitySlope,
        final double logRatioFishingToNaturalMortality,
        final int maximumAge,
        final double Linf,
        final double coefficientVariationLinf,
        final double[] binMids,
        final double mkRatio,
        final double[] maturityPerBin, //todo check that L50-L95 make sense when it's jacknife
        final double bVariableLengthToWeightConversion

    ) {
        final AgeToLength ageToLength =
            buildAgeToLengthKey(binMids, mkRatio, Linf, coefficientVariationLinf, maximumAge);

        //rescale to useful
        final double selectivityCmAt50Percent =
            Math.exp(logSelectivityCmAt50PercentAsPercentageOfLinf) * Linf;
        final double selectivityCmAt95Percent = selectivityCmAt50Percent +
            Math.exp(logSelectivitySlope) * selectivityCmAt50Percent;
        final double ratioFishingToNaturalMortality =
            Math.exp(logRatioFishingToNaturalMortality);

        //normalize observed catch at length
        final double[] catchAtLengthObservedNormalized = new double[catchAtLengthObserved.length];
        double totalCatches = 0;
        for (final double caught : catchAtLengthObserved) {
            totalCatches += caught;
        }
        for (int bin = 0; bin < catchAtLengthObservedNormalized.length; bin++) {
            catchAtLengthObservedNormalized[bin] = catchAtLengthObserved[bin] / totalCatches;
        }

        //compute SPR in theory
        final TheoreticalSPR theoreticalSPR = sprFormula(
            selectivityCmAt50Percent,
            selectivityCmAt95Percent,
            ratioFishingToNaturalMortality,
            maximumAge,
            binMids,
            mkRatio,
            maturityPerBin,
            bVariableLengthToWeightConversion,
            ageToLength
        );

        //compare and compute NLL
        double error = 0;
        for (int bin = 0; bin < catchAtLengthObservedNormalized.length; bin++) {
            if (catchAtLengthObservedNormalized[bin] > 0 & theoreticalSPR.getCatchesAtLength()[bin] > 0) {
                error += catchAtLengthObserved[bin] * Math.log(theoreticalSPR.getCatchesAtLength()[bin] / catchAtLengthObservedNormalized[bin]);
            }
        }

        //need to penalize for selectivity
        double penalization = 0;
        final double penalizationValue = error;
        /*
          double Pen=0;
  double PenVal=NLL;
  Pen=R::dbeta(exp(pars(0)), 5.0, 0.1,0) * PenVal;
  if (exp(pars(0)) >= 1) Pen=PenVal*exp(pars(0));
  NLL = NLL + Pen;
         */
        final BetaDistribution distribution = new BetaDistribution(5, 0.1);
        final double expPar0 = Math.exp(logSelectivityCmAt50PercentAsPercentageOfLinf);
        if (expPar0 >= 1)
            penalization = penalizationValue * expPar0;
        else
            penalization = distribution.density(expPar0) * penalizationValue;

        error = error + penalization;

        return -error;


    }

    @Override
    public int getProblemDimension() {
        return 3;
    }

    public static class TheoreticalSPR {

        private final double spr;

        private final double[] catchesAtLength;


        public TheoreticalSPR(final double spr, final double[] catchesAtLength) {
            this.spr = spr;
            this.catchesAtLength = catchesAtLength;
        }

        public double getSpr() {
            return spr;
        }

        public double[] getCatchesAtLength() {
            return catchesAtLength;
        }
    }

    public static class AgeToLength {

        private final double[][] ageToLengthKey;

        private final double[] relativeLengthAtAge;

        public AgeToLength(final double[][] ageToLengthKey, final double[] relativeLengthAtAge) {
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


    public static class LBSPREstimate {

        private final double spr;

        private final double fishingToNaturalMortalityRatio;

        private final double lengthAt50PercentSelectivity;

        private final double lengthAt95PercentSelectivity;

        private final double likelihood;


        public LBSPREstimate(
            final double spr,
            final double fishingToNaturalMortalityRatio,
            final double lengthAt50PercentSelectivity,
            final double lengthAt95PercentSelectivity,
            final double likelihood
        ) {
            this.spr = spr;
            this.fishingToNaturalMortalityRatio = fishingToNaturalMortalityRatio;
            this.lengthAt50PercentSelectivity = lengthAt50PercentSelectivity;
            this.lengthAt95PercentSelectivity = lengthAt95PercentSelectivity;
            this.likelihood = likelihood;
        }


        public double getSpr() {
            return spr;
        }

        public double getFishingToNaturalMortalityRatio() {
            return fishingToNaturalMortalityRatio;
        }

        public double getLengthAt50PercentSelectivity() {
            return lengthAt50PercentSelectivity;
        }

        public double getLengthAt95PercentSelectivity() {
            return lengthAt95PercentSelectivity;
        }

        public double getLikelihood() {
            return likelihood;
        }


        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("spr", spr)
                .add("fishingToNaturalMortalityRatio", fishingToNaturalMortalityRatio)
                .add("lengthAt50PercentSelectivity", lengthAt50PercentSelectivity)
                .add("lengthAt95PercentSelectivity", lengthAt95PercentSelectivity)
                .add("likelihood", likelihood)
                .toString();
        }
    }

}

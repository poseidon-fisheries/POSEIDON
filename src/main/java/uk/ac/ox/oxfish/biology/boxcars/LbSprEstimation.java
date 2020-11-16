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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * class to compute Length-based SPR the way Hordyk does it (maximize log-likelihood, changing mortality and selectivity)
 */
public class LbSprEstimation extends SimpleProblemDouble{

    private final double[] catchAtLengthObserved;
    private final double Linf;
    private final double coefficientVariationLinf;
    private final double[] binMids;
    private final double mkRatio;
    private final double[] maturityPerBin; //todo check that L50-L95 make sense when it's jacknife
    private final double bVariableLengthToWeightConversion;




    public LbSprEstimation(double[] catchAtLengthObserved, double linf,
                           double coefficientVariationLinf, double[] binMids,
                           double mkRatio, double[] maturityPerBin,
                           double bVariableLengthToWeightConversion) {
        this.catchAtLengthObserved = catchAtLengthObserved;
        Linf = linf;
        this.coefficientVariationLinf = coefficientVariationLinf;
        this.binMids = binMids;
        this.mkRatio = mkRatio;
        this.maturityPerBin = maturityPerBin;
        this.bVariableLengthToWeightConversion = bVariableLengthToWeightConversion;
    }


    public static LBSPREstimate  computeSPR(double[] catchAtLengthObserved, double linf,
                                    double coefficientVariationLinf, double[] binMids,
                                    double mkRatio, double[] maturityPerBin,
                                    double bVariableLengthToWeightConversion)
    {
        LbSprEstimation problem = new LbSprEstimation(catchAtLengthObserved, linf, coefficientVariationLinf, binMids, mkRatio, maturityPerBin, bVariableLengthToWeightConversion);

        SimpleProblemWrapper wrapper = new SimpleProblemWrapper();
        wrapper.setSimpleProblem(problem);
        wrapper.setParallelThreads(1);
        wrapper.setDefaultRange(1);
        final NelderMeadSimplex nelderMeadSimplex = NelderMeadSimplex.createNelderMeadSimplex(
                wrapper
                , null);
        nelderMeadSimplex.setCheckRange(false);
        OptimizationParameters params = OptimizerFactory.makeParams(
                nelderMeadSimplex,
                15,wrapper

        );
        params.setTerminator(new CombinedTerminator(
                new EvaluationTerminator(500),
                new FitnessConvergenceTerminator(),
                false
        ));
        //kill the error printer which EVA abuses
        PrintStream _err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));

        double[] bestMultiplier = OptimizerFactory.optimizeToDouble(
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

        double lengthForSelectivityAt50cm = Math.exp(bestMultiplier[0]) * problem.Linf;
        double selectivitySlope = Math.exp(bestMultiplier[1]);
        double lengthForSelectivityAt95cm = lengthForSelectivityAt50cm + lengthForSelectivityAt50cm * selectivitySlope;
        double fishingMortalityToNaturalMortalityRatio = Math.exp(bestMultiplier[2]);

        final TheoreticalSPR bestSPR = sprFormula(lengthForSelectivityAt50cm,
                lengthForSelectivityAt95cm,
                fishingMortalityToNaturalMortalityRatio,
                100,
                linf,
                coefficientVariationLinf,
                binMids,
                mkRatio,
                maturityPerBin,
                bVariableLengthToWeightConversion);

//        SL50 <- exp(runOpt$par[1]) * Linf
//        dSL50 <- exp(runOpt$par[2])
//        SL95 <- SL50 + dSL50 * SL50
//        FM <- exp(runOpt$par[3])

        return new LBSPREstimate(
                bestSPR.getSpr(),
                fishingMortalityToNaturalMortalityRatio,
                lengthForSelectivityAt50cm,
                lengthForSelectivityAt95cm,
                problem.evaluate(bestMultiplier)[0]);
    }


    @Override
    public double[] evaluate(double[] x) {



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

    @Override
    public int getProblemDimension() {
        return 3;
    }

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


    //LBSPRgen in the Rcpp file
    static public final TheoreticalSPR sprFormula(
            double selectivityCmAt50Percent,
            double selectivityCmAt95Percent,
            double ratioFishingToNaturalMortality,
            int maximumAge,
            double[] binMids,
            double mkRatio,
            double[] maturityPerBin, //todo check that L50-L95 make sense when it's jacknife
            double bVariableLengthToWeightConversion,
            final AgeToLength ageToLength) {
        //logistic selectivity here
        double[] selectivityAtLength = new double[binMids.length];
        for (int bin = 0; bin < selectivityAtLength.length; bin++) {
            selectivityAtLength[bin] = 1d / (
                    1 + Math.exp(-Math.log(19.0) * (binMids[bin] - selectivityCmAt50Percent) / (selectivityCmAt95Percent - selectivityCmAt50Percent))
            );
        }

        double[][] catchesMatrix = new double[maximumAge + 1][binMids.length];
        double[] survivorsAtAge = new double[maximumAge + 1]; // this is Ns
        double[] unfishedNumberAtAge = new double[maximumAge+1]; //this is N0
        double[] maturityAtAge = new double[maximumAge+1]; //this is Ma

        double cumulativeSelexAtAge = 0; //sum(Sx) in the original code
        for (int age = 0; age < catchesMatrix.length; age++) {

            for (int lengthBin = 0; lengthBin < binMids.length; lengthBin++) {
                cumulativeSelexAtAge += selectivityAtLength[lengthBin] * ageToLength.getAgeToLengthKey()[age][lengthBin];
                catchesMatrix[age][lengthBin] = ageToLength.getAgeToLengthKey()[age][lengthBin] * selectivityAtLength[lengthBin];

                maturityAtAge[age] += ageToLength.getAgeToLengthKey()[age][lengthBin] * maturityPerBin[lengthBin];

            }

            double ratioedSelex = cumulativeSelexAtAge / (age + 1); //MSX
            survivorsAtAge[age] = Math.pow((1 - ageToLength.getRelativeLengthAtAge()[age]),
                    mkRatio + (mkRatio * ratioFishingToNaturalMortality) * ratioedSelex
            );
            unfishedNumberAtAge[age] = Math.pow(1-ageToLength.relativeLengthAtAge[age],mkRatio);
        }

        //compute and normalize catch at length
        double[] catchAtLength = new double[binMids.length];
        double sum = 0;
        for (int lengthBin = 0; lengthBin < binMids.length; lengthBin++)
        {
            for (int age = 0; age < catchesMatrix.length; age++) {
                catchAtLength[lengthBin] += survivorsAtAge[age] * catchesMatrix[age][lengthBin];
            }
            sum+=catchAtLength[lengthBin];
        }
        for (int i = 0; i < catchAtLength.length; i++) {
            catchAtLength[i]=catchAtLength[i]/sum;
        }



        //SPR
        double fishedEggs = 0;
        double unfishedEggs = 0;
        for (int age = 0; age < ageToLength.getRelativeLengthAtAge().length; age++) {

            assert unfishedNumberAtAge[age]>= survivorsAtAge[age] : age;

            fishedEggs+= maturityAtAge[age] * survivorsAtAge[age] * Math.pow(ageToLength.relativeLengthAtAge[age],
                    bVariableLengthToWeightConversion);

            unfishedEggs+= maturityAtAge[age] * unfishedNumberAtAge[age] * Math.pow(ageToLength.relativeLengthAtAge[age],
                    bVariableLengthToWeightConversion);

        }

        assert fishedEggs<=unfishedEggs;

        return new TheoreticalSPR(fishedEggs/unfishedEggs,
                catchAtLength);



    }

    //LBSPRgen in the Rcpp file
    static public final TheoreticalSPR sprFormula(
            double selectivityCmAt50Percent,
            double selectivityCmAt95Percent,
            double ratioFishingToNaturalMortality,
            int maximumAge,
            double Linf,
            double coefficientVariationLinf,
            double[] binMids,
            double mkRatio,
            double[] maturityPerBin, //todo check that L50-L95 make sense when it's jacknife
            double bVariableLengthToWeightConversion) {

        //build age-to-length key
        final AgeToLength ageToLength =
                buildAgeToLengthKey(binMids, mkRatio, Linf, coefficientVariationLinf, maximumAge);

        return sprFormula(selectivityCmAt50Percent,selectivityCmAt95Percent,ratioFishingToNaturalMortality,maximumAge,
                binMids,mkRatio,maturityPerBin,bVariableLengthToWeightConversion,ageToLength);


    }


    //LBSPRopt in the original cpp code
    static public double lbsprDistance(
            double[] catchAtLengthObserved,
            //these three are the variables of the model, they are logged because I think it helps with optim in R
            double logSelectivityCmAt50PercentAsPercentageOfLinf,
            double logSelectivitySlope,
            double logRatioFishingToNaturalMortality,
            int maximumAge,
            double Linf,
            double coefficientVariationLinf,
            double[] binMids,
            double mkRatio,
            double[] maturityPerBin, //todo check that L50-L95 make sense when it's jacknife
            double bVariableLengthToWeightConversion

    ){
        final AgeToLength ageToLength =
                buildAgeToLengthKey(binMids, mkRatio, Linf, coefficientVariationLinf, maximumAge);

        //rescale to useful
        double selectivityCmAt50Percent =
                Math.exp(logSelectivityCmAt50PercentAsPercentageOfLinf) * Linf;
        double selectivityCmAt95Percent = selectivityCmAt50Percent +
                Math.exp(logSelectivitySlope) * selectivityCmAt50Percent;
        double ratioFishingToNaturalMortality =
                Math.exp(logRatioFishingToNaturalMortality);

        //normalize observed catch at length
        double[] catchAtLengthObservedNormalized = new double[catchAtLengthObserved.length];
        double totalCatches = 0;
        for (double caught : catchAtLengthObserved) {
            totalCatches+=caught;
        }
        for (int bin = 0; bin < catchAtLengthObservedNormalized.length; bin++) {
            catchAtLengthObservedNormalized[bin] = catchAtLengthObserved[bin]/totalCatches;
        }

        //compute SPR in theory
        final TheoreticalSPR theoreticalSPR = sprFormula(selectivityCmAt50Percent, selectivityCmAt95Percent, ratioFishingToNaturalMortality, maximumAge,
                binMids, mkRatio, maturityPerBin, bVariableLengthToWeightConversion, ageToLength);

        //compare and compute NLL
        double error = 0;
        for (int bin = 0; bin < catchAtLengthObservedNormalized.length; bin++) {
            if(catchAtLengthObservedNormalized[bin]>0 & theoreticalSPR.getCatchesAtLength()[bin]>0)
            {
                error+= catchAtLengthObserved[bin] * Math.log(theoreticalSPR.getCatchesAtLength()[bin]/ catchAtLengthObservedNormalized[bin] );
            }
        }

        //need to penalize for selectivity
        double penalization = 0;
        double penalizationValue = error;
        /*
          double Pen=0;
  double PenVal=NLL;
  Pen=R::dbeta(exp(pars(0)), 5.0, 0.1,0) * PenVal;
  if (exp(pars(0)) >= 1) Pen=PenVal*exp(pars(0));
  NLL = NLL + Pen;
         */
        BetaDistribution distribution = new BetaDistribution(5,0.1);
        final double expPar0 = Math.exp(logSelectivityCmAt50PercentAsPercentageOfLinf);
        if(expPar0>=1)
            penalization = penalizationValue * expPar0;
        else
            penalization = distribution.density(expPar0) * penalizationValue;

        error = error + penalization;

        return -error;



    }




    public static class TheoreticalSPR{

        private final double spr;

        private final double[] catchesAtLength;


        public TheoreticalSPR(double spr, double[] catchesAtLength) {
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

    public static class AgeToLength{

        private final double[][] ageToLengthKey;

        private final double[] relativeLengthAtAge;

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


    public static class LBSPREstimate {

        private final double spr;

        private final double fishingToNaturalMortalityRatio;

        private final double lengthAt50PercentSelectivity;

        private final double lengthAt95PercentSelectivity;

        private final double likelihood;


        public LBSPREstimate(double spr, double fishingToNaturalMortalityRatio, double lengthAt50PercentSelectivity, double lengthAt95PercentSelectivity, double likelihood) {
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

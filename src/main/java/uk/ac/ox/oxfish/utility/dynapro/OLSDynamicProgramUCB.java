package uk.ac.ox.oxfish.utility.dynapro;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by carrknight on 12/16/16.
 */
public class OLSDynamicProgramUCB extends OLSDynamicProgram {


    /**
     * each array is the "beta"s of the variance function
     */
    private double[][] varianceCoefficients;
    private double[][] temporaryVarianceCoefficients;

    /**
     * we are going to choose the arm with qvalue + risk * standard_deviation
     */
    private double risk;


    public OLSDynamicProgramUCB(
            int possibleActions,
            Function<FishState, Double> rewardFunction, boolean addSquares,
            boolean addCubes, boolean addInteractions, boolean addCumulative, boolean addAverages, boolean addLags,
            double errorRate, Predicate<double[]> lastStep,
            double risk, Function<Pair<FishState, Double>, Double>... features) {
        super(possibleActions, rewardFunction, addSquares, addCubes, addInteractions, addCumulative, addAverages,
              addLags,
              errorRate, lastStep, features);
        this.risk = risk;

        varianceCoefficients = new double[getLinearParameters().length][getRegressionDimension()];
        for(int i=0; i<varianceCoefficients.length; i++)
            varianceCoefficients[i] = new double[getRegressionDimension()];

    }


    @Override
    protected void updateLinearParametersGivenRegression(
            int i, OLSMultipleLinearRegression regression, double[][] x) {


        //compute variance as well
        double[] residuals = regression.estimateResiduals();
        //square them then log them
        for(int j=0; j<residuals.length; j++)
            residuals[j] = Math.log(residuals[j] * residuals[j]);
        //regress
        OLSMultipleLinearRegression variance = new OLSMultipleLinearRegression();
        variance.setNoIntercept(true);
        variance.newSampleData(residuals,x);
        temporaryVarianceCoefficients[i] = variance.estimateRegressionParameters();
        super.updateLinearParametersGivenRegression(i, regression, x);
    }

    /**
     * lspiRun separate regressions for each possible state
     */
    @Override
    public void regress() {
        temporaryVarianceCoefficients = new double[varianceCoefficients.length][varianceCoefficients[0].length];
        super.regress();
        Preconditions.checkArgument(temporaryVarianceCoefficients != null);
        varianceCoefficients = temporaryVarianceCoefficients;
        temporaryVarianceCoefficients = null;
    }

    /**
     * by default this just computes the q value of each action but it could be modified
     * to compute something more akin UCB
     *
     * @param currentFeatures the features to use to extract the q value (and whatever else)
     * @return an array producing the scores
     */
    @Override
    protected double[] scoreEachAction(double[] currentFeatures) {
        double[] qValues = super.scoreEachAction(currentFeatures);
        for(int i=0; i<qValues.length; i++) {
            double std = 0;
            for(int j = 0; j< getRegressionDimension(); j++)
                std += currentFeatures[j] * varianceCoefficients[i][j];
            std = Math.sqrt(Math.exp(std));
            qValues[i] = qValues[i] + risk * std;
        }
        return qValues;
    }


    /**
     * Setter for property 'errorRate'.
     *
     * @param errorRate Value to set for property 'errorRate'.
     */
    @Override
    public void setErrorRate(double errorRate) {
        super.setErrorRate(errorRate);
        if(errorRate == 0)
            setRisk(0);
        else
            setRisk(1.5);
    }

    /**
     * Getter for property 'risk'.
     *
     * @return Value for property 'risk'.
     */
    public double getRisk() {
        return risk;
    }

    /**
     * Setter for property 'risk'.
     *
     * @param risk Value to set for property 'risk'.
     */
    public void setRisk(double risk) {
        this.risk = risk;
    }
}

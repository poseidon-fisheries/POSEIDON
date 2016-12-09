package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.bandit.BanditSwitch;
import uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm;

import java.util.function.Function;

/**
 * A not very good implementation of multi-logit classifier
 * Created by carrknight on 12/5/16.
 */
public class LogisticMultiClassifier {

    /**
     * the coefficients to use. one array for each possible Y
     */
    private final double[][] betas;


    public LogisticMultiClassifier(double[][] betas) {
        this.betas = betas;
    }

    /**
     * pick an arm given your beta and the input matrix
     * @param x the input matrix
     * @param random the randomizer
     * @return choice in terms of index
     */
    public Integer choose(final double[][] x, MersenneTwisterFast random)
    {

        return SoftmaxBanditAlgorithm.drawFromSoftmax(
                random,
                getNumberOfOptions(),
                new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer arm) {
                        double[] beta = betas[arm];
                        assert beta.length == x[0].length;
                        //sum them up
                        double sum = 0;
                        for(int i=0; i<beta.length ;i++)
                            sum += beta[i] *  x[arm][i];
                        return sum;
                    }
                }

        );

    }





    /**
     * the probability of making a particular choice
     * @param arm index of the arm you want to know the probability of picking
     * @param x input matrix
     * @return the probability
     */
    public double getProbability(int arm, final double[][] x){


        double[] ecdf = SoftmaxBanditAlgorithm.getECDF(
                getNumberOfOptions(),
                new Function<Integer, Double>()
                {
                    @Override
                    public Double apply(Integer arm) {
                        double[] beta = betas[arm];
                        assert beta.length == x[0].length;
                        //sum them up
                        double sum = 0;
                        for(int i=0; i<beta.length ;i++)
                            sum += beta[i] *  x[arm][i];
                        return sum;
                    }
                },
                1d);
        if(arm == 0)
            return ecdf[0];
        else
            return ecdf[arm]-ecdf[arm-1];
    }

    /**
     * how many Y can this classifier guess
     * @return
     */
    public int getNumberOfOptions(){
        return betas.length;
    }


    /**
     * Getter for property 'betas'.
     *
     * @return Value for property 'betas'.
     */
    public double[][] getBetas() {
        return betas;
    }


}

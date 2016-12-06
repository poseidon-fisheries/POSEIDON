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

    /**
     * the observation extractors (the functions that return the "x" associated with each group). One array for each possible Y
     */
    private final ObservationExtractor[][] extractors;


    /**
     * function (that can return null) that extracts from a bandit arm a seatile associated with it.
     * This is then fed to the observtion extractor
     */
    private Function<Integer,SeaTile> armToTileExtractor;

    public LogisticMultiClassifier(
            double[][] betas, ObservationExtractor[][] extractors) {
        this(betas, extractors, integer -> null);
    }

    public LogisticMultiClassifier(
            double[][] betas, ObservationExtractor[][] extractors,
            Function<Integer, SeaTile> armToTileExtractor) {
        this.betas = betas;
        this.extractors = extractors;
        this.armToTileExtractor = armToTileExtractor;
    }

    public Integer choose(Fisher fisher, FishState state, MersenneTwisterFast random)
    {

        //this is the same algorithm as the SOFTMAX bandit so we just call that one.
        //the only difference is that the "reward" here is a linear combination
        return SoftmaxBanditAlgorithm.drawFromSoftmax(
                random,
                getNumberOfOptions(),
                new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer arm) {
                        double[] beta = betas[arm];
                        ObservationExtractor[] x = extractors[arm];
                        assert beta.length == x.length;
                        //sum them up
                        double sum = 0;
                        for(int i=0; i<beta.length ;i++)
                            sum += beta[i] * x[i].extract(armToTileExtractor.apply(arm),
                                                         state.getHoursSinceStart(),fisher,state);
                        return sum;
                    }
                }

        );

    }


    public double getProbability(int choice,
                                 Fisher fisher, FishState state){


        double[] ecdf = SoftmaxBanditAlgorithm.getECDF(getNumberOfOptions(),
                                                       new Function<Integer, Double>() {
                                                           @Override
                                                           public Double apply(Integer integer) {
                                                               double[] beta = betas[integer];
                                                               ObservationExtractor[] x = extractors[integer];
                                                               assert beta.length == x.length;
                                                               //sum them up
                                                               double sum = 0;
                                                               for (int i = 0; i < beta.length; i++)
                                                                   sum += beta[i] * x[i].extract(null,
                                                                                                state.getHoursSinceStart(),
                                                                                                fisher, state);
                                                               return sum;
                                                           }
                                                       },
                                                       1d);
        if(choice == 0)
                return ecdf[0];
        else
            return ecdf[choice]-ecdf[choice-1];
    }

    /**
     * how many Y can this classifier guess
     * @return
     */
    public int getNumberOfOptions(){
        assert extractors.length==betas.length;
        return extractors.length;
    }


    /**
     * Getter for property 'betas'.
     *
     * @return Value for property 'betas'.
     */
    public double[][] getBetas() {
        return betas;
    }

    /**
     * Getter for property 'extractors'.
     *
     * @return Value for property 'extractors'.
     */
    public ObservationExtractor[][] getExtractors() {
        return extractors;
    }

    public Function<Integer, SeaTile> getArmToTileExtractor() {
        return armToTileExtractor;
    }

    public void setArmToTileExtractor(
            Function<Integer, SeaTile> armToTileExtractor) {
        this.armToTileExtractor = armToTileExtractor;
    }
}

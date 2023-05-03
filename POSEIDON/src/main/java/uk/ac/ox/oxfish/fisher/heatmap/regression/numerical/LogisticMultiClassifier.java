/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
     * reward associated with NaN
     */
    public static final double INVALID_PENALTY = -100000000d;
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
                        //there might be some very invalid ones
                        if(Double.isFinite(sum))
                            return sum;
                        else
                            return INVALID_PENALTY;
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


        return SoftmaxBanditAlgorithm.getProbabilities(
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
                1d)[arm];

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

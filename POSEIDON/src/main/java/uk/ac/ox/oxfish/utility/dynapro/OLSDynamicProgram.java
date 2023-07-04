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

package uk.ac.ox.oxfish.utility.dynapro;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Batch dynamic program (makes oscillations less likely) using OLS
 * on all data
 * Created by carrknight on 12/15/16.
 */
@SuppressWarnings({"varargs"})
public class OLSDynamicProgram {


    /**
     * how many actions are possible
     */
    private final int possibleActions;
    private final Function<FishState, Double> rewardFunction;
    /**
     * the number of features + intercept + transformations
     */
    private final int regressionDimension;
    private final boolean addSquares;
    private final boolean addCubes;
    private final boolean addInteractions;
    private final boolean addCumulative;
    private final boolean addAverages;
    private final boolean addLags;
    /**
     * holds the features describing S for any step we observe.
     * Index refers to the action taken
     */
    private final LinkedList<double[]>[] preDecisionStates;
    /**
     * holds the features describing S' for any step we observe
     * Index refers to the action taken
     */
    private final LinkedList<double[]>[] postDecisionStates;
    /**
     * returns true if it is the last step of the game given the features observed
     */
    private final Predicate<double[]> lastStep;
    /**
     * holds the rewards observed at each step
     * Index refers to the action taken
     */
    private final LinkedList<Double>[] rewards;
    /**
     * functions that extract a feature from the fishstate and the previous feature
     */
    private final Function<Entry<FishState, Double>, Double>[] features;
    /**
     * store the linear parameters as they are regressed
     */
    protected double[][] temporaryParameters;
    /**
     * each array is the "beta"s of the linear approximation
     */
    private double[][] linearParameters;
    private double[] oldFeatures;
    private int lastAction;
    private double errorRate;

    @SafeVarargs
    @SuppressWarnings({"unchecked", "rawtypes"})
    public OLSDynamicProgram(
        final int possibleActions,
        final Function<FishState, Double> rewardFunction,
        final boolean addSquares,
        final boolean addCubes,
        final boolean addInteractions,
        final boolean addCumulative,
        final boolean addAverages,
        final boolean addLags,
        final double errorRate, final Predicate<double[]> lastStep,
        final Function<Entry<FishState, Double>, Double>... features
    ) {
        this.possibleActions = possibleActions;
        this.rewardFunction = rewardFunction;
        this.addCumulative = addCumulative;
        this.addAverages = addAverages;
        this.addLags = addLags;
        this.errorRate = errorRate;
        this.lastStep = lastStep;
        this.features = features;
        this.addSquares = addSquares;
        this.addCubes = addCubes;
        this.addInteractions = addInteractions;

        //always add an intercept
        int dimensions = 1 + features.length;
        if (addSquares)
            dimensions += features.length;
        if (addCubes)
            dimensions += features.length;
        if (addCumulative)
            dimensions += features.length;
        if (addAverages)
            dimensions += features.length;
        if (addLags)
            dimensions += features.length;
        if (addInteractions)
            dimensions += (features.length) * (features.length - 1) / 2;
        regressionDimension = dimensions;
        linearParameters = new double[possibleActions][regressionDimension];

        preDecisionStates = (LinkedList<double[]>[]) new LinkedList[possibleActions];
        postDecisionStates = (LinkedList<double[]>[]) new LinkedList[possibleActions];
        rewards = (LinkedList<Double>[]) new LinkedList[possibleActions];
        for (int action = 0; action < possibleActions; action++) {
            preDecisionStates[action] = new LinkedList<>();
            postDecisionStates[action] = new LinkedList<>();
            rewards[action] = new LinkedList<>();
            linearParameters[action] = new double[regressionDimension];
        }

    }

    public int step(final FishState state) {
        return step(state, lastAction);
    }

    public int step(final FishState state, final int previousAction) {

        final double[] currentFeatures = featurize(state, oldFeatures == null ?
            new double[regressionDimension] : oldFeatures);
        final double reward = rewardFunction.apply(state);

        //find action by maximizing q value
        lastAction = pickBestAction(currentFeatures).getKey();
        //randomize if needed
        if (state.getRandom().nextDouble() < errorRate)
            lastAction = state.getRandom().nextInt(getPossibleActions());

        if (oldFeatures != null) {
            preDecisionStates[previousAction].add(oldFeatures);
            postDecisionStates[previousAction].add(currentFeatures);
            rewards[previousAction].add(reward);
        }
        oldFeatures = currentFeatures;

        return lastAction;

    }

    private double[] featurize(final FishState state, final double[] previousFactors) {
        final double[] toReturn = new double[regressionDimension];
        final double[] originals = new double[features.length];
        int i = 0;
        toReturn[i] = 1;
        i++; //intercept
        for (int j = 0; j < originals.length; j++, i++) {
            originals[j] = features[j].apply(entry(state, previousFactors[j + 1]));
            toReturn[i] = originals[j];
        }
        assert i == originals.length + 1;
        if (addSquares) {
            for (int j = 0; j < originals.length; j++, i++)
                toReturn[i] = originals[j] * originals[j];
        }
        if (addCubes) {
            for (int j = 0; j < originals.length; j++, i++)
                toReturn[i] = originals[j] * originals[j] * originals[j];
        }
        if (addCumulative) {
            for (int j = 0; j < originals.length; j++, i++)
                toReturn[i] = originals[j] + previousFactors[i];
        }
        if (addAverages) {
            for (int j = 0; j < originals.length; j++, i++)
                toReturn[i] = 0.2 * originals[j] + 0.8 * previousFactors[i];
        }
        if (addLags) {
            for (int j = 0; j < originals.length; j++, i++)
                toReturn[i] = previousFactors[j + 1];  //+1 due to intercept
        }

        if (addInteractions) {
            for (int j = 0; j < originals.length; j++)
                for (int k = j + 1; k < originals.length; k++, i++) {
                    assert j != k;
                    toReturn[i] = originals[j] * originals[k];
                }

        }
        assert i == toReturn.length;

        return toReturn;
    }

    /**
     * returns best action-best q pair
     *
     * @param currentFeatures
     * @return
     */
    private Entry<Integer, Double> pickBestAction(final double[] currentFeatures) {
        final double[] scores = scoreEachAction(currentFeatures);
        int bestAction = 0;
        for (int i = 1; i < scores.length; i++) {
            final double candidate = scores[i];
            if ((candidate > scores[bestAction])) {
                bestAction = i;
            }
        }
        return entry(bestAction, scores[bestAction]);
    }

    /**
     * Getter for property 'possibleActions'.
     *
     * @return Value for property 'possibleActions'.
     */
    public int getPossibleActions() {
        return possibleActions;
    }

    /**
     * by default this just computes the q value of each action but it could be modified
     * to compute something more akin UCB
     *
     * @param currentFeatures the features to use to extract the q value (and whatever else)
     * @return an array producing the scores
     */
    protected double[] scoreEachAction(final double[] currentFeatures) {
        final double[] qValues = new double[getPossibleActions()];
        for (int i = 0; i < qValues.length; i++)
            qValues[i] = qValue(currentFeatures, i);
        return qValues;
    }

    /**
     * the approximate value we give to the total sum of rewards of the next steps
     * if we take the next action
     *
     * @param features features current
     * @param action   action we take
     * @return the value function if the next action is constrained
     */
    public double qValue(final double[] features, final int action) {
        //q value is 0 when the game ends!
        if (lastStep.test(features))
            return 0;

        double sum = 0;
        final double[] beta = linearParameters[action];
        assert beta.length == features.length;
        for (int i = 0; i < features.length; i++)
            sum += beta[i] * features[i];
        return sum;

    }

    /**
     * lspiRun separate regressions for each possible state
     */
    public void regress() {

        temporaryParameters = new double[linearParameters.length][linearParameters[0].length];
        for (int i = 0; i < linearParameters.length; i++) {
            assert preDecisionStates[i].size() == postDecisionStates[i].size();
            assert preDecisionStates[i].size() == rewards[i].size();
            final double[][] x = new double[preDecisionStates[i].size()][getRegressionDimension()];
            final double[] y = new double[preDecisionStates[i].size()];
            final Iterator<double[]> pre = preDecisionStates[i].iterator();
            final Iterator<double[]> post = postDecisionStates[i].iterator();
            final Iterator<Double> rewardIterator = rewards[i].iterator();
            //create design matrix
            int j = 0;
            while (pre.hasNext()) {
                final double[] features = pre.next();
                x[j] = Arrays.copyOf(features, features.length);
                //y is just reward plus max Q
                final double reward = rewardIterator.next();
                final double[] postFeatures = Arrays.copyOf(post.next(), features.length);
                final double maxQ = pickBestAction(postFeatures).getValue();
                y[j] = reward + maxQ;
                j++;
            }
            ///feed it
            final OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.setNoIntercept(true); //we bring our own
            regression.newSampleData(y, x);
            updateLinearParametersGivenRegression(i, regression, x);
        }
        linearParameters = temporaryParameters;
        temporaryParameters = null;

    }

    /**
     * Getter for property 'regressionDimension'.
     *
     * @return Value for property 'regressionDimension'.
     */
    public int getRegressionDimension() {
        return regressionDimension;
    }

    protected void updateLinearParametersGivenRegression(
        final int i,
        final OLSMultipleLinearRegression regression,
        final double[][] x
    ) {
        temporaryParameters[i] = regression.estimateRegressionParameters();
    }

    /**
     * Getter for property 'addSquares'.
     *
     * @return Value for property 'addSquares'.
     */
    public boolean isAddSquares() {
        return addSquares;
    }

    /**
     * Getter for property 'addCubes'.
     *
     * @return Value for property 'addCubes'.
     */
    public boolean isAddCubes() {
        return addCubes;
    }

    /**
     * Getter for property 'addInteractions'.
     *
     * @return Value for property 'addInteractions'.
     */
    public boolean isAddInteractions() {
        return addInteractions;
    }

    /**
     * Getter for property 'errorRate'.
     *
     * @return Value for property 'errorRate'.
     */
    public double getErrorRate() {
        return errorRate;
    }

    /**
     * Setter for property 'errorRate'.
     *
     * @param errorRate Value to set for property 'errorRate'.
     */
    public void setErrorRate(final double errorRate) {
        this.errorRate = errorRate;
    }

    /**
     * Getter for property 'linearParameters'.
     *
     * @return Value for property 'linearParameters'.
     */
    public double[][] getLinearParameters() {
        return linearParameters;
    }

    /**
     * Getter for property 'rewardFunction'.
     *
     * @return Value for property 'rewardFunction'.
     */
    public Function<FishState, Double> getRewardFunction() {
        return rewardFunction;
    }

    /**
     * Getter for property 'oldFeatures'.
     *
     * @return Value for property 'oldFeatures'.
     */
    public double[] getOldFeatures() {
        return oldFeatures;
    }

    /**
     * Getter for property 'lastAction'.
     *
     * @return Value for property 'lastAction'.
     */
    public int getLastAction() {
        return lastAction;
    }
}

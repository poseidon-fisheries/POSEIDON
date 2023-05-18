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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

/**
 * Basically the poor man's version of approximate post-decision dynamic programming using the dumbest state approximation (linear)
 * and the worst post-decision conflation ( a different linear function per action taken). <br>
 * While I see this being useful only once (and thus, a separate object being more of an overkill) I want to keep this
 * object separate so that it's easily removed when a grown-up library is used.
 * Created by carrknight on 10/12/16.
 */
public class AmateurishApproximateDynamicProgram {


    /**
     * how many actions are possible
     */
    private final int possibleActions;

    /**
     * each array is the "beta"s of the linear approximation
     */
    private final double[][] linearParameters;

    /**
     * Usually called "alpha"
     */
    private double learningRate;

    public AmateurishApproximateDynamicProgram(int possibleActions, int valueFunctionDimension, double learningRate) {
        this.possibleActions = possibleActions;
        linearParameters = new double[possibleActions][];
        for (int i = 0; i < possibleActions; i++)
            linearParameters[i] = new double[valueFunctionDimension];
        this.learningRate = learningRate;
    }

    /**
     * just a bit easier to use version of updateAction() which this method actually call.
     *
     * @param actionTaken         the action you took
     * @param instantaneousReward just the instantaneous reward achieved after having taken actionTake
     * @param featuresAtTheTime   the state of the world when you took your previous action
     */
    public void updateActionDueToImmediateReward(
        int actionTaken, double instantaneousReward, double discountFactor,
        double[] featuresAtTheTime, double[] featuresNow
    ) {
        Pair<Integer, Double> currentBest = chooseBestAction(featuresNow);
        updateAction(actionTaken, instantaneousReward + discountFactor * currentBest.getFirst(), featuresAtTheTime);

    }

    /**
     * go through all the actions and choose the one with highest value given the current features
     *
     * @param stateFeatures the features of the state
     * @return the action commanding the highest value and the value function
     */
    public Pair<Integer, Double> chooseBestAction(double... stateFeatures) {
        int bestAction = 0;
        double highestValue = judgeAction(0, stateFeatures);
        for (int i = 1; i < possibleActions; i++) {
            double value = judgeAction(i, stateFeatures);
            if (value > highestValue) {
                bestAction = i;
                highestValue = value;
            }
        }

        return new Pair<>(bestAction, highestValue);

    }

    /**
     * you previously took "actionTaken", the reward observation from doing that (reward + max best state now) helps us
     * update the linear parameters associated with the previous action
     *
     * @param actionTaken       the action you took last time
     * @param rewardObservation the immediate reward you got plus the max value of the state where you are now
     * @param featuresAtTheTime the features describing the state when the last decision was taken!
     */
    public void updateAction(
        int actionTaken, double rewardObservation,
        double... featuresAtTheTime
    ) {

        double[] parameters = linearParameters[actionTaken];
        Preconditions.checkArgument(featuresAtTheTime.length == parameters.length, "dimension mismatch");

        //get back your old prediction
        double predictedValue = judgeAction(actionTaken, featuresAtTheTime);


        //w = w + a * (sample - prediction) * feature
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = parameters[i] + learningRate * (rewardObservation - predictedValue) * featuresAtTheTime[i];
            Preconditions.checkState(
                Double.isFinite(parameters[i]),
                rewardObservation + " " + predictedValue + " " + featuresAtTheTime[i] + "\n" + toString()
            );

        }

    }

    /**
     * returns the value function of being at a state (summarized by the state variables) and taking an action (identified by the action number)
     *
     * @param action        the action id
     * @param stateFeatures the measurements summarising the state we are at
     * @return a number, the higher the better
     */
    public double judgeAction(int action, double... stateFeatures) {

        Preconditions.checkArgument(stateFeatures.length == linearParameters[action].length, "dimension mismatch");
        double value = 0;
        for (int i = 0; i < stateFeatures.length; i++)
            value += stateFeatures[i] * linearParameters[action][i];
        return value;
    }

    @Override
    public String toString() {
        return FishStateUtilities.deepToStringArray(linearParameters, " ", "\n");
    }

    /**
     * smooth all parameters by the same number to avoid them exploding
     */
    public void renormalizeParameters() {
        for (int i = 0; i < linearParameters.length; i++)
            for (int j = 0; j < linearParameters[i].length; j++)
                linearParameters[i][j] *= .99;


    }

    /**
     * Getter for property 'linearParameters'.
     *
     * @return Value for property 'linearParameters'.
     */
    public double[][] getLinearParameters() {
        return linearParameters;
    }
}

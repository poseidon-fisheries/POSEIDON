package uk.ac.ox.oxfish.utility.dynapro;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Batch dynamic program (makes oscillations less likely) using OLS
 * on all data
 * Created by carrknight on 12/15/16.
 */
public class OLSDynamicProgram
{



    /**
     * how many actions are possible
     */
    private final int possibleActions;


    /**
     * each array is the "beta"s of the linear approximation
     */
    private double[][] linearParameters;


    /**
     * functions that extract a feature from the fishstate and the previous feature
     */
    private Function<Pair<FishState,Double>, Double>[] features;


    private final Function<FishState,Double> rewardFunction;

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


    private  double[] oldFeatures;

    private int lastAction;

    private double errorRate;


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

    public OLSDynamicProgram(
            int possibleActions,
            Function<FishState, Double> rewardFunction,
            boolean addSquares,
            boolean addCubes,
            boolean addInteractions,
            boolean addCumulative,
            boolean addAverages,
            boolean addLags,
            double errorRate, Predicate<double[]> lastStep,
            Function<Pair<FishState, Double>, Double>... features) {
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
        if(addSquares)
            dimensions+=features.length;
        if(addCubes)
            dimensions+=features.length;
        if(addCumulative)
            dimensions+=features.length;
        if(addAverages)
            dimensions+=features.length;
        if(addLags)
            dimensions+=features.length;
        if(addInteractions)
            dimensions+= (features.length)*(features.length-1)/2;
        regressionDimension = dimensions;
        linearParameters = new double[possibleActions][regressionDimension];

        preDecisionStates = new LinkedList[possibleActions];
        postDecisionStates = new LinkedList[possibleActions];
        rewards = new LinkedList[possibleActions];
        for(int action = 0; action<possibleActions; action++)
        {
            preDecisionStates[action] = new LinkedList<>();
            postDecisionStates[action] = new LinkedList<>();
            rewards[action] = new LinkedList<>();
            linearParameters[action] = new double[regressionDimension];
        }

    }

    public int step(FishState state)
    {
        return step(state,lastAction);
    }



    public int step(FishState state, int previousAction)
    {

        double[] currentFeatures = featurize(state,oldFeatures == null?
                new double[regressionDimension] : oldFeatures);
        double reward = rewardFunction.apply(state);

        //find action by maximizing q value
        lastAction = pickBestAction(currentFeatures).getFirst();
        //randomize if needed
        if(state.getRandom().nextDouble()<errorRate)
            lastAction = state.getRandom().nextInt(getPossibleActions());

        if(oldFeatures != null)
        {
            preDecisionStates[previousAction].add(oldFeatures);
            postDecisionStates[previousAction].add(currentFeatures);
            rewards[previousAction].add(reward);
        }
        oldFeatures = currentFeatures;

        return lastAction;

    }

    /**
     * returns best action-best q pair
     * @param currentFeatures
     * @return
     */
    private Pair<Integer,Double> pickBestAction(double[] currentFeatures) {
        double[] scores = scoreEachAction(currentFeatures);
        int bestAction = 0;
        for (int i = 1; i < scores.length; i++){
            double candidate = scores[i];
            if ((candidate > scores[bestAction])){
                bestAction = i;
            }
        }
        return new Pair<>(bestAction,scores[bestAction]);
    }

    /**
     * by default this just computes the q value of each action but it could be modified
     * to compute something more akin UCB
     * @param currentFeatures the features to use to extract the q value (and whatever else)
     * @return an array producing the scores
     */
    protected double[] scoreEachAction(double[] currentFeatures) {
        double[] qValues = new double[getPossibleActions()];
        for(int i=0; i<qValues.length; i++)
            qValues[i] = qValue(currentFeatures,i);
        return qValues;
    }


    /**
     * lspiRun separate regressions for each possible state
     */
    public void regress()
    {

        temporaryParameters = new double[linearParameters.length][linearParameters[0].length];
        for(int i=0; i<linearParameters.length; i++)
        {
            assert preDecisionStates[i].size() == postDecisionStates[i].size();
            assert preDecisionStates[i].size() ==  rewards[i].size();
            double[][] x = new double[preDecisionStates[i].size()][getRegressionDimension()];
            double[] y = new double[preDecisionStates[i].size()];
            Iterator<double[]> pre = preDecisionStates[i].iterator();
            Iterator<double[]> post = postDecisionStates[i].iterator();
            Iterator<Double> rewardIterator = rewards[i].iterator();
            //create design matrix
            int j=0;
            while(pre.hasNext())
            {
                double[] features = pre.next();
                x[j] = Arrays.copyOf(features, features.length);
                //y is just reward plus max Q
                double reward = rewardIterator.next();
                double[] postFeatures = Arrays.copyOf(post.next(),features.length);
                double maxQ = pickBestAction(postFeatures).getSecond();
                y[j] = reward+ maxQ;
                j++;
            }
            ///feed it
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.setNoIntercept(true); //we bring our own
            regression.newSampleData(y,x);
            updateLinearParametersGivenRegression(i, regression,x);
        }
        linearParameters=temporaryParameters;
        temporaryParameters=null;

    }

    /**
     * store the linear parameters as they are regressed
     */
    protected double[][] temporaryParameters;

    protected void updateLinearParametersGivenRegression(int i,
                                                         OLSMultipleLinearRegression regression,
                                                         double[][] x) {
        temporaryParameters[i] = regression.estimateRegressionParameters();
    }

    /**
     * the approximate value we give to the total sum of rewards of the next steps
     * if we take the next action
     * @param features features current
     * @param action action we take
     * @return the value function if the next action is constrained
     */
    public double qValue(double[] features, int action)
    {
        //q value is 0 when the game ends!
        if(lastStep.test(features))
            return 0;

        double sum =0;
        double[] beta = linearParameters[action];
        assert beta.length == features.length;
        for(int i=0; i<features.length; i++)
            sum += beta[i] * features[i];
        return sum;

    }






    private double[] featurize(FishState state,  double[] previousFactors)
    {
        double[] toReturn = new double[regressionDimension];
        double[] originals = new double[features.length];
        int i =0;
        toReturn[i] = 1; i++; //intercept
        for(int j=0; j<originals.length; j++, i++)
        {
            originals[j] = features[j].apply(new Pair<>(state,previousFactors[j+1]));
            toReturn[i] = originals[j];
        }
        assert  i == originals.length+1;
        if(addSquares)
        {
            for(int j=0; j<originals.length; j++, i++)
                toReturn[i] = originals[j] * originals[j];
        }
        if(addCubes)
        {
            for(int j=0; j<originals.length; j++, i++)
                toReturn[i] = originals[j] * originals[j] * originals[j];
        }
        if(addCumulative)
        {
            for(int j=0; j<originals.length; j++, i++)
                toReturn[i] = originals[j] + previousFactors[i];
        }
        if(addAverages)
        {
            for(int j=0; j<originals.length; j++, i++)
                toReturn[i] = 0.2 * originals[j] + 0.8* previousFactors[i];
        }
        if(addLags)
        {
            for(int j=0; j<originals.length; j++, i++)
                toReturn[i] =previousFactors[j+1];  //+1 due to intercept
        }

        if(addInteractions)
        {
            for(int j=0; j<originals.length; j++)
                for(int k=j+1; k<originals.length; k++, i++)
                {
                    assert j != k;
                    toReturn[i] = originals[j]*originals[k];
                }

        }
        assert i == toReturn.length;

        return toReturn;
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
     * Getter for property 'regressionDimension'.
     *
     * @return Value for property 'regressionDimension'.
     */
    public int getRegressionDimension() {
        return regressionDimension;
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
    public void setErrorRate(double errorRate) {
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

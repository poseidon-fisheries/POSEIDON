package uk.ac.ox.oxfish.utility.bandit;

import ec.util.MersenneTwisterFast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The classic epsilon greedy bandit
 * Created by carrknight on 11/9/16.
 */
public class EpsilonGreedyBanditAlgorithm implements BanditAlgorithm {

    private final double[] averages;

    private final int[] observations;

    private double explorationProbability;

    public EpsilonGreedyBanditAlgorithm(int numberOfArms, double explorationProbability)
    {
        averages = new double[numberOfArms];
        observations = new int[numberOfArms];
        this.explorationProbability = explorationProbability;
    }

    @Override
    public int chooseArm(MersenneTwisterFast random) {

        if(random.nextDouble()<explorationProbability)
            return random.nextInt(averages.length);


        double max = averages[0];
        ArrayList<Integer> maxIndices = new ArrayList<>();
        maxIndices.add(0);
        for(int i=1; i<averages.length; i++)
        {
            if(averages[i] > max)
            {
                max = averages[i];
                 maxIndices = new ArrayList<>();
                maxIndices.add(i);
            }
            else if(averages[i]==max)
                maxIndices.add(i);
        }

        assert maxIndices.size() > 0;
        return maxIndices.get(random.nextInt(maxIndices.size()));




    }

    @Override
    public void observeReward(double reward, int armPlayed) {

        observations[armPlayed]++;
        averages[armPlayed] =   averages[armPlayed] + (reward-averages[armPlayed])/observations[armPlayed];
    }

    /**
     * Getter for property 'explorationProbability'.
     *
     * @return Value for property 'explorationProbability'.
     */
    public double getExplorationProbability() {
        return explorationProbability;
    }

    /**
     * Setter for property 'explorationProbability'.
     *
     * @param explorationProbability Value to set for property 'explorationProbability'.
     */
    public void setExplorationProbability(double explorationProbability) {
        this.explorationProbability = explorationProbability;
    }
}

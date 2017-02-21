package uk.ac.ox.oxfish.utility.bandit;

import burlap.datastructures.BoltzmannDistribution;
import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;

import java.util.function.Function;

/**
 * Boltzmann exploration bandit algorithm
 * Created by carrknight on 11/10/16.
 */
public class SoftmaxBanditAlgorithm implements BanditAlgorithm{

    private final BanditAverage averages;


    private double temperature;
    private double decay;

    public SoftmaxBanditAlgorithm(BanditAverage averages,
                                  final double temp,
                                  final double decay) {
        this.averages = averages;
        this.temperature=temp;
        this.decay = decay;

    }

    @Override
    public int chooseArm(MersenneTwisterFast random) {


        return drawFromSoftmax(random, averages.getNumberOfArms(),
                               averages::getAverage, temperature);


    }


    /**
     * given a set of arms and a function telling me the expected reward from each, use softmax to draw one arm at random
     * @param random randomizer
     * @param numberOfArms the number of arms to choose from
     * @param expectedReturnOfArm a function returning the expected return associated with a particular arm
     * @return the index of the arm to pick
     */
    public static Integer drawFromSoftmax(MersenneTwisterFast random,
                                          int numberOfArms,
                                          Function<Integer,Double> expectedReturnOfArm){


        return drawFromSoftmax(random,numberOfArms,expectedReturnOfArm,1d);
    }

    /**
     * given a set of arms and a function telling me the expected reward from each, use softmax to draw one arm at random
     * @param random randomizer
     * @param numberOfArms the number of arms to choose from
     * @param expectedReturnOfArm a function returning the expected return associated with a particular arm
     * @param temperature a number that can add stochasticity to the draw
     * @return the index of the arm to pick
     */
    public static Integer drawFromSoftmax(MersenneTwisterFast random,
                                          int numberOfArms,
                                          Function<Integer,Double> expectedReturnOfArm,
                                          double temperature) {

        double[] preferences = new double[numberOfArms];
        for(int i=0; i<preferences.length; i++) {
            preferences[i] = expectedReturnOfArm.apply(i);
            if(!Double.isFinite(preferences[i]))
                preferences[i] = 0; //non-sampled areas default preference is 0
        }
        BoltzmannDistribution distribution = new BoltzmannDistribution(preferences,temperature);

        return distribution.sample();

    }

    public static double[] getProbabilities(
            int numberOfArms, Function<Integer, Double> expectedReturnOfArm, double temperature) {
        double[] preferences = new double[numberOfArms];
        for(int i=0; i<preferences.length; i++)
            preferences[i] = expectedReturnOfArm.apply(i);
        BoltzmannDistribution distribution = new BoltzmannDistribution(preferences,temperature);

        return distribution.getProbabilities();
    }

    /**
     * updates rewards and decays temperature
     * @param reward
     * @param armPlayed
     */
    @Override
    public void observeReward(double reward, int armPlayed) {
        averages.observeReward(reward, armPlayed);
        temperature=Math.max(1, decay *temperature);

    }

    public int getNumberOfObservations(int arm) {
        return averages.getNumberOfObservations(arm);
    }
}

package uk.ac.ox.oxfish.utility.bandit;

import ec.util.MersenneTwisterFast;

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
        double[] ecdf = getECDF(numberOfArms, expectedReturnOfArm, temperature);

        double seed = random.nextDouble();
        for(int i=0; i<numberOfArms; i++)
            if(seed<ecdf[i])
                return i;
        assert false;
        throw new RuntimeException("Can't be here!");

    }

    public static double[] getECDF(
            int numberOfArms, Function<Integer, Double> expectedReturnOfArm, double temperature) {
        double denominator = 0;
        double[] ecdf = new double[numberOfArms]; //cumulative density

        //find denominator by summing up all the numerators
        for(int i=0; i<numberOfArms; i++)
        {
            double numerator =  expectedReturnOfArm.apply(i);
            numerator = Double.isFinite(numerator) ?  numerator : 0; //clean NaN
            denominator += Math.exp(numerator/temperature);
            //store temporarilly all numerators in the ecdf array
            ecdf[i] = numerator;
        }
        //divide all numerators by denominator to get the probability
        for(int i=0; i<numberOfArms; i++)
        {
            ecdf[i] = Math.exp(ecdf[i]/temperature)/denominator;

            if(i>0)
                ecdf[i] += ecdf[i-1];
        }
        assert Math.abs(ecdf[numberOfArms-1]-1d)<.01;
        return ecdf;
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

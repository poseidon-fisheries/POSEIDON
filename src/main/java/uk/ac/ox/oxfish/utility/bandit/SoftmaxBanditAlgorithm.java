package uk.ac.ox.oxfish.utility.bandit;

import ec.util.MersenneTwisterFast;

/**
 * Boltzmann exploration bandit algorithm
 * Created by carrknight on 11/10/16.
 */
public class SoftmaxBanditAlgorithm implements BanditAlgorithm{

    private final BanditAverage averages;


    private double temperature;

    public SoftmaxBanditAlgorithm(BanditAverage averages, final double temp) {
        this.averages = averages;
        this.temperature=temp;
    }

    @Override
    public int chooseArm(MersenneTwisterFast random) {


        double denominator = 0;
        for(int i=0; i<averages.getNumberOfArms(); i++)
        {
            double average = averages.getAverage(i);
            average = Double.isFinite(average) ?  average : 0; //clean NaN
            denominator += Math.exp(average/temperature);
        }
        double[] probabilities = new double[averages.getNumberOfArms()];
        for(int i=0; i<averages.getNumberOfArms(); i++)
        {
            double average = averages.getAverage(i);
            average = Double.isFinite(average) ?  average : 0; //clean NaN
            probabilities[i] = Math.exp(average/temperature)/denominator;
            if(i>0)
                probabilities[i] += probabilities[i-1];
        }
        assert Math.abs(probabilities[averages.getNumberOfArms()-1]-1d)<.01;

        double seed = random.nextDouble();
        for(int i=0; i<averages.getNumberOfArms(); i++)
            if(seed<probabilities[i])
                return i;

        assert false;
        throw new RuntimeException("Can't be here!");


    }

    @Override
    public void observeReward(double reward, int armPlayed) {
        averages.observeReward(reward, armPlayed);
        temperature=Math.max(1,.98*temperature);

    }

    public int getNumberOfObservations(int arm) {
        return averages.getNumberOfObservations(arm);
    }
}

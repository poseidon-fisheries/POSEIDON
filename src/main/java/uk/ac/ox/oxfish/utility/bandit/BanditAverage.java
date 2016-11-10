package uk.ac.ox.oxfish.utility.bandit;

import uk.ac.ox.oxfish.model.data.Averager;

import java.util.function.Supplier;

/**
 * Helper method to keep track of the average reward (and number of observations)
 * Created by carrknight on 11/10/16.
 */
public class BanditAverage {


    private final Averager<Double>[] averages;
    private final int[] observations;


    public BanditAverage(int numberOfArms, Supplier<Averager<Double>> constructor)
    {
        averages = new Averager[numberOfArms];
        observations = new int[numberOfArms];
        for(int i=0; i<numberOfArms; i++)
            averages[i] = constructor.get();
    }

    public void observeReward(double reward, int arm)
    {
        averages[arm].addObservation(reward);
        observations[arm]++;
    }


    public int getNumberOfObservations(int arm)
    {
        return observations[arm];
    }

    public double getAverage(int arm)
    {
        return averages[arm].getSmoothedObservation();
    }

    public int getNumberOfArms(){
        return averages.length;
    }

}

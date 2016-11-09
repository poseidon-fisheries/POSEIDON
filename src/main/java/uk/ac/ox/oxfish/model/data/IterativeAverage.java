package uk.ac.ox.oxfish.model.data;

/**
 * Just a recursive formulation for computing the average
 * Created by carrknight on 11/9/16.
 */
public class IterativeAverage<T extends Number> implements Averager<T> {


    private double average = 0;

    private int observations = 0;

    @Override
    public void addObservation(T observation) {
        observations++;
        average += (observation.doubleValue()-average)/observations;
    }

    /**
     * returns the average
     */
    @Override
    public double getSmoothedObservation() {
        return average;
    }


}

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.PriorityQueue;

/**
 * Created by carrknight on 6/28/16.
 */
public abstract class AbstractKernelRegression implements GeographicalRegression<Double> {



    private  final int maximumNumberOfObservations;

    private double bandwidth;


    private final PriorityQueue<GeographicalObservation<Double>> observations;


    public AbstractKernelRegression(int maximumNumberOfObservations,
                                    double bandwidth) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
        this.bandwidth = bandwidth;
        observations = new PriorityQueue<>(maximumNumberOfObservations);
    }

    /**
     * adds an observation and if there are too many removes the oldest one
     * @param observation
     * @param fisher
     */
    @Override
    public void addObservation(GeographicalObservation observation, Fisher fisher) {
        observations.add(observation);
        if(observations.size()>maximumNumberOfObservations)
        {
            assert observations.size()==maximumNumberOfObservations+1;
            observations.poll();
        }
    }


    public Double predict(int x, int y, double time)
    {

        if(getObservations().size()==0)
            return 0d;
        if(getObservations().size()==1)
            return getObservations().peek().getValue();


        double[] prediction = generatePrediction(x, y, time);
        return prediction[0]/prediction[1];



    }

    /**
     * like predict but this one returns an array with [0] being the numerator and [1] being the kernel sum.
     * Useful if you need intermediate steps
     * @param x the gridX you are predicting at
     * @param y the gridY you are predicting at
     * @param time the time (in hours) you want to predict to
     * @return an array with numerator and denominator
     */
    public double[] generatePrediction(int x, int y, double time)
    {
        double kernelSum = 0;
        double numerator = 0;
        for(GeographicalObservation<Double> observation : getObservations())
        {
            double distance = distance(observation.getX(),
                                       observation.getY(),
                                       observation.getTime(),
                                       x,
                                       y,
                                       time);
            double kernel = kernel(distance/ getBandwidth());
            kernelSum+= kernel;
            numerator+= kernel*observation.getValue();
        }

        return new double[]{numerator,kernelSum};

    }

    abstract protected double distance(double fromX, double fromY, double fromTime,
                                       double toX, double toY, double toTime);


    @Override
    public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {

        if(tile.getAltitude()>=0)
            return Double.NaN;
        else
            return predict(tile.getGridX(),tile.getGridY(),time);
    }


    public double kernel(double u)
    {
        return Math.max(1d/(Math.exp(u)+2+Math.exp(-u)),0);
    }

    /**
     * Getter for property 'observations'.
     *
     * @return Value for property 'observations'.
     */
    public PriorityQueue<GeographicalObservation<Double>> getObservations() {
        return observations;
    }

    /**
     * Getter for property 'maximumNumberOfObservations'.
     *
     * @return Value for property 'maximumNumberOfObservations'.
     */
    public int getMaximumNumberOfObservations() {
        return maximumNumberOfObservations;
    }

    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }



}

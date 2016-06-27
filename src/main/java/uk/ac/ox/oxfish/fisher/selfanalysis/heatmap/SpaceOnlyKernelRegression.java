package uk.ac.ox.oxfish.fisher.selfanalysis.heatmap;

import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.PriorityQueue;

/**
 * Predicts through a kernel (logistic) average.
 * Created by carrknight on 6/27/16.
 */
public class SpaceOnlyKernelRegression implements  GeographicalRegression
{



    private  final int maximumNumberOfObservations;

    private double distanceBandwidth;


    private final PriorityQueue<GeographicalObservation> observations;


    public SpaceOnlyKernelRegression(int maximumNumberOfObservations,
                                     double distanceBandwidth) {
        this.maximumNumberOfObservations = maximumNumberOfObservations;
        this.distanceBandwidth = distanceBandwidth;
        observations = new PriorityQueue<>(maximumNumberOfObservations);
    }

    /**
     * adds an observation and if there are too many removes the oldest one
     * @param observation
     */
    @Override
    public void addObservation(GeographicalObservation observation) {
        observations.add(observation);
        if(observations.size()>maximumNumberOfObservations)
        {
            assert observations.size()==maximumNumberOfObservations+1;
            observations.poll();
        }
    }


    @Override
    public double predict(SeaTile tile, double time) {

        if(tile.getAltitude()>=0)
            return 0;
        else
            return predict(tile.getGridX(),tile.getGridY(),time);
    }

    @Override
    public double predict(int x, int y, double time)
    {

        if(observations.size()==0)
            return 0;
        if(observations.size()==1)
            return observations.peek().getValue();

        double kernelSum = 0;
        double numerator = 0;
        for(GeographicalObservation observation : observations)
        {
            double distance = cellDistance(observation.getX(),
                                           observation.getY(),
                                           x,
                                           y);
            double kernel = kernel(distance/ distanceBandwidth);
            kernelSum+= kernel;
            numerator+= kernel*observation.getValue();
        }

        return numerator/kernelSum;



    }




    private static double cellDistance(double x0,double y0,double x1,double y1){
        return Math.sqrt(Math.pow(x0-x1,2) + Math.pow(y0-y1,2));
    }

    private static double kernel(double u)
    {
        return Math.max(1d/(Math.exp(u)+2+Math.exp(-u)),0);
    }

    /**
     * Getter for property 'observations'.
     *
     * @return Value for property 'observations'.
     */
    public PriorityQueue<GeographicalObservation> getObservations() {
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
     * Getter for property 'distanceBandwidth'.
     *
     * @return Value for property 'distanceBandwidth'.
     */
    public double getDistanceBandwidth() {
        return distanceBandwidth;
    }

    /**
     * Setter for property 'distanceBandwidth'.
     *
     * @param distanceBandwidth Value to set for property 'distanceBandwidth'.
     */
    public void setDistanceBandwidth(double distanceBandwidth) {
        this.distanceBandwidth = distanceBandwidth;
    }
}

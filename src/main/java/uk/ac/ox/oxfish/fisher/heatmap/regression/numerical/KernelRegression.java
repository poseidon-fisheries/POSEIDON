package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.EpanechinikovKernel;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFKernel;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.PriorityQueue;

/**
 * A kernel regression with a limited
 * Created by carrknight on 9/2/16.
 */
public class KernelRegression  implements GeographicalRegression<Double>{


    /**
     * the rbf weight we give to each element
     */
    private final double[] bandwidths;

    /**
     * functions to extract features from the cell
     */
    private final ObservationExtractor[] extractors;


    /**
     * kernel object to use
     */
    private final EpanechinikovKernel kernel = new EpanechinikovKernel(0);


    /**
     * delete observations after you have more than this
     */
    private final int maximumNumberOfObservationsToKeep;



    /**
     * observations
     */
    private final PriorityQueue<GeographicalObservation<Double>> observations;


    public KernelRegression(
            int maximumNumberOfObservationsToKeep,
            Pair<ObservationExtractor,Double>... extractorsAndBandwidths)
    {

        this.bandwidths = new double[extractorsAndBandwidths.length];
        this.extractors = new ObservationExtractor[extractorsAndBandwidths.length];

        for(int i=0; i< extractorsAndBandwidths.length; i++)
        {
            this.extractors[i] = extractorsAndBandwidths[i].getFirst();
            this.bandwidths[i] = extractorsAndBandwidths[i].getSecond();
        }

        this.maximumNumberOfObservationsToKeep = maximumNumberOfObservationsToKeep;
        observations = new PriorityQueue<>(maximumNumberOfObservationsToKeep);
    }


    /**
     * adds an observation and if there are too many removes the oldest one
     * @param observation
     * @param fisher
     */

    public void addObservation(GeographicalObservation observation, Fisher fisher) {
        observations.add(observation);
        if(observations.size()>maximumNumberOfObservationsToKeep)
        {
            assert observations.size()==maximumNumberOfObservationsToKeep+1;
            observations.poll();
        }
    }

    /**
     * turn the "V" value of the geographical observation into a number
     *
     * @param observation
     * @param fisher
     * @return
     */
    @Override
    public double extractNumericalYFromObservation(
            GeographicalObservation<Double> observation, Fisher fisher) {
        return  observation.getValue();
    }



    /**
     * predict numerical value here
     *
     * @param tile
     * @param time
     * @param fisher
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher) {


        double kernelSum = 0;
        double numerator = 0;
        for(GeographicalObservation<Double> observation : observations)
        {
            double currentKernel = 1;
            for(int i=0; i<bandwidths.length; i++) {
                kernel.setBandwidth(bandwidths[i]);
                currentKernel *= kernel.distance(
                        extractors[i].extract(tile,time,fisher),
                        extractors[i].extract(observation.getTile(),
                                              observation.getTime(),
                                              fisher)
                );
                //don't bother if it's a 0
                if((currentKernel )<.00001)
                    break;
            }

            if((currentKernel )>.00001) {
                kernelSum += currentKernel;
                numerator += currentKernel * observation.getValue();
            }
        }

        if(kernelSum <.00001)
            return Double.NaN;

        return numerator/kernelSum;


    }


    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return bandwidths;
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {

        assert parameterArray.length == bandwidths.length;
        System.arraycopy(parameterArray,0,bandwidths,0,bandwidths.length);
    }


}

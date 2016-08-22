package uk.ac.ox.oxfish.fisher.heatmap.regression;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFKernel;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.*;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Each tile is a separate local linear regression where the weight of the observation is given by RBF distance
 * Created by carrknight on 8/18/16.
 */
public class GeographicallyWeightedRegression implements GeographicalRegression<Double> {


    /**
     * functions used to turn an observation into a double array of features
     */
    private final ObservationExtractor[] extractors;


    private final HashMap<SeaTile,LowessTile> lowesses = new HashMap<>();







    private final RBFKernel kernel;


    public GeographicallyWeightedRegression(
            NauticalMap map, double exponentialForgetting,
            Distance distance, double rbfBandwidth,
            ObservationExtractor[] nonInterceptExtractors,
            double initialMin,
            double initialMax,
            double initialUncertainty,
            MersenneTwisterFast random) {
        Preconditions.checkArgument(initialMax>initialMin);
        //get extractors and add intercept
        this.extractors = new ObservationExtractor[nonInterceptExtractors.length+1];
        for(int i=0; i<nonInterceptExtractors.length; i++)
            this.extractors[i+1] = nonInterceptExtractors[i];
        this.extractors[0] = new ObservationExtractor() {
            @Override
            public double extract(SeaTile tile, double timeOfObservation, Fisher agent) {
                return 1;
            }
        };

        this.kernel = new RBFKernel(new RegressionDistance() {
            @Override
            public double distance(
                    Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
                return distance.distance(tile,observation.getTile(),map);
            }
        },rbfBandwidth);

        //each tile its own lowess with a random intercept
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for(SeaTile tile : tiles) {
            double[] beta = new double[nonInterceptExtractors.length+1];
            beta[0] = random.nextDouble() *(initialMax-initialMin) + initialMin;
            lowesses.put(tile, new LowessTile(nonInterceptExtractors.length + 1,
                                              initialUncertainty, beta,
                                              exponentialForgetting));
        }



    }

    @Override
    public void addObservation(
            GeographicalObservation<Double> observation, Fisher fisher) {
        //add observation with 1/weight as sigma^2
        double[] features = ObservationExtractor.convertToFeatures(
                observation.getTile(), observation.getTime(),
                fisher, extractors);
        //go through all the tiles
        for(Map.Entry<SeaTile,LowessTile> lowess : lowesses.entrySet())
        {
            double sigma = 1d/
                    kernel.distance(fisher,lowess.getKey(),observation.getTime(),observation);

            if(!Double.isFinite(sigma)) {
                lowess.getValue().increaseUncertainty();
                continue;
            }
            else {
                lowess.getValue().addObservation(features, observation.getValue(), sigma);
            }
        }
    }


    /**
     * returns the current kernel prediction
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher) {

        LowessTile predictor = lowesses.get(tile);
        if(predictor==null)
            return Double.NaN;
        else {
            double[] features = ObservationExtractor.convertToFeatures(
                    tile,time,fisher,extractors);
            double prediction = 0;
            for(int i=0; i<features.length; i++)
                prediction += features[i] * predictor.getBeta()[i];
            return prediction;
        }

    }


    @VisibleForTesting
    public double[] getBeta(SeaTile tile)
    {
        LowessTile predictor = lowesses.get(tile);
        if(predictor==null)
            return  null;
        else
            return predictor.getBeta();
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }

    /**
     * It's already a double so return it!
     */
    @Override
    public double extractNumericalYFromObservation(
            GeographicalObservation<Double> observation, Fisher fisher) {
        return observation.getValue();
    }
}

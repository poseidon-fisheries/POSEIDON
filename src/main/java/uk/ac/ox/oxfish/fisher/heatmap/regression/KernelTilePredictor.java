package uk.ac.ox.oxfish.fisher.heatmap.regression;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.LinkedList;

/**
 * A recursive kernel predictor. Because it needs to predict always in the same spot time will be a forgetting factor
 * instead of the more appropriate additional data dimension.
 * It uses product (gaussian) Kernel
 * Created by carrknight on 7/8/16.
 */
public class KernelTilePredictor{


    private double currentPrediction = 0;

    private double currentDenominator = 0;

    private final double forgettingFactor;

    private final SeaTile whereAmIPredicting;

    /**
     * the bandwidth are within the distance objects
     */
    private final LinkedList<RegressionDistance> distances = new LinkedList<>();


    public KernelTilePredictor(double forgettingFactor,
                               SeaTile whereAmIPredicting,
                               RegressionDistance... initialDistances) {
        this.forgettingFactor = forgettingFactor;
        this.whereAmIPredicting = whereAmIPredicting;
        for(RegressionDistance distance : initialDistances)
            distances.add(distance);
    }

    public void addObservation(GeographicalObservation observation, Fisher fisher)
    {
        //compute kernel
        double kernel = 1;
        for(RegressionDistance distance : distances)
            kernel *= gaussianTransform(distance.distance(fisher,
                                                          whereAmIPredicting,
                                                          observation.getTime(),
                                                          observation));

        //update denominator
        currentDenominator = currentDenominator * forgettingFactor + kernel;
        Preconditions.checkArgument(Double.isFinite(currentDenominator));
        //update predictor
        if (currentDenominator > 0)
            currentPrediction += (observation.getValue() - currentPrediction) * kernel / currentDenominator;

    }



    private double gaussianTransform(double distance){

        return Math.exp(-(distance)/2d);

    }

    public double getCurrentPrediction() {
        return currentPrediction;
    }

    public double getCurrentDenominator() {
        return currentDenominator;
    }

    public double getForgettingFactor() {
        return forgettingFactor;
    }

    public SeaTile getWhereAmIPredicting() {
        return whereAmIPredicting;
    }

    public LinkedList<RegressionDistance> getDistances() {
        return distances;
    }



}

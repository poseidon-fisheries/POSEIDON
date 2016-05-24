package uk.ac.ox.oxfish.fisher.erotetic;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simple filter: only take elements that are above the threshold but only as long as you have minimumNumberOfObservations
 * observations
 * Created by carrknight on 4/10/16.
 */
public class ThresholdFilter<T> implements FeatureFilter<T>
{

    private final int minimumNumberOfObservations;

    private double minimumThreshold;

    private final String featureName;

    public ThresholdFilter(int minimumNumberOfObservations, double minimumThreshold, String featureName) {
        this.minimumNumberOfObservations = minimumNumberOfObservations;
        this.minimumThreshold = minimumThreshold;
        this.featureName = featureName;
    }

    /**
     * Grabs the list of current options and returns the list of all options that are acceptable
     * @param currentOptions list of options, possibly already filtered by others. It is <b>unmodifiable</b>
     * @param representation the set of all feature extractors available
     * @param state          the model   @return a list of acceptable options or null if there is pure indifference among them
     * @param equipment
     * @param status
     * @param memory
     */
    @Override
    public List<T> filterOptions(
            List<T> currentOptions, FeatureExtractors<T> representation, FishState state, FisherEquipment equipment,
            FisherStatus status, FisherMemory memory) {
        HashMap<T, Double> features = representation.extractFeature(featureName,
                                                                    currentOptions,
                                                                    state, equipment, status,memory );
        //no feature, indifference
        if(features == null || features.isEmpty())
        {
            if(Log.TRACE)
                Log.trace("Threshold filter found no features and is therefore indifferent");
            return null;
        }


        //not enough features, indifferent
        List<T> actualOptions = new LinkedList<>(currentOptions);
        actualOptions.retainAll(features.keySet());
        if(actualOptions.size() < minimumNumberOfObservations)
        {
            if(Log.TRACE)
                Log.trace("Threshold filter found " + actualOptions.size() +
                                  " options with features: too few compared to the minimum of " + minimumNumberOfObservations);
            return null;
        }
         else
        {
            //you have enough! take only the ones that pass the threshold
            LinkedList<T> passTheTest = new LinkedList<>();
            for(Map.Entry<T,Double> feature : features.entrySet())
            {
                if(actualOptions.contains(feature.getKey()) &&
                        feature.getValue() >= minimumThreshold)
                    passTheTest.add(feature.getKey());
            }
            if(Log.TRACE)
                Log.trace("Threshold filter  found " + passTheTest +
                                  " as acceptable, a total of " + passTheTest.size() + " options out of " +
                                  features.size() + " available");
            return passTheTest;
        }
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }

    /**
     * Getter for property 'minimumThreshold'.
     *
     * @return Value for property 'minimumThreshold'.
     */
    public double getMinimumThreshold() {
        return minimumThreshold;
    }

    /**
     * Setter for property 'minimumThreshold'.
     *
     * @param minimumThreshold Value to set for property 'minimumThreshold'.
     */
    public void setMinimumThreshold(double minimumThreshold) {
        this.minimumThreshold = minimumThreshold;
    }
}

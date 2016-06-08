package uk.ac.ox.oxfish.fisher.erotetic;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map;

/**
 * Like threshold filter but the threshold is just another feature
 * Created by carrknight on 4/11/16.
 */
public class FeatureThresholdAnswer<T>  implements EroteticAnswer<T>
{


    private final int minimumNumberOfObservations;


    private final String featureName;


    private final String thresholdFeatureName;

    private final boolean goodAboveTrue;

    public FeatureThresholdAnswer(
            int minimumNumberOfObservations,
            String featureName,
            String thresholdFeatureName)
    {
        this(minimumNumberOfObservations, featureName, thresholdFeatureName,true);
    }

    public FeatureThresholdAnswer(
            int minimumNumberOfObservations,
            String featureName,
            String thresholdFeatureName,
            boolean goodAboveTrue)
    {
        this.minimumNumberOfObservations = minimumNumberOfObservations;
        this.featureName = featureName;
        this.thresholdFeatureName = thresholdFeatureName;
        this.goodAboveTrue = goodAboveTrue;
    }



    /**
     * Grabs the list of current options and returns the list of all options that are acceptable
     *
     * @param currentOptions list of options, possibly already filtered by others. It is <b>unmodifiable</b>
     * @param representation the set of all feature extractors available
     * @param state          the model   @return a list of acceptable options or null if there is pure indifference among them
     * @param fisher
     */
    @Override
    public List<T> answer(
            List<T> currentOptions, FeatureExtractors<T> representation, FishState state, Fisher fisher) {
        Map<T, Double> features = representation.extractFeature(featureName,
                                                                currentOptions,
                                                                state, fisher);
        Map<T, Double> thresholds = representation.extractFeature(thresholdFeatureName,
                                                                    currentOptions,
                                                                    state, fisher);



        return ThresholdAnswer.thresholdAnswer(
                currentOptions,
                features,
                thresholds::get,
                minimumNumberOfObservations,
                goodAboveTrue
        );
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
}

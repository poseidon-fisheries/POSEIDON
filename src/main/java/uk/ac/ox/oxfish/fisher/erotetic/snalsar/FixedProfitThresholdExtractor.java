package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FixedMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carrknight on 6/7/16.
 */
public class FixedProfitThresholdExtractor implements ProfitThresholdExtractor<SeaTile> {


    private final double threshold;


    public FixedProfitThresholdExtractor(double threshold) {
        this.threshold = threshold;
    }


    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     *
     * @param toRepresent the list of object from which to extract a feature
     * @param model       the model to represent
     * @param fisher
     */
    @Override
    public Map<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {
        HashMap<SeaTile,Double> features = new HashMap<>();

        return new FixedMap<>(threshold,toRepresent );
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public double getThreshold() {
        return threshold;
    }
}

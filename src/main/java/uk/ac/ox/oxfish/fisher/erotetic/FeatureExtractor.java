package uk.ac.ox.oxfish.fisher.erotetic;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.Map;

/**
 * A function used by agents to extract/represent a feature of the object of class T they want to examine
 * Created by carrknight on 4/10/16.
 */
public interface  FeatureExtractor<T> {


    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     * @param toRepresent the list of object from which to extract a feature
     * @param model the model to represent
     * @param fisher
     * */
    Map<T,Double> extractFeature(
            Collection<T> toRepresent, FishState model, Fisher fisher);




    //common names:
    String AVERAGE_PROFIT_FEATURE = "Average Fishery Profits Per Trip";


}

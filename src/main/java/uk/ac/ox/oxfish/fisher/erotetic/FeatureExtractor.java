package uk.ac.ox.oxfish.fisher.erotetic;

import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;

/**
 * A function used by agents to extract/represent a feature of the object of class T they want to examine
 * Created by carrknight on 4/10/16.
 */
public interface  FeatureExtractor<T> {


    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     * @param toRepresent the list of object from which to extract a feature
     * @param model the model to represent
     * @param equipment
     *@param status
     * @param memory @return a map of toRepresent ---> feature (as double); could be empty or null.
     * For all elements that were present as parameters
     * but not in the output this extractor could not find the correct feature for them.
     */
    HashMap<T,Double> extractFeature(
            Collection<T> toRepresent, FishState model, FisherEquipment equipment, FisherStatus status,
            FisherMemory memory);




}

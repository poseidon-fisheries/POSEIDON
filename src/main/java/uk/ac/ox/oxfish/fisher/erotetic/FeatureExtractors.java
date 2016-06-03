package uk.ac.ox.oxfish.fisher.erotetic;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The hub where we store all methods fishers use to extract features from objects of class T
 * Created by carrknight on 4/10/16.
 */
public class FeatureExtractors<T> {


    /**
     * a map to go from the name of the feature to extract to the feature extractor itself
     */
    private final HashMap<String,FeatureExtractor<T>> extractors = new HashMap<>();




    public void addFeatureExtractor(String nameOfFeature, FeatureExtractor<T> extractor)
    {
        extractors.put(nameOfFeature,extractor);
    }

    public FeatureExtractor<T> removeFeatureExtractor(String nameOfFeature)
    {
        return extractors.remove(nameOfFeature);
    }

    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     * @param featureName the name of the feature to extract
     * @param toRepresent the list of object from which to extract a feature
     * @param model the model to represent
     * @param fisher
     * */
    public Map<T,Double> extractFeature(
            String featureName,
            Collection<T> toRepresent,
            FishState model, Fisher fisher)
    {
        FeatureExtractor<T> extractor = extractors.get(featureName);
        //if there is no extractor then there is no feature to extract
        if(extractor==null)
            return null;

        return extractor.extractFeature(toRepresent, model,fisher);

    }




}

package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.RememberedProfitsExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 5/31/16.
 */
public class RememberedProfitsExtractorFactory implements AlgorithmFactory<RememberedProfitsExtractor> {


    private boolean includeOpportunityCosts = true;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RememberedProfitsExtractor apply(FishState state) {
        return new RememberedProfitsExtractor(includeOpportunityCosts);
    }


    /**
     * Getter for property 'includeOpportunityCosts'.
     *
     * @return Value for property 'includeOpportunityCosts'.
     */
    public boolean isIncludeOpportunityCosts() {
        return includeOpportunityCosts;
    }

    /**
     * Setter for property 'includeOpportunityCosts'.
     *
     * @param includeOpportunityCosts Value to set for property 'includeOpportunityCosts'.
     */
    public void setIncludeOpportunityCosts(boolean includeOpportunityCosts) {
        this.includeOpportunityCosts = includeOpportunityCosts;
    }
}

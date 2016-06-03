package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.LessThanXFishersHereExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


public class LessThanXFishersHereExtractorFactory implements AlgorithmFactory<LessThanXFishersHereExtractor>
{


    private DoubleParameter minimumNumberOfFishersToMakeItUnacceptable = new FixedDoubleParameter(1d);
    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LessThanXFishersHereExtractor apply(FishState state) {
        return  new LessThanXFishersHereExtractor(
                minimumNumberOfFishersToMakeItUnacceptable.apply(state.getRandom()).intValue());
    }

    /**
     * Getter for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     *
     * @return Value for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     */
    public DoubleParameter getMinimumNumberOfFishersToMakeItUnacceptable() {
        return minimumNumberOfFishersToMakeItUnacceptable;
    }

    /**
     * Setter for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     *
     * @param minimumNumberOfFishersToMakeItUnacceptable Value to set for property 'minimumNumberOfFishersToMakeItUnacceptable'.
     */
    public void setMinimumNumberOfFishersToMakeItUnacceptable(
            DoubleParameter minimumNumberOfFishersToMakeItUnacceptable) {
        this.minimumNumberOfFishersToMakeItUnacceptable = minimumNumberOfFishersToMakeItUnacceptable;
    }
}

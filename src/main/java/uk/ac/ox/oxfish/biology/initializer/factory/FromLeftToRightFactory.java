package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The initializer for the left-to-right biology initializer
 * Created by carrknight on 6/22/15.
 */
public class FromLeftToRightFactory implements StrategyFactory<BiologyInitializer>
{

    /**
     * leftmost biomass
     */
    private DoubleParameter maximumBiomass = new FixedDoubleParameter(5000);

    /**
     * how many times we attempt to smooth the biology between two elements
     */
    private DoubleParameter biologySmoothingIndex = new FixedDoubleParameter(1000000);



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BiologyInitializer apply(FishState state) {
        return new FromLeftToRightInitializer(maximumBiomass.apply(state.random),
                                              biologySmoothingIndex.apply(state.random).intValue());
    }


    public DoubleParameter getMaximumBiomass() {
        return maximumBiomass;
    }

    public void setMaximumBiomass(DoubleParameter maximumBiomass) {
        this.maximumBiomass = maximumBiomass;
    }

    public DoubleParameter getBiologySmoothingIndex() {
        return biologySmoothingIndex;
    }

    public void setBiologySmoothingIndex(DoubleParameter biologySmoothingIndex) {
        this.biologySmoothingIndex = biologySmoothingIndex;
    }
}

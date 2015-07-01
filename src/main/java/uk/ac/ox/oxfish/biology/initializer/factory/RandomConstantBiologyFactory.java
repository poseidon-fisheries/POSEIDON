package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.RandomConstantBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a RandomConstantBiologyInitializer
 * Created by carrknight on 6/22/15.
 */
public class RandomConstantBiologyFactory implements AlgorithmFactory<RandomConstantBiologyInitializer> {



    private double minBiomass = 0;

    private double maxBiomass = 5000;

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
    public RandomConstantBiologyInitializer apply(FishState state) {
        return new RandomConstantBiologyInitializer(minBiomass,maxBiomass,
                                                    biologySmoothingIndex.apply(state.random).intValue());
    }


    public double getMinBiomass() {
        return minBiomass;
    }

    public void setMinBiomass(double minBiomass) {
        this.minBiomass = minBiomass;
    }

    public double getMaxBiomass() {
        return maxBiomass;
    }

    public void setMaxBiomass(double maxBiomass) {
        this.maxBiomass = maxBiomass;
    }

    public DoubleParameter getBiologySmoothingIndex() {
        return biologySmoothingIndex;
    }

    public void setBiologySmoothingIndex(DoubleParameter biologySmoothingIndex) {
        this.biologySmoothingIndex = biologySmoothingIndex;
    }
}

package uk.ac.ox.oxfish.geography.habitat;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates rocky initializers
 * Created by carrknight on 10/1/15.
 */
public class RockyPyramidsFactory implements AlgorithmFactory<RockyPyramidsHabitatInitializer>
{


    private int numberOfPeaks = 10;


    private DoubleParameter smoothingValue = new FixedDoubleParameter(.7d);

    private int maxSpread = 6;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RockyPyramidsHabitatInitializer apply(FishState state) {
        return new RockyPyramidsHabitatInitializer(numberOfPeaks,smoothingValue,
                                             maxSpread);
    }

    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    public void setNumberOfPeaks(int numberOfPeaks) {
        this.numberOfPeaks = numberOfPeaks;
    }

    public DoubleParameter getSmoothingValue() {
        return smoothingValue;
    }

    public void setSmoothingValue(DoubleParameter smoothingValue) {
        this.smoothingValue = smoothingValue;
    }

    public int getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(int maxSpread) {
        this.maxSpread = maxSpread;
    }
}

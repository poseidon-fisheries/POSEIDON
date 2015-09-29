package uk.ac.ox.oxfish.geography.habitat;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * A factory that creates the rocky rectangle habitat initializers.
 * Created by carrknight on 9/29/15.
 */
public class RockyRectanglesHabitatFactory implements AlgorithmFactory<RockyRectanglesHabitatInitializer>
{


    private DoubleParameter rockyHeight = new UniformDoubleParameter(5,10);
    private DoubleParameter rockyWidth = new UniformDoubleParameter(5,10);

    private DoubleParameter numberOfRectangles = new FixedDoubleParameter(5);


    public RockyRectanglesHabitatFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RockyRectanglesHabitatInitializer apply(FishState state) {
        return new RockyRectanglesHabitatInitializer(rockyHeight,rockyWidth,
                                              numberOfRectangles.apply(state.getRandom()).intValue());
    }

    public DoubleParameter getRockyHeight() {
        return rockyHeight;
    }

    public void setRockyHeight(DoubleParameter rockyHeight) {
        this.rockyHeight = rockyHeight;
    }

    public DoubleParameter getRockyWidth() {
        return rockyWidth;
    }

    public void setRockyWidth(DoubleParameter rockyWidth) {
        this.rockyWidth = rockyWidth;
    }

    public DoubleParameter getNumberOfRectangles() {
        return numberOfRectangles;
    }

    public void setNumberOfRectangles(DoubleParameter numberOfRectangles) {
        this.numberOfRectangles = numberOfRectangles;
    }
}

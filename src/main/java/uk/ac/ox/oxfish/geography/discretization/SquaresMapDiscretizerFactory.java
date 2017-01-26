package uk.ac.ox.oxfish.geography.discretization;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The factory building squared maps
 * Created by carrknight on 1/27/17.
 */
public class SquaresMapDiscretizerFactory implements AlgorithmFactory<SquaresMapDiscretizer>
{



    private DoubleParameter horizontalSplits = new FixedDoubleParameter(2);

    private DoubleParameter verticalSplits = new FixedDoubleParameter(2);

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SquaresMapDiscretizer apply(FishState fishState) {
        return new SquaresMapDiscretizer(verticalSplits.apply(fishState.getRandom()).intValue(),
                                         horizontalSplits.apply(fishState.getRandom()).intValue());
    }


    /**
     * Getter for property 'horizontalSplits'.
     *
     * @return Value for property 'horizontalSplits'.
     */
    public DoubleParameter getHorizontalSplits() {
        return horizontalSplits;
    }

    /**
     * Setter for property 'horizontalSplits'.
     *
     * @param horizontalSplits Value to set for property 'horizontalSplits'.
     */
    public void setHorizontalSplits(DoubleParameter horizontalSplits) {
        this.horizontalSplits = horizontalSplits;
    }

    /**
     * Getter for property 'verticalSplits'.
     *
     * @return Value for property 'verticalSplits'.
     */
    public DoubleParameter getVerticalSplits() {
        return verticalSplits;
    }

    /**
     * Setter for property 'verticalSplits'.
     *
     * @param verticalSplits Value to set for property 'verticalSplits'.
     */
    public void setVerticalSplits(DoubleParameter verticalSplits) {
        this.verticalSplits = verticalSplits;
    }
}

package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;


import uk.ac.ox.oxfish.fisher.heatmap.regression.KernelTransduction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.SpaceRegressionDistance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class KernelTransductionFactory implements AlgorithmFactory<KernelTransduction>
{


    private DoubleParameter spaceBandwidth = new FixedDoubleParameter(5);


    private DoubleParameter forgettingFactor = new FixedDoubleParameter(1);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public KernelTransduction apply(FishState state) {
        return new KernelTransduction(
                state.getMap(),
                forgettingFactor.apply(state.getRandom()),
                new SpaceRegressionDistance(
                        spaceBandwidth.apply(state.getRandom())
                )
        );
    }


    public DoubleParameter getSpaceBandwidth() {
        return spaceBandwidth;
    }

    public void setSpaceBandwidth(DoubleParameter spaceBandwidth) {
        this.spaceBandwidth = spaceBandwidth;
    }

    public DoubleParameter getForgettingFactor() {
        return forgettingFactor;
    }

    public void setForgettingFactor(DoubleParameter forgettingFactor) {
        this.forgettingFactor = forgettingFactor;
    }
}



package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;


import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.KernelTransduction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
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
        double bandwidth = spaceBandwidth.apply(state.getRandom());
        return new KernelTransduction(
                state.getMap(),
                forgettingFactor.apply(state.getRandom()),
                new Pair<>(new ObservationExtractor() {
                    @Override
                    public double extract(
                            SeaTile tile, double timeOfObservation, Fisher agent) {
                        return tile.getGridX();
                    }
                },bandwidth),

                new Pair<>(new ObservationExtractor() {
                    @Override
                    public double extract(
                            SeaTile tile, double timeOfObservation, Fisher agent) {
                        return tile.getGridY();
                    }
                },bandwidth));
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



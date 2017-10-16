package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import uk.ac.ox.oxfish.fisher.erotetic.snalsar.AverageProfitsThresholdExtractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates the Avergae Profits Threshold Extractor
 * Created by carrknight on 6/8/16.
 */
public class AverageProfitsThresholdFactory implements AlgorithmFactory<AverageProfitsThresholdExtractor>
{

    private DoubleParameter scale = new FixedDoubleParameter(1d);


    public AverageProfitsThresholdFactory() {
    }


    public AverageProfitsThresholdFactory(double multiplier) {

        scale = new FixedDoubleParameter(multiplier);
    }



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public AverageProfitsThresholdExtractor apply(FishState state) {
        return new AverageProfitsThresholdExtractor(scale.apply(state.getRandom()));
    }


    public DoubleParameter getScale() {
        return scale;
    }

    public void setScale(DoubleParameter scale) {
        this.scale = scale;
    }
}

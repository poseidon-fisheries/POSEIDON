package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.KnifeEdgeCashflowObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgeCashflowFactory implements AlgorithmFactory<KnifeEdgeCashflowObjective> {


    private DoubleParameter period = new FixedDoubleParameter(365);

    private DoubleParameter threshold = new FixedDoubleParameter(10d);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public KnifeEdgeCashflowObjective apply(FishState fishState) {
        CashFlowObjectiveFactory factory = new CashFlowObjectiveFactory(period);
        return new KnifeEdgeCashflowObjective(
                threshold.apply(fishState.getRandom()),
                factory.apply(fishState));

    }

    /**
     * Getter for property 'period'.
     *
     * @return Value for property 'period'.
     */
    public DoubleParameter getPeriod() {
        return period;
    }

    /**
     * Setter for property 'period'.
     *
     * @param period Value to set for property 'period'.
     */
    public void setPeriod(DoubleParameter period) {
        this.period = period;
    }

    /**
     * Getter for property 'threshold'.
     *
     * @return Value for property 'threshold'.
     */
    public DoubleParameter getThreshold() {
        return threshold;
    }

    /**
     * Setter for property 'threshold'.
     *
     * @param threshold Value to set for property 'threshold'.
     */
    public void setThreshold(DoubleParameter threshold) {
        this.threshold = threshold;
    }
}

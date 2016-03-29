package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A factory building cashflow objective functions
 * Created by carrknight on 3/24/16.
 */
public class CashFlowObjectiveFactory implements AlgorithmFactory<CashFlowObjective>{



    private DoubleParameter period = new FixedDoubleParameter(365);

    public CashFlowObjectiveFactory(DoubleParameter period) {
        this.period = period;
    }

    public CashFlowObjectiveFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public CashFlowObjective apply(FishState fishState) {
        int period = this.period.apply(fishState.getRandom()).intValue();
        Preconditions.checkArgument(period>0, "Cashflow objective must have a period higher than 0");
        return new CashFlowObjective(period);
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
}

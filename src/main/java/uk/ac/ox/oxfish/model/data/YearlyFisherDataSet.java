package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * the data gatherer for a fisher that steps every year. It gathers:
 * <ul>
 *     <li> CASH</li>
 *     <li> NET_CASH_FLOW</li>
 * </ul>
 */
public class YearlyFisherDataSet extends DataSet<Fisher>
{


    public YearlyFisherDataSet() {
        super(IntervalPolicy.EVERY_YEAR);
    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, Fisher observed) {
        //CASH
        registerGather("CASH", Fisher::getCash,Double.NaN);

        registerGather("NET_CASH_FLOW", new Function<Fisher, Double>() {
            double oldCash = observed.getCash();
            @Override
            public Double apply(Fisher fisher) {
                double flow = fisher.getCash() - oldCash;
                oldCash = fisher.getCash();
                return flow;
            }
        },Double.NaN);

        super.start(state, observed);

    }
}

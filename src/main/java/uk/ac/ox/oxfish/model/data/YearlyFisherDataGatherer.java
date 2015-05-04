package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Function;

/**
 * the data gatherer for a fisher that steps every year. It gathers:
 * <ul>
 *     <li> CASH</li>
 *     <li> NET_CASH_FLOW</li>
 * </ul>
 */
public class YearlyFisherDataGatherer extends DataGatherer<Fisher>
{


    public YearlyFisherDataGatherer() {
        super(true);
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

package uk.ac.ox.oxfish.model.data.collectors;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.function.Function;

/**
 * the data gatherer for a fisher that steps every year. It gathers:
 * <ul>
 *     <li> CASH</li>
 *     <li> NET_CASH_FLOW</li>
 * </ul>
 */
public class YearlyFisherTimeSeries extends TimeSeries<Fisher>
{


    public static final String CASH_COLUMN = "CASH";
    public static final String CASH_FLOW_COLUMN = "NET_CASH_FLOW";
    public static final String FUEL_CONSUMPTION = "FUEL_CONSUMPTION";

    public YearlyFisherTimeSeries() {
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
        registerGatherer(CASH_COLUMN, Fisher::getBankBalance, Double.NaN);

        registerGatherer(CASH_FLOW_COLUMN, new Function<Fisher, Double>() {
            double oldCash = observed.getBankBalance();

            @Override
            public Double apply(Fisher fisher) {
                double flow = fisher.getBankBalance() - oldCash;
                oldCash = fisher.getBankBalance();
                return flow;
            }
        }, Double.NaN);


        registerGatherer(FUEL_CONSUMPTION,
                         fisher -> observed.getYearlyCounterColumn(FUEL_CONSUMPTION),Double.NaN);

        //also aggregate
        for(Specie specie : state.getSpecies())
        {
            final String landings = specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
            registerGatherer(landings,
                             FishStateUtilities.generateYearlySum(observed.getDailyData().getColumn(
                                     landings))
                    , Double.NaN);





        }


        super.start(state, observed);

    }
}

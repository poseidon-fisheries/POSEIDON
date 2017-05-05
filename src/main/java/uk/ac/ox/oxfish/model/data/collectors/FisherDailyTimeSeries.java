package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;

/**
 * Dataset for each fisher being updated once a day
 * Created by carrknight on 8/4/15.
 */
public class FisherDailyTimeSeries extends TimeSeries<Fisher> {


    public static final String CASH_COLUMN = FisherYearlyTimeSeries.CASH_COLUMN;
    public static final String CATCHES_COLUMN_NAME = "Catches";


    public FisherDailyTimeSeries() {
        super(IntervalPolicy.EVERY_DAY);

    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, Fisher observed) {

        registerGatherer(CASH_COLUMN, new Gatherer<Fisher>() {
            @Override
            public Double apply(Fisher fisher) {
                return fisher.getBankBalance();
            }
        }, Double.NaN);


        registerGatherer(FisherYearlyTimeSeries.CASH_FLOW_COLUMN,
                         new Gatherer<Fisher>() {

                              double oldCash = observed.getBankBalance();

        @Override
        public Double apply(Fisher fisher) {
            double flow = fisher.getBankBalance() - oldCash;
            oldCash = fisher.getBankBalance();
            return flow;
        }}, Double.NaN);


        for(Species species : state.getSpecies())
        {
            final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;

            registerGatherer(landings,
                             (new Gatherer<Fisher>() {
                                 @Override
                                 public Double apply(Fisher fisher) {
                                     return fisher.getDailyCounter().getLandingsPerSpecie(species.getIndex());
                                 }
                             }),
                             Double.NaN);

            final String catches = species + " " + CATCHES_COLUMN_NAME;

            registerGatherer(catches,
                             (new Gatherer<Fisher>() {
                                 @Override
                                 public Double apply(Fisher fisher) {
                                     return fisher.getDailyCounter().getCatchesPerSpecie(species.getIndex());
                                 }
                             }),
                             Double.NaN);

        }


        super.start(state, observed);

    }

    @Override
    public void step(SimState simState) {

    /*
        for(int i=0; i< monthlyAverageCatch.length; i++)
        {
            monthlyAverageCatch[i].addObservation(getObserved().getDailyCounter().getLandingsPerSpecie(i));
            monthlyAverageEarnings[i].addObservation(getObserved().getDailyCounter().getEarningsPerSpecie(i));
        }
*/
        super.step(simState);


    }

}

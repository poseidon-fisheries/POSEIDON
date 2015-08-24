package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.model.market.AbstractMarket;

import java.util.function.Function;

/**
 * Dataset for each fisher being updated once a day
 * Created by carrknight on 8/4/15.
 */
public class DailyFisherTimeSeries extends TimeSeries<Fisher> {


    public static final String CASH_COLUMN = YearlyFisherTimeSeries.CASH_COLUMN;



    public DailyFisherTimeSeries() {
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

        registerGatherer(CASH_COLUMN, Fisher::getBankBalance, Double.NaN);

        for(Specie specie : state.getSpecies())
        {
            final String landings = specie + " " + AbstractMarket.LANDINGS_COLUMN_NAME;

            registerGatherer(landings,
                             fisher -> fisher.getDailyCounter().getLandingsPerSpecie(specie.getIndex()),
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

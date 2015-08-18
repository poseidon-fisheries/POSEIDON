package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.MovingAverage;

/**
 * Dataset for each fisher being updated once a day
 * Created by carrknight on 8/4/15.
 */
public class DailyFisherTimeSeries extends TimeSeries<Fisher> {


    public static final String CASH_COLUMN = YearlyFisherTimeSeries.CASH_COLUMN;

    private MovingAverage<Double>[] monthlyAverageCatch;
    private MovingAverage<Double>[] monthlyAverageEarnings;

    public DailyFisherTimeSeries(int numberOfSpecies) {
        super(IntervalPolicy.EVERY_DAY);
  /*
        monthlyAverageCatch = new MovingAverage[numberOfSpecies];
        monthlyAverageEarnings = new MovingAverage[numberOfSpecies];
        for(int i=0; i<numberOfSpecies; i++) {
            monthlyAverageCatch[i] = new MovingAverage<>(90);
            monthlyAverageEarnings[i] = new MovingAverage<>(90);
        }
        */
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
        /*
        for(Specie specie : state.getSpecies())

        {
            registerGatherer("Average Quarterly Landings from " + specie,
                             fisher -> monthlyAverageCatch[specie.getIndex()].getSmoothedObservation(),
                             Double.NaN);
            registerGatherer("Average QuarterlyEarnings from " + specie,
                             fisher -> monthlyAverageCatch[specie.getIndex()].getSmoothedObservation(),
                             Double.NaN);
        }
        */
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

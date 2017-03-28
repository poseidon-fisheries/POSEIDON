package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;

/**
 *
 * like a normal counter but has arrays ready to make catch data faster to read and write
 * Created by carrknight on 8/14/15.
 */
public class FisherDailyCounter extends Counter {

    private double[] landings;

    private double[] earnings;

    public FisherDailyCounter(int numberOfSpecies) {
        super(IntervalPolicy.EVERY_DAY);
        landings = new double[numberOfSpecies];
        earnings = new double[numberOfSpecies];
        super.addColumn(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
        super.addColumn(FisherYearlyTimeSeries.EFFORT);
    }

    @Override
    public void step(SimState simState) {
        super.step(simState);

        for(int i=0; i<landings.length; i++)
        {
            landings[i]=0;
            earnings[i]=0;
        }
    }

    /**
     * increment catch earnings column by this
     *
     * @param add        by how much to increment
     */
    public void countLanding(Species species, double add) {
        landings[species.getIndex()]+=add;
    }

    public void countEarnings(Species species, double add) {
        earnings[species.getIndex()]+=add;
    }

    public double getLandingsPerSpecie(int index)
    {
        return landings[index];
    }

    public double getEarningsPerSpecie(int index)
    {
        return earnings[index];
    }
}

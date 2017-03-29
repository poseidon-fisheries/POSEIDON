package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

/**
 * Simple departing strategy that forces agents to go out only as long as they haven't gone out more than x hours this year.
 *  It isn't meant to be realistic but used when we want to fix effort at a level (for calibration purposes)
 * Created by carrknight on 3/29/17.
 */
public class MaxHoursPerYearDepartingStrategy implements DepartingStrategy {

    private final double maxHoursOut;


    public MaxHoursPerYearDepartingStrategy(double maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }

    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(Fisher fisher, FishState model, MersenneTwisterFast random) {

        return fisher.getYearlyCounterColumn(FisherYearlyTimeSeries.HOURS_OUT) < maxHoursOut;


    }
}

package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The fisher is willing to go out only some months of the year
 * Created by carrknight on 1/6/16.
 */
public class MonthlyDepartingStrategy implements  DepartingStrategy {


    private final boolean allowedAtSea[];


    public MonthlyDepartingStrategy(boolean[] allowedAtSea) {
        Preconditions.checkArgument(allowedAtSea.length == 12);
        this.allowedAtSea = allowedAtSea;
    }

    public MonthlyDepartingStrategy(int... monthsAllowed) {

        allowedAtSea = new boolean[12];
        for(int month : monthsAllowed)
            allowedAtSea[month]=true;

    }

    /**
     * The fisher goes out only on allotted months
     *
     *
     * @param fisher the fisher making the decision
     * @param model the state
     * @param random the randomizer
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        //integer division, gets you the "month" correctly
        int month = (int)(model.getDayOfTheYear() / 30.42);
        assert month>=0;
        assert month<=11;
        return allowedAtSea[month];

    }

    @Override
    public void start(FishState model, Fisher fisher) {
        //doesn't schedule itself
    }

    @Override
    public void turnOff(Fisher fisher) {
        //doesn't need to turn off
    }

    public boolean[] getAllowedAtSea() {
        return allowedAtSea;
    }
}

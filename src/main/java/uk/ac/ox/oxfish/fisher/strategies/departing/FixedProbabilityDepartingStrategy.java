package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple strategy for departure where the fisher decides to get out of port at random with fixed probability
 * Created by carrknight on 4/18/15.
 */
public class FixedProbabilityDepartingStrategy implements DepartingStrategy {

    private final double probabilityToLeavePort;


    public FixedProbabilityDepartingStrategy(double probabilityToLeavePort)
    {
        Preconditions.checkArgument(probabilityToLeavePort >= 0, "Probability can't be negative!");
        Preconditions.checkArgument(probabilityToLeavePort <= 1, "Probability can't be above 1");
        this.probabilityToLeavePort = probabilityToLeavePort;
    }


    /**
     * ignored
     */
    @Override
    public void start(FishState model,Fisher fisher) {

    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher the fisher who is deciding whether to move or not
     * @param model the model. Not used
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(Fisher fisher, FishState model) {
        return fisher.grabRandomizer().nextBoolean(probabilityToLeavePort);
    }


    public double getProbabilityToLeavePort() {
        return probabilityToLeavePort;
    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        //nothing
    }
}


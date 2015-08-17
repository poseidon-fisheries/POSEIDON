package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The fisher must spend x hours at port before going back out
 * Created by carrknight on 8/11/15.
 */
public class FixedRestTimeDepartingStrategy implements DepartingStrategy
{


    private final double minimumHoursToWait;

    public FixedRestTimeDepartingStrategy(double minimumHoursToWait) {
        Preconditions.checkArgument(minimumHoursToWait >= 0);
        this.minimumHoursToWait = minimumHoursToWait;
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, FishState model) {
        return status.getHoursAtPort() >= minimumHoursToWait;
    }

    /**
     */
    @Override
    public void start(FishState model,Fisher fisher) {

    }

    /**
     */
    @Override
    public void turnOff() {
//nothing
    }
}

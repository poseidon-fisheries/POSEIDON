package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.Startable;

/**
 * The strategy used by the fisher to decide whether to leave port or not
 * Created by carrknight on 4/2/15.
 */
public interface DepartingStrategy extends FisherStartable{

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     * @return  true if the fisherman wants to leave port.
     */
    boolean shouldFisherLeavePort(FisherEquipment equipment, FisherStatus status, FisherMemory memory, FishState model);




}

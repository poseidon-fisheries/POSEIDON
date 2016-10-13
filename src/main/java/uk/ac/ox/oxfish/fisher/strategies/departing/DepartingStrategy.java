package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The strategy used by the fisher to decide whether to leave port or not
 * Created by carrknight on 4/2/15.
 */
public interface DepartingStrategy extends FisherStartable{

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     * @return  true if the fisherman wants to leave port.
     */
    boolean shouldFisherLeavePort(Fisher fisher, FishState model, MersenneTwisterFast random);




}

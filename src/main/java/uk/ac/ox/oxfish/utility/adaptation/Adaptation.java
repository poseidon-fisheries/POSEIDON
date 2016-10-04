package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * More of a marker than anything else, tells me that this method adapts some
 * object of class T to a fisher
 * Created by carrknight on 10/4/16.
 */
public interface Adaptation<T> extends FisherStartable {



    /**
     * Ask yourself to adapt
     * @param toAdapt who is doing the adaptation
     * @param state
     * @param random the randomizer
     */
    public void adapt(Fisher toAdapt, FishState state, MersenneTwisterFast random);
}

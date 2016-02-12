package uk.ac.ox.oxfish.model.network;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * A true/false check on whether a connection is allowed
 * Created by carrknight on 2/12/16.
 */
public interface NetworkPredicate {


    /**
     * Used by the network builder to see if this friendship is allowed.
     * @param from origin
     * @param to destination
     * @return true if the connection can be built
     */
    public boolean test(Fisher from, Fisher to);
}

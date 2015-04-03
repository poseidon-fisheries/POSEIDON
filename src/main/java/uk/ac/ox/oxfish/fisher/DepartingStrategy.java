package uk.ac.ox.oxfish.fisher;

/**
 * The strategy used by the fisher to decide whether to leave port or not
 * Created by carrknight on 4/2/15.
 */
public interface DepartingStrategy {

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     * @return  true if the fisherman wants to leave port.
     */
    boolean leavePort();

}

package uk.ac.ox.oxfish.utility.imitation;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 *
 *
 * Created by carrknight on 7/27/15.
 */
public class TowardFriendMovement implements ImitativeMovement
{

    /**
     *  movement along the x axis
     */
    private double xVelocity;

    /**
     * movement along the y axis
     */
    private double yVelocity;

    private double inertia = 0.4;



    @Override
    public SeaTile adapt(
            Fisher fisher, NauticalMap map, SeaTile previous, SeaTile current, double previousFitness,
            double newFitness,
            Fisher bestFriend, double friendFitness, SeaTile friendSeaTile) {
        return null;
    }
}

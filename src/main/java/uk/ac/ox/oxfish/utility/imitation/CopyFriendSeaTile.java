package uk.ac.ox.oxfish.utility.imitation;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Very simple imitative strategy: copy your friend has long as its fitness is better than yours
 *
 * Created by carrknight on 7/27/15.
 */
public class CopyFriendSeaTile implements ImitativeMovement {

    @Override
    public SeaTile adapt(
            Fisher fisher, NauticalMap map, SeaTile previous, SeaTile current, double previousFitness,
            double newFitness,
            Fisher bestFriend, double friendFitness, SeaTile friendSeaTile) {

        //if you have no friend, or you have but his fitness makes no sense or his fitness is lower than yours
        if(bestFriend == null || !Double.isFinite(friendFitness) ||friendSeaTile == null ||
                friendFitness < newFitness || friendFitness < previousFitness)
        {
            //ignore your friend and go back doing what you are doing
            if(previousFitness > newFitness)
                return previous;
            else
                return current;
        }


        //otherwise copy your new friend!
        return friendSeaTile;
    }
}

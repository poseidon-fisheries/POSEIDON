package uk.ac.ox.oxfish.utility.imitation;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Used similarly to IterativeMovement (that is, used by agents to adapt their destination strategies) but adding more information,
 * in particular a reference to a "friend" and his fitness
 * Created by carrknight on 7/27/15.
 */
public interface ImitativeMovement
{


    SeaTile adapt(
            Fisher fisher, NauticalMap map, SeaTile previous, SeaTile current, double previousFitness,
            double newFitness, Fisher bestFriend, double friendFitness, SeaTile friendSeaTile);


}

package uk.ac.ox.oxfish.fisher.equipment;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Technology used to catch fish
 * Created by carrknight on 4/20/15.
 */
public interface Gear {

    public Catch fish(
            Fisher fisher,
            SeaTile where,
            double hoursSpentFishing, GlobalBiology modelBiology);
}

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

    /**
     * get how much gas is consumed by fishing a spot with this gear
     * @param fisher  the dude fishing
     * @param where the location being fished
     * @return liters of gas consumed for every hour spent fishing
     */
    public double getFuelConsumptionPerHourOfFishing(Fisher fisher,
                                                     Boat boat,
                                                     SeaTile where);
}

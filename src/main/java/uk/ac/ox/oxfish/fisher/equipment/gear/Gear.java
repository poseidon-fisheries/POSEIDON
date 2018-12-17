/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Technology used to catch fish
 * Created by carrknight on 4/20/15.
 */
public interface Gear {

    Catch fish(
            Fisher fisher,
            LocalBiology localBiology,
            SeaTile context, int hoursSpentFishing,
            GlobalBiology modelBiology);

    /**
     * get how much gas is consumed by fishing a spot with this gear
     * @param fisher  the dude fishing
     * @param where the location being fished
     * @return liters of gas consumed for every hour spent fishing
     */
    double getFuelConsumptionPerHourOfFishing(
            Fisher fisher,
            Boat boat,
            SeaTile where);


    double[] expectedHourlyCatch(
            Fisher fisher,
            SeaTile where,
            int hoursSpentFishing,
            GlobalBiology modelBiology
    );


    Gear makeCopy();

    /**
     * are the two gears the same (keeping away from modifying equals)
     * @param o gear we are comparing to
     * @return
     */
    public boolean isSame(Gear o);
}

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

package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A record describing the achieved catch for a specific instance of fishing
 * Created by carrknight on 6/30/15.
 */
public class FishingRecord
{


    /**
     * hours spent fishing.
     */
    final private int hoursSpentFishing;


    /**
     * where did fishing occur
     */
    final private SeaTile tileFished;

    /**
     * the catch
     */
    final private Catch fishCaught;




    public FishingRecord(
            int hoursSpentFishing, SeaTile tileFished, Catch fishCaught) {
        this.hoursSpentFishing = hoursSpentFishing;
        this.tileFished = tileFished;
        this.fishCaught = fishCaught;
    }

    public int getHoursSpentFishing() {
        return hoursSpentFishing;
    }


    public SeaTile getTileFished() {
        return tileFished;
    }

    public Catch getFishCaught() {
        return fishCaught;
    }

    /**
     * adds to fishing records together; fails if they belong to different sea-tiles!
     * @param original the original
     * @param newRecord
     * @return
     */
    public static  FishingRecord sumRecords(
            FishingRecord original,
            FishingRecord newRecord
    ){

        Preconditions.checkArgument(original.tileFished==newRecord.tileFished,
                                    "Fishing records do not belong to same tile!");
        return new FishingRecord(
                        original.hoursSpentFishing + newRecord.hoursSpentFishing,
                        original.getTileFished(),
                        Catch.sumCatches(original.getFishCaught(),newRecord.getFishCaught())



        );
    }


}

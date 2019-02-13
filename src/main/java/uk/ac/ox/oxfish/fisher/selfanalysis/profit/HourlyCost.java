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

package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Simple fixed $/hr cost
 * Created by carrknight on 7/13/16.
 */
public class HourlyCost implements Cost{

    private final double hourlyCost;


    public HourlyCost(double hourlyCost) {
        this.hourlyCost = hourlyCost;
    }


    /**
     * computes and return the cost
     *  @param fisher  agent that did the trip
     * @param model
     * @param record  the trip record
     * @param revenue revenue from catches   @return $ spent
     * @param durationInHours
     */
    @Override
    public double cost(
            Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours) {
        return hourlyCost * durationInHours;
    }

    public double getHourlyCost() {
        return hourlyCost;
    }
}

/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

public class EffortCost implements Cost {


    private double costPerHourSpentFishing;

    public EffortCost(double costPerHourSpentFishing) {
        this.costPerHourSpentFishing = costPerHourSpentFishing;
        Preconditions.checkArgument(costPerHourSpentFishing > 0);
        Preconditions.checkArgument(Double.isFinite(costPerHourSpentFishing));
    }

    @Override
    public double cost(Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours) {
        return costPerHourSpentFishing * record.getEffort();
    }

    public double getCostPerHourSpentFishing() {
        return costPerHourSpentFishing;
    }

    public void setCostPerHourSpentFishing(double costPerHourSpentFishing) {
        this.costPerHourSpentFishing = costPerHourSpentFishing;
    }

    @Override
    public double expectedAdditionalCosts(
        Fisher fisher,
        double additionalTripHours,
        double additionalEffortHours,
        double additionalKmTravelled
    ) {
        return additionalEffortHours * costPerHourSpentFishing;
    }
}

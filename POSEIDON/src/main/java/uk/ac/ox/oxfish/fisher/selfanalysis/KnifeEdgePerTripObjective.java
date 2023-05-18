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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

/**
 * The simplest kind of satistificing utility function: trip made more than x? utility is 1, else utility is -1
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgePerTripObjective extends TripBasedObjectiveFunction {

    /**
     * should I count opportunity costs?
     */
    private final boolean opportunityCosts;

    /**
     * threshold above (and equal) to which your utility is +1 else is -1
     */
    private final double minimumProfitPerHour;


    public KnifeEdgePerTripObjective(boolean opportunityCosts, double minimumProfitPerHour) {
        this.opportunityCosts = opportunityCosts;
        this.minimumProfitPerHour = minimumProfitPerHour;
    }

    @Override
    protected double extractUtilityFromTrip(
        Fisher observer, TripRecord tripRecord, Fisher Observed
    ) {
        return tripRecord.getProfitPerHour(opportunityCosts) >= minimumProfitPerHour ?
            +1 : -1;
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public boolean isOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * Getter for property 'minimumProfitPerHour'.
     *
     * @return Value for property 'minimumProfitPerHour'.
     */
    public double getMinimumProfitPerHour() {
        return minimumProfitPerHour;
    }
}

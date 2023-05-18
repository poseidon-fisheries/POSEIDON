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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;

/**
 * Ignores earnings by all fish that isn't the one targeted
 * Created by carrknight on 3/24/16.
 */
public class TargetSpeciesTripObjective extends TripBasedObjectiveFunction {


    private final Species species;

    private final boolean opportunityCosts;

    public TargetSpeciesTripObjective(Species species, boolean opportunityCosts) {
        this.species = species;
        this.opportunityCosts = opportunityCosts;
    }

    /**
     * the utility is earnings for selected species - total costs
     *
     * @param observer
     * @param tripRecord
     * @return
     */
    @Override
    protected double extractUtilityFromTrip(
        Fisher observer, TripRecord tripRecord, Fisher Observed
    ) {
        double profits = tripRecord.getEarningsOfSpecies(species.getIndex()) - tripRecord.getTotalCosts();
        profits = opportunityCosts ? profits - tripRecord.getOpportunityCosts() : profits;
        profits /= tripRecord.getDurationInHours();
        return profits;
    }
}

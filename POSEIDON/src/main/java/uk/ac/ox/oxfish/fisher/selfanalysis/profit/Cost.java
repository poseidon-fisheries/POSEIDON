/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A cost computation, to be used by fishers to properly estimate profits
 * Created by carrknight on 7/12/16.
 */
public interface Cost {


    /**
     * computes and return the cost
     *
     * @param fisher          agent that did the trip
     * @param record          the trip record
     * @param revenue         revenue from catches
     * @param durationInHours this is given as an argument because when the fisher is acting for real this is called
     *                        by
     * @return dollars spent
     */
    public double cost(
        Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours
    );

    /**
     * ask the cost object to return the $ spent for a trip modification
     *
     * @param additionalTripHours   the amount of hours you will spend out (travelling + fishing + whatever)
     * @param additionalEffortHours the additional amount of hours you will spend as "effort" (so not travelling, pure fishing)
     * @param additionalKmTravelled the additional distance travelled.
     */
    public double expectedAdditionalCosts(
        Fisher fisher,
        double additionalTripHours,
        double additionalEffortHours,
        double additionalKmTravelled
    );
}

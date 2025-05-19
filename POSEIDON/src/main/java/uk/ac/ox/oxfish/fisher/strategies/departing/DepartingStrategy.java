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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The strategy used by the fisher to decide whether to leave port or not
 * Created by carrknight on 4/2/15.
 */
public interface DepartingStrategy extends FisherStartable {

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @return true if the fisherman wants to leave port.
     */
    boolean shouldFisherLeavePort(Fisher fisher, FishState model, MersenneTwisterFast random);


    /**
     * The fisher asks himself how many more days out at sea they can go out this year
     *
     * @return number of days the fisher thinks it can still go out
     */
    default int predictedDaysLeftFishingThisYear(Fisher fisher, FishState model, MersenneTwisterFast random) {
        return 365 - model.getDayOfTheYear();
    }


}

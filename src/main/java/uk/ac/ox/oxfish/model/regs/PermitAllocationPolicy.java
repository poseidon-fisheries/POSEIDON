/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * allocation policy is a function that tells me which agents are allowed to participate in
 * a fishery next year
 */
public interface PermitAllocationPolicy {


    /**
     * given all the fishers who participates, return a list of those that will be
     * allowed to participate in the fishery (those that are not returned are assumed to be banned)
     * @param participants ALL the fishers subject to the regulation (both allowed and not)
     * @param state model
     * @return a list of those fishers that are allowed to go out next year
     */
    public List<Fisher> computeWhichFishersAreAllowed(
            List<Fisher> participants,
            FishState state

    );




}

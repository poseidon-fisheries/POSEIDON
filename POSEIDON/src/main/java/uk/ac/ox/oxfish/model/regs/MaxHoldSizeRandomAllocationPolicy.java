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

import java.util.*;

/**
 * goes through the list of agents at random and allows enough participants to fill as much of the quota left as possible(defined in hold size)
 */
public class MaxHoldSizeRandomAllocationPolicy implements PermitAllocationPolicy {


    /**
     * target hold size to achieve
     */
    private double maxCumulativeHoldSize;


    public MaxHoldSizeRandomAllocationPolicy(double maxCumulativeHoldSize) {
        this.maxCumulativeHoldSize = maxCumulativeHoldSize;
    }

    /**
     * given all the fishers who participates, return a list of those that will be
     * allowed to participate in the fishery (those that are not returned are assumed to be banned)
     *
     * @param participants ALL the fishers subject to the regulation (both allowed and not)
     * @param state        model
     * @return a list of those fishers that are allowed to go out next year
     */
    @Override
    public List<Fisher> computeWhichFishersAreAllowed(
            List<Fisher> participants, FishState state) {


        System.out.println(state.getYear() + " --- " + state.getDay());

        ArrayList<Fisher> participantsCopy = new ArrayList<>(participants);
        Collections.shuffle(participantsCopy,new Random(state.getRandom().nextLong()));


        List<Fisher> allowedFishers = new LinkedList<>();
        double sumOfHoldSize = 0;
        for (Fisher fisher : participantsCopy) {
            if(fisher.getMaximumHold() + sumOfHoldSize <= maxCumulativeHoldSize)
            {
                allowedFishers.add(fisher);
                sumOfHoldSize+=fisher.getMaximumHold();
            }
        }

        return allowedFishers;


    }


    /**
     * Getter for property 'maxCumulativeHoldSize'.
     *
     * @return Value for property 'maxCumulativeHoldSize'.
     */
    public double getMaxCumulativeHoldSize() {
        return maxCumulativeHoldSize;
    }

    /**
     * Setter for property 'maxCumulativeHoldSize'.
     *
     * @param maxCumulativeHoldSize Value to set for property 'maxCumulativeHoldSize'.
     */
    public void setMaxCumulativeHoldSize(double maxCumulativeHoldSize) {
        this.maxCumulativeHoldSize = maxCumulativeHoldSize;
    }
}

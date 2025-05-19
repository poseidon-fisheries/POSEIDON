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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * a permit allocation policy that is simply given a list of percentages of boats to allow out each year.
 * There is no randomness: the boats are allowed by order given by the ID (so for example .27 means that the first 27% of the boats are allowed out
 * as ordered by their ID). <br>
 * total_number_of_boats = floor(max * percentage) <br>
 * <p>
 * If there are more years simulated than the list of percentages given, just always return the last given percentage
 * <p>
 * This is mostly useful to force a fixed effort through the years (which is useful for MERA comparisons)
 */
public class ExogenousPercentagePermitAllocation implements PermitAllocationPolicy {


    private final List<Double> yearlyPercentagesOfAllowedEffort;

    public ExogenousPercentagePermitAllocation(List<Double> yearlyPercentagesOfAllowedEffort) {
        this.yearlyPercentagesOfAllowedEffort = yearlyPercentagesOfAllowedEffort;
    }

    @Override
    public List<Fisher> computeWhichFishersAreAllowed(List<Fisher> participants, FishState state) {

        final int index = Math.min(state.getYear(), yearlyPercentagesOfAllowedEffort.size() - 1);
        final int boatsAllowed = (int) Math.floor(participants.size() * yearlyPercentagesOfAllowedEffort.get(index));

        final ArrayList<Fisher> reordered = Lists.newArrayList(participants);
        Collections.sort(
            reordered,
            (firstFisher, secondFisher) -> Integer.compare(firstFisher.getID(), secondFisher.getID())
        );

        List<Fisher> toReturn = new ArrayList<>(boatsAllowed);
        for (int i = 0; i < boatsAllowed; i++) {
            toReturn.add(reordered.get(i));
        }


        return toReturn;
    }
}

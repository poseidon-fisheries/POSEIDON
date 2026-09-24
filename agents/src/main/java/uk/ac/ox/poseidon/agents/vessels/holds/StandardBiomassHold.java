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

package uk.ac.ox.poseidon.agents.vessels.holds;

import lombok.Getter;
import uk.ac.ox.poseidon.agents.catches.CatchCategoriser;
import uk.ac.ox.poseidon.agents.catches.CategorisedCatch;

/**
 * A {@link BiomassHold} with a fixed capacity, plus a small tolerance overage allowed before
 * {@link #addContent(CategorisedCatch)} rejects content outright.
 */
@Getter
public class StandardBiomassHold extends BiomassHold {

    private final double totalCapacityInKg;
    private final double toleranceInKg;

    /**
     * @param catchCategoriser  sorts uncategorised content added to the hold
     * @param totalCapacityInKg the hold's total capacity, in kilograms
     * @param toleranceInKg     how far over {@code totalCapacityInKg} the hold may go before
     *                          {@link #addContent(CategorisedCatch)} rejects further content
     */
    public StandardBiomassHold(
        final CatchCategoriser catchCategoriser,
        final double totalCapacityInKg,
        final double toleranceInKg
    ) {
        super(catchCategoriser);
        this.totalCapacityInKg = totalCapacityInKg;
        this.toleranceInKg = toleranceInKg;
    }

    /**
     * @throws IllegalStateException if adding {@code categorisedCatch} would exceed
     *                                {@link #totalCapacityInKg} plus {@link #toleranceInKg}
     */
    @Override
    public void addContent(final CategorisedCatch categorisedCatch) {
        final CategorisedCatch newContent = content.add(categorisedCatch);
        if (newContent.getTotalBiomassInKg() <= totalCapacityInKg + toleranceInKg) {
            content = newContent;
        } else {
            throw new IllegalStateException(
                "Trying to store %f kg in the hold, but only %f kg of capacity available."
                    .formatted(
                        categorisedCatch.getTotalBiomassInKg(),
                        getAvailableCapacityInKg()
                    )
            );
        }
    }

}

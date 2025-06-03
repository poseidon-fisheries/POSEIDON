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

package uk.ac.ox.poseidon.agents.vessels.hold;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class StandardBiomassHold implements Hold<Biomass> {

    private final double totalCapacityInKg;
    private final double toleranceInKg;
    private Map<String, Bucket<Biomass>> content = new HashMap<>();

    @Override
    public void addContent(
        final String categoryCode,
        final Bucket<Biomass> contentToAdd
    ) {
        final Bucket<Biomass> newBucket =
            content
                .getOrDefault(categoryCode, Bucket.empty())
                .add(contentToAdd);
        if (newBucket.getTotalBiomass().asKg() <= totalCapacityInKg + toleranceInKg) {
            content.put(categoryCode, newBucket);
        } else {
            throw new IllegalStateException(
                "Trying to store %f kg in the hold, but only %f kg of capacity available."
                    .formatted(
                        contentToAdd.getTotalBiomass().asKg(),
                        getAvailableCapacityInKg()
                    )
            );
        }
    }

    @Override
    public Map<String, Bucket<Biomass>> extractContent() {
        final Map<String, Bucket<Biomass>> removedContent = content;
        content = new HashMap<>();
        return removedContent;
    }

}

/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.vessels.hold;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

@Getter
@RequiredArgsConstructor
public class StandardBiomassHold implements Hold<Biomass> {

    private final double totalCapacityInKg;
    private final double toleranceInKg;
    private Bucket<Biomass> content = Bucket.empty();

    @Override
    public void addContent(final Bucket<Biomass> contentToAdd) {
        final Bucket<Biomass> newContent = content.add(contentToAdd);
        if (newContent.getTotalBiomass().asKg() <= totalCapacityInKg + toleranceInKg) {
            content = newContent;
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
    public Bucket<Biomass> extractContent() {
        final Bucket<Biomass> removedContent = content;
        content = Bucket.empty();
        return removedContent;
    }

}

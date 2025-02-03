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
import uk.ac.ox.poseidon.agents.vessels.hold.OvercapacityDiscardingStrategy.Result;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

@RequiredArgsConstructor
public class StandardBiomassHold implements Hold<Biomass> {

    private final double capacityInKg;
    private final double toleranceInKg;
    private final OvercapacityDiscardingStrategy<Biomass> overcapacityDiscardingStrategy;
    @Getter
    private Bucket<Biomass> content = Bucket.empty();

    @Override
    public Bucket<Biomass> addContent(final Bucket<Biomass> contentToAdd) {
        final Bucket<Biomass> newContent = content.add(contentToAdd);
        if (newContent.getTotalBiomass().asKg() <= capacityInKg + toleranceInKg) {
            content = newContent;
            return Bucket.empty();
        } else {
            final Result<Biomass> discardingResult =
                overcapacityDiscardingStrategy.discard(
                    contentToAdd,
                    content,
                    capacityInKg,
                    toleranceInKg
                );
            content = discardingResult.updatedHoldContent;
            return discardingResult.discarded;
        }
    }

    @Override
    public boolean isFull() {
        return content.getTotalBiomass().asKg() >= capacityInKg - toleranceInKg;
    }

    @Override
    public Bucket<Biomass> removeContent() {
        final Bucket<Biomass> removedContent = content;
        content = Bucket.empty();
        return removedContent;
    }
}

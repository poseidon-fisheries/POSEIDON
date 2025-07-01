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

import static java.lang.Double.POSITIVE_INFINITY;

@Getter
@RequiredArgsConstructor
public class InfiniteBiomassHold implements Hold<Biomass> {

    private Bucket<Biomass> content = Bucket.empty();

    @Override
    public void addContent(final Bucket<Biomass> contentToAdd) {
        content.add(contentToAdd);
    }

    @Override
    public Bucket<Biomass> extractContent() {
        final Bucket<Biomass> removedContent = content;
        content = Bucket.empty();
        return removedContent;
    }

    @Override
    public double getTotalCapacityInKg() {
        return POSITIVE_INFINITY;
    }

}

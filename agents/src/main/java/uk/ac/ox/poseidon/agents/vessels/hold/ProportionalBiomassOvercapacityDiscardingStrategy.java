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

import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

public class ProportionalBiomassOvercapacityDiscardingStrategy
    implements OvercapacityDiscardingStrategy<Biomass> {
    @Override
    public Result<Biomass> discard(
        final Bucket<Biomass> contentToAdd,
        final Bucket<Biomass> currentHoldContent,
        final double capacityInKg,
        final double toleranceInKg
    ) {
        final double contentToAddInKg = contentToAdd.getTotalBiomass().asKg();
        final double currentContentInKg = currentHoldContent.getTotalBiomass().asKg();
        final double availableCapacityInKg = capacityInKg - currentContentInKg;
        if (contentToAddInKg <= availableCapacityInKg) {
            return new Result<>(
                Bucket.empty(),
                currentHoldContent.add(contentToAdd)
            );
        } else {
            final double proportionToKeep = availableCapacityInKg / contentToAddInKg;
            final Bucket<Biomass> contentToKeep =
                contentToAdd.mapContent(biomass ->
                    biomass.multiply(proportionToKeep)
                );
            return new Result<>(
                contentToAdd.subtract(contentToKeep),
                currentHoldContent.add(contentToKeep)
            );
        }
    }
}

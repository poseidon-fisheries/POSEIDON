/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.catches.disposition;

import lombok.NonNull;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.BiomassBucket;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

/**
 * Applies per-species discard proportions without enforcing capacity limits. Compose with
 * {@code ProportionallyLimitingBiomassToHold} if capacity limiting is required.
 */
public class ProportionalSpeciesBiomassRetention implements DispositionProcess {

    private final @NonNull SpeciesIndexedDoubleArray proportionsToDiscard;

    public ProportionalSpeciesBiomassRetention(
        @NonNull final SpeciesIndexedDoubleArray proportionsToDiscard
    ) {
        proportionsToDiscard.forEachValue(proportion ->
            checkUnitRange(proportion, "proportion")
        );
        this.proportionsToDiscard = proportionsToDiscard;
    }

    @Override
    public Disposition partition(
        final Disposition currentDisposition,
        final double availableCapacityInKg
    ) {
        final Bucket discarded =
            switch (currentDisposition.getRetained()) {
                case final BiomassBucket currentlyRetained
                    when currentlyRetained.sameIndex(proportionsToDiscard) ->
                    currentlyRetained.mapBiomassValueWithIndex((biomass, index) ->
                        proportionsToDiscard.getDouble(index) * biomass
                    );
                case final Bucket currentlyRetained -> currentlyRetained.map((species, content) ->
                    Biomass.ofKg(proportionsToDiscard.getOrDefault(species, 0) * content.asKg())
                );
            };
        return new Disposition(
            currentDisposition.getRetained().subtract(discarded),
            currentDisposition.getDiscardedAlive().add(discarded),
            currentDisposition.getDiscardedDead()
        );
    }

}

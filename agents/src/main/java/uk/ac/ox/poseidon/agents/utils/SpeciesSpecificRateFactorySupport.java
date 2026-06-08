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

package uk.ac.ox.poseidon.agents.utils;

import org.apache.commons.collections4.keyvalue.MultiKey;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.biology.species.SpeciesIndexedDoubleArray;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

public final class SpeciesSpecificRateFactorySupport {

    private SpeciesSpecificRateFactorySupport() {
    }

    public static SpeciesIndexedDoubleArray buildRatesByKey(
        final Collection<? extends Species> species,
        final Map<MultiKey<Object>, Double> ratesBySpeciesKey,
        final String label
    ) {
        if (ratesBySpeciesKey == null) {
            throw new IllegalArgumentException("ratesBySpeciesKey should not be null");
        }
        final SpeciesIndex speciesIndex = SpeciesIndex.of(Set.copyOf(species));
        final Set<MultiKey<Object>> knownKeys =
            species.stream().map(Species::getKey).collect(Collectors.toSet());

        ratesBySpeciesKey.forEach((key, rate) -> {
            checkArgument(
                knownKeys.contains(key),
                "Unknown species key '%s' in %s",
                key,
                label
            );
            checkUnitRange(rate, label);
        });

        final double[] rates = speciesIndex.newDoubleArray();
        for (int i = 0; i < speciesIndex.size(); i++) {
            final Species indexSpecies = speciesIndex.speciesAt(i);
            final Double rate = ratesBySpeciesKey.get(indexSpecies.getKey());
            rates[i] = rate == null ? 0.0 : rate;
        }

        return SpeciesIndexedDoubleArray.of(rates, speciesIndex);
    }
}

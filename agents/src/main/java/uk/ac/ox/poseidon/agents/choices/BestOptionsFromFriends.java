/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegister;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class BestOptionsFromFriends<O> implements Supplier<OptionValues<O>> {

    private final VesselComponentRegister<? extends OptionValues<O>> optionValuesRegister;
    private final Supplier<? extends Iterable<? extends Vessel>> friendsSupplier;

    @Override
    public OptionValues<O> get() {
        final Map<O, Double> aggregatedValues = new HashMap<>();
        for (final Vessel friend : friendsSupplier.get()) {
            if (!friend.isActive()) continue;
            final OptionValues<O> values = optionValuesRegister
                .getComponent(friend)
                .orElse(null);
            if (values == null) continue;
            for (final Entry<O, Double> entry : values.getBestEntries()) {
                final Double existing = aggregatedValues.get(entry.getKey());
                final double newValue = entry.getValue();
                if (existing == null || newValue > existing) {
                    aggregatedValues.put(entry.getKey(), newValue);
                }
            }
        }
        return new ImmutableOptionValues<>(aggregatedValues);
    }
}

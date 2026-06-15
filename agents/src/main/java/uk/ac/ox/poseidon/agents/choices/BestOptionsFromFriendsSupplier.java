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

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegister;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.upToNOf;

@RequiredArgsConstructor(access = PACKAGE)
class BestOptionsFromFriendsSupplier<O> implements Supplier<OptionValues<O>> {

    private final Vessel vessel;
    private final int maxNumberOfFriends;
    private final VesselComponentRegister<? extends OptionValues<O>> optionValuesRegister;
    private final MersenneTwisterFast rng;

    private final @Getter(lazy = true) ImmutableList<Vessel> friends = chooseFriends();

    private ImmutableList<Vessel> chooseFriends() {
        assert optionValuesRegister != null;
        assert this.vessel != null;
        return upToNOf(
            maxNumberOfFriends,
            optionValuesRegister
                .getVessels()
                .filter(Vessel::isActive)
                .filter(vessel -> vessel.getHomePort() == this.vessel.getHomePort())
                .filter(vessel -> vessel != this.vessel)
                .toList(),
            rng
        );
    }

    @Override
    public OptionValues<O> get() {
        final Map<O, Double> aggregatedValues = new HashMap<>();
        for (final Vessel friend : getFriends()) {
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

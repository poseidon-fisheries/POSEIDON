/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.behaviours.choices;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.registers.Register;
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
    private final Register<? extends OptionValues<O>> optionValuesRegister;
    private final MersenneTwisterFast rng;

    private final @Getter(lazy = true) ImmutableList<Vessel> friends = chooseFriends();

    private ImmutableList<Vessel> chooseFriends() {
        assert optionValuesRegister != null;
        assert this.vessel != null;
        return upToNOf(
            maxNumberOfFriends,
            optionValuesRegister
                .getVessels()
                .filter(vessel -> vessel.getHomePort() == this.vessel.getHomePort())
                .filter(vessel -> vessel != this.vessel)
                .toList(),
            rng
        );
    }

    @Override

    public OptionValues<O> get() {
        // It's faster to build a hashmap first and then let
        // ImmutableOptionValues copy it into an ImmutableMap
        final Map<O, Double> aggregatedValues = new HashMap<>();
        for (final Vessel friend : getFriends()) {
            optionValuesRegister
                .get(friend)
                .ifPresent(values -> {
                    for (final Entry<O, Double> entry : values.getBestEntries()) {
                        aggregatedValues.merge(entry.getKey(), entry.getValue(), Math::max);
                    }
                });
        }
        return new ImmutableOptionValues<>(aggregatedValues);
    }
}

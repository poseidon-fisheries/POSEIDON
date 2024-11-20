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

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.registers.Register;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

@RequiredArgsConstructor
public class BestOptionsSupplier<O> implements Supplier<OptionValues<O>> {

    private final Vessel vessel;
    private final Register<? extends OptionValues<O>> optionValuesRegister;

    @Override
    public OptionValues<O> get() {
        return new ImmutableOptionValues<>(
            optionValuesRegister
                .getOtherEntries(vessel)
                .map(Map.Entry::getValue)
                .flatMap(optionValues -> optionValues.getBestEntries().stream())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, Math::max))
        );
    }
}

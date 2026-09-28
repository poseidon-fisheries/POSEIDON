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

import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static lombok.AccessLevel.PACKAGE;

/**
 * Supplies the best options across every other active vessel's own {@link OptionValues}
 * component in a {@link VesselComponentRegister}, merging ties by keeping the highest value seen
 * for each option.
 */
@RequiredArgsConstructor(access = PACKAGE)
public class BestOptions<O> implements Supplier<OptionValues<O>> {

    private final Vessel vessel;
    private final VesselComponentRegister<? extends OptionValues<O>> optionValuesRegister;

    /**
     * @return an {@link ImmutableOptionValues} snapshot of every active other vessel's best
     * options, merged by keeping the highest value for each option
     */
    @Override
    public OptionValues<O> get() {
        return new ImmutableOptionValues<>(
            optionValuesRegister
                .getOtherEntries(vessel)
                .filter(entry -> entry.getKey().isActive())
                .map(Map.Entry::getValue)
                .flatMap(optionValues -> optionValues.getBestEntries().stream())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, Math::max))
        );
    }
}

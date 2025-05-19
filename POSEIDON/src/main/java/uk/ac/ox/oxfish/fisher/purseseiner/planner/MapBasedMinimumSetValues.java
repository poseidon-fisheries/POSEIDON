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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class MapBasedMinimumSetValues implements MinimumSetValues {

    private final Map<Integer, Map<ActionClass, Double>> minimumSetValues;

    public MapBasedMinimumSetValues(final Map<Integer, Map<ActionClass, Double>> minimumSetValues) {
        this.minimumSetValues =
            minimumSetValues.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> ImmutableMap.copyOf(entry.getValue())
            ));
    }

    @Override
    public double getMinimumSetValue(
        final int year,
        final ActionClass actionClass
    ) {
        return minimumSetValues.get(year).get(actionClass);
    }
}

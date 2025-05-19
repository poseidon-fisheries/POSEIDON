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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class FixedLocationValues extends ObservableLocationValues {

    private final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader;
    // nothing to observe when values are fixed, but required by the LocationValues interface
    private Map<Int2D, Double> values;

    FixedLocationValues(final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader) {
        this.valueLoader = valueLoader;
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        this.values = ImmutableMap.copyOf(valueLoader.apply(fisher));
    }

    @Override
    public double getValueAt(final Int2D location) {
        return checkNotNull(values).getOrDefault(location, 0.0);
    }

    @Override
    public Set<Map.Entry<Int2D, Double>> getValues() {
        return values.entrySet();
    }

}

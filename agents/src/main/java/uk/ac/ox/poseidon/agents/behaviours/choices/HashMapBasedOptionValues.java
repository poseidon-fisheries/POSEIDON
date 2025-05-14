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

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PACKAGE;

@Getter
@NoArgsConstructor(access = PACKAGE)
public abstract class HashMapBasedOptionValues<O>
    extends MapBasedOptionValues<O>
    implements MutableOptionValues<O> {

    protected final Map<O, Double> values = new HashMap<>();

    @Override
    public void observe(
        final O option,
        final double value
    ) {
        final double oldValue = values.getOrDefault(option, 0.0);
        values.put(option, newValue(option, oldValue, value));
        invalidateCache();
    }

    protected void invalidateCache() {
        cachedBest = null;
    }

    protected abstract double newValue(
        O option,
        double oldValue,
        double observedValue
    );
}

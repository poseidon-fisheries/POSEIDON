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

import java.util.HashMap;
import java.util.Map;

public class AveragingOptionValues<T> extends MapBasedOptionValues<T> {

    private final Map<T, Integer> counts = new HashMap<>();

    @Override
    public void observe(
        final T option,
        final double value
    ) {
        final int count = counts.getOrDefault(option, 0);
        counts.put(option, count + 1);
        final double oldValue = values.getOrDefault(option, 0.0);
        values.put(option, (count * oldValue + value) / (count + 1));
        invalidateCache();
    }

}

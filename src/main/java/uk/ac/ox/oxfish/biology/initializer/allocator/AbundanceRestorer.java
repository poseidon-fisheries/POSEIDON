/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.util.Map;
import java.util.Map.Entry;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.allocator.SmallLargeAllocationGridsSupplier.SizeGroup;

public class AbundanceRestorer extends Restorer<Entry<String, SizeGroup>, double[][], AbundanceLocalBiology> {

    AbundanceRestorer(
        final AbundanceReallocator reallocator,
        final AbundanceAggregator aggregator,
        final Map<Integer, Integer> schedule
    ) {
        super(reallocator, aggregator, schedule);
    }

    @Override
    public Map<Species, double[][]> subtract(
        final Map<Species, double[][]> initialAggregations,
        final Map<Species, double[][]> aggregationsToSubtract
    ) {
        return initialAggregations.entrySet().stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> {
                    final DoubleGrid2D snapshotAbundance =
                        new DoubleGrid2D(entry.getValue());
                    final DoubleGrid2D fadsAbundance =
                        new DoubleGrid2D(aggregationsToSubtract.get(entry.getKey()));
                    return snapshotAbundance.add(fadsAbundance.multiply(-1)).getField();
                }
            ));
    }

}

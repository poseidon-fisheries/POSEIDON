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

package uk.ac.ox.oxfish.biology.tuna;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import java.util.Map;
import java.util.Map.Entry;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;

public class AbundanceRestorer
    extends Restorer<Entry<String, SizeGroup>, AbundanceLocalBiology> {

    AbundanceRestorer(
        final AbundanceReallocator reallocator,
        final AbundanceAggregator aggregator,
        final Map<Integer, Integer> schedule
    ) {
        super(reallocator, aggregator, schedule);
    }

    @Override
    AbundanceLocalBiology subtract(
        final GlobalBiology globalBiology,
        final AbundanceLocalBiology aggregatedBiology,
        final AbundanceLocalBiology biologyToSubtract
    ) {
        return new AbundanceLocalBiology(
            globalBiology.getSpecies()
                .stream()
                .collect(toImmutableMap(
                    identity(),
                    species -> subtractMatrices(
                        aggregatedBiology.getAbundance(species).asMatrix(),
                        biologyToSubtract.getAbundance(species).asMatrix()
                    )
                ))
        );
    }

    // Use DoubleGrid2D as an easy way to subtract the two matrices
    private static double[][] subtractMatrices(
        final double[][] initialAggregation,
        final double[][] aggregationToSubtract
    ) {
        return new DoubleGrid2D(initialAggregation)
            .add(new DoubleGrid2D(aggregationToSubtract).multiply(-1))
            .getField();
    }
}

/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

class AbundanceExcluder extends Excluder<AbundanceLocalBiology> {

    AbundanceExcluder(
        final Extractor<AbundanceLocalBiology> extractor,
        final Aggregator<AbundanceLocalBiology> aggregator
    ) {
        super(extractor, aggregator);
    }

    @Override
    AbundanceLocalBiology exclude(
        final AbundanceLocalBiology aggregatedBiology,
        final AbundanceLocalBiology biologyToExclude
    ) {
        return new AbundanceLocalBiology(
            aggregatedBiology
                .getAbundance()
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> AbundanceExcluder.subtractMatrices(
                        entry.getValue(),
                        biologyToExclude.getAbundance().get(entry.getKey())
                    )
                ))
        );
    }

    /**
     * Use DoubleGrid2D as an easy way to subtract the two matrices. There might be a bit of
     * overhead in instantiating the DoubleGrid2D objects, but we'll' deal with it if it turns out
     * to be a problem.
     */
    private static double[][] subtractMatrices(
        final double[][] initialAggregation,
        final double[][] aggregationToSubtract
    ) {
        return new DoubleGrid2D(initialAggregation)
            .add(new DoubleGrid2D(aggregationToSubtract).multiply(-1))
            .lowerBound(0) // cannot subtract what is not there
            .getField();
    }
}

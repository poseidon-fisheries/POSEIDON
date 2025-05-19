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

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

abstract class Excluder<B extends LocalBiology> {

    private final Aggregator<B> aggregator;
    private final Extractor<B> extractor;

    Excluder(
        final Extractor<B> extractor,
        final Aggregator<B> aggregator
    ) {
        this.extractor = extractor;
        this.aggregator = aggregator;
    }

    public Aggregator<B> getAggregator() {
        return aggregator;
    }

    public Extractor<B> getExtractor() {
        return extractor;
    }

    B exclude(final B originalBiology, final FishState fishState) {
        final List<B> biologiesToExclude = extractor.apply(fishState);
        if (biologiesToExclude.isEmpty()) {
            return originalBiology;
        } else {
            B aggregatedBiology = aggregator.apply(fishState.getBiology(), biologiesToExclude);
            B newBiology = exclude(originalBiology, aggregatedBiology);
            return newBiology;
        }
    }

    abstract B exclude(B aggregatedBiology, B biologyToExclude);

}

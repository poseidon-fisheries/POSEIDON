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

import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;

/**
 * This is a {@link Restorer} that works with {@link AbundanceLocalBiology}.
 */
public class AbundanceRestorer
    extends Restorer<Entry<String, SizeGroup>, AbundanceLocalBiology> {

    AbundanceRestorer(
        final AbundanceReallocator reallocator,
        final AbundanceAggregator aggregator,
        final Map<Integer, Integer> schedule
    ) {
        super(
            reallocator,
            aggregator,
            new AbundanceExtractorProcess(false, true),
            new FadAbundanceExcluder(),
            schedule
        );
    }

}
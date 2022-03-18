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
import static com.google.common.collect.Iterables.get;
import static java.util.function.UnaryOperator.identity;

import java.util.Collection;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;

/**
 * An {@link Aggregator} that works with {@link AbundanceLocalBiology} biologies.
 */
class AbundanceAggregator extends Aggregator<AbundanceLocalBiology> {

    @Override
    public AbundanceLocalBiology apply(
        final GlobalBiology globalBiology,
        final Collection<AbundanceLocalBiology> sourceBiologies
    ) {
        return sourceBiologies.isEmpty()
            ? new AbundanceLocalBiology(globalBiology)
            : new AbundanceLocalBiology(
                globalBiology.getSpecies().stream().collect(toImmutableMap(
                    identity(),
                    species -> {
                        // We're grabbing the bins and subdivisions from the first
                        // biology instead of the species because sometimes the meristics
                        // are not initialised in testing code.
                        final StructuredAbundance abundance =
                            get(sourceBiologies, 0).getAbundance(species);
                        final int subdivisions = abundance.getSubdivisions();
                        final int bins = abundance.getBins();
                        Iterable<StructuredAbundance> iterator = sourceBiologies.stream().map(b -> b.getAbundance(species))::iterator;
                        return StructuredAbundance.sum(
                                iterator,
                            bins,
                            subdivisions
                        ).asMatrix();
                    }
                ))
            );
    }
}

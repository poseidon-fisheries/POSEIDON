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
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.IntStream.range;

import java.util.Collection;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

/**
 * An {@link Aggregator} that works with {@link AbundanceLocalBiology} biologies.
 */
class AbundanceAggregator extends Aggregator<AbundanceLocalBiology> {

    AbundanceAggregator() {
        super(AbundanceLocalBiology.class);
    }

    @Override
    AbundanceLocalBiology aggregate(
        final GlobalBiology globalBiology,
        final Collection<AbundanceLocalBiology> localBiologies
    ) {
        // Create a map from species to empty abundance arrays which we
        // are going to mutate directly when summing up global abundance
        final Map<Species, double[][]> abundances = globalBiology.getSpecies()
            .stream()
            .collect(toImmutableMap(
                identity(),
                species -> new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()]
            ));
        localBiologies.forEach(localBiology ->
            abundances.forEach((species, abundance) ->
                range(0, abundance.length).forEach(subdivision ->
                    range(0, abundance[subdivision].length).forEach(bin ->
                        abundance[subdivision][bin] +=
                            localBiology.getAbundance(species).getAbundance(subdivision, bin)
                    )
                )
            )
        );
        return new AbundanceLocalBiology(abundances);
    }
}

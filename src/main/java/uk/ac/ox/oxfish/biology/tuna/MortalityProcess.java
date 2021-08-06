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

import static java.util.stream.IntStream.range;

import com.google.common.primitives.ImmutableDoubleArray;
import java.util.List;
import java.util.Optional;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A proportional mortality process. It needs all species meristics to be {@link TunaMeristics}.
 * Note that "proportional mortality" here means mortality as a direct percentage as opposed to
 * {@link uk.ac.ox.oxfish.biology.complicated.ProportionalMortalityProcess} that does
 * exponentiation.
 */
public class MortalityProcess implements BiologicalProcess<AbundanceLocalBiology> {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Optional<AbundanceLocalBiology> process(
        final FishState fishState,
        final AbundanceLocalBiology aggregatedBiology
    ) {
        // Make a copy of the abundance that we are going to mutate directly
        final AbundanceLocalBiology biology = new AbundanceLocalBiology(aggregatedBiology);
        fishState.getSpecies().forEach(species -> {
            final double[][] matrix = biology.getAbundance(species).asMatrix();
            final TunaMeristics meristics = (TunaMeristics) species.getMeristics();
            final List<ImmutableDoubleArray> mortalities = meristics.getProportionalMortalities();
            range(0, meristics.getNumberOfSubdivisions()).forEach(subdivision ->
                range(0, matrix[subdivision].length).forEach(bin ->
                    matrix[subdivision][bin] *= (1 - mortalities.get(subdivision).get(bin))
                )
            );
        });
        return Optional.of(biology);
    }
}

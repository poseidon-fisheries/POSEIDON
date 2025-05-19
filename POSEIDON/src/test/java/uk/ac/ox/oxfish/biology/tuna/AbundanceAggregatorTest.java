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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class AbundanceAggregatorTest {

    @Test
    public void test() {
        final AbundanceAggregator abundanceAggregator = new AbundanceAggregator();

        final GlobalBiology globalBiology = GlobalBiology.genericListOfSpecies(2);
        final List<AbundanceLocalBiology> inputBiologies =
            range(0, 3)
                .mapToObj(__ -> new AbundanceLocalBiology(globalBiology))
                .collect(toImmutableList());

        final List<Species> species = globalBiology.getSpecies();

        /*
            Init our abundance arrays like this:

                     species
            biology    0   1
            -------  --- ---
                  0    1   2
                  1   10  20
                  2  100 200
        */
        range(0, inputBiologies.size()).forEach(b ->
            range(0, species.size()).forEach(s ->
                inputBiologies.get(b).getAbundance(species.get(s)).asMatrix()[0][0] =
                    Math.pow(10, b) * (s + 1)
            )
        );

        final AbundanceLocalBiology outputBiology =
            abundanceAggregator.apply(globalBiology, inputBiologies);

        // Note that the biomasses and the abundance numbers should be equal since we're using
        // fake meristics where the weight of one fish is 1.0.
        Assertions.assertEquals(111, outputBiology.getAbundance(species.get(0)).getAbundance(0, 0), EPSILON);
        Assertions.assertEquals(111, outputBiology.getBiomass(species.get(0)), EPSILON);
        Assertions.assertEquals(222, outputBiology.getAbundance(species.get(1)).getAbundance(0, 0), EPSILON);
        Assertions.assertEquals(222, outputBiology.getBiomass(species.get(1)), EPSILON);

        // Check that the original abundances haven't been changed
        range(0, inputBiologies.size()).forEach(b ->
            range(0, species.size()).forEach(s -> {
                final double expected = Math.pow(10, b) * (s + 1);
                Assertions.assertEquals(
                    expected,
                    inputBiologies.get(b).getAbundance(species.get(s)).getAbundance(0, 0),
                    EPSILON
                );
                Assertions.assertEquals(expected, inputBiologies.get(b).getBiomass(species.get(s)), EPSILON);
            })
        );
    }

}

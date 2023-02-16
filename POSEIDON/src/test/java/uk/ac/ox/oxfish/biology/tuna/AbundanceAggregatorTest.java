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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.GlobalBiomassLogger;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

public class AbundanceAggregatorTest extends TestCase {

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
        assertEquals(111, outputBiology.getAbundance(species.get(0)).getAbundance(0, 0), EPSILON);
        assertEquals(111, outputBiology.getBiomass(species.get(0)), EPSILON);
        assertEquals(222, outputBiology.getAbundance(species.get(1)).getAbundance(0, 0), EPSILON);
        assertEquals(222, outputBiology.getBiomass(species.get(1)), EPSILON);

        // Check that the original abundances haven't been changed
        range(0, inputBiologies.size()).forEach(b ->
            range(0, species.size()).forEach(s -> {
                final double expected = Math.pow(10, b) * (s + 1);
                assertEquals(
                    expected,
                    inputBiologies.get(b).getAbundance(species.get(s)).getAbundance(0, 0),
                    EPSILON
                );
                assertEquals(
                    expected,
                    inputBiologies.get(b).getBiomass(species.get(s)),
                    EPSILON
                );
            })
        );
    }


    public void testWithEpoAbundanceScenario() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();
        scenario.useDummyData();
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        fishState.start();

        final List<Species> species = fishState.getSpecies();

        final Map<Species, Double> biomassesBeforeExtraction = getBiomasses(fishState, species);

        final Collection<AbundanceLocalBiology> extractedBiologies =
            new AbundanceExtractorProcess(true, true)
                .process(fishState, null);

        compareBiomasses(
            biomassesBeforeExtraction,
            getBiomasses(fishState, species)
        );
        compareBiomasses(
            biomassesBeforeExtraction,
            getBiomasses(fishState, species, extractedBiologies)
        );

        final Collection<AbundanceLocalBiology> aggregatedBiologies =
            new AbundanceAggregatorProcess().process(fishState, extractedBiologies);

        compareBiomasses(
            biomassesBeforeExtraction,
            getBiomasses(fishState, species)
        );
        compareBiomasses(
            biomassesBeforeExtraction,
            getBiomasses(fishState, species, aggregatedBiologies)
        );

    }

    private static ImmutableMap<Species, Double> getBiomasses(
        final FishState fishState,
        final Collection<Species> species,
        final Collection<AbundanceLocalBiology> extractedBiologies
    ) {
        //noinspection UnstableApiUsage
        return Streams.zip(
            species.stream(),
            GlobalBiomassLogger.getBiomassesStream(fishState, extractedBiologies),
            FishStateUtilities::entry
        ).collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

    private static void compareBiomasses(
        final Map<Species, Double> expected,
        final Map<Species, Double> actual
    ) {
        expected.forEach((species, biomass) ->
            assertEquals(biomass, actual.get(species), EPSILON)
        );
    }

    private static Map<Species, Double> getBiomasses(
        final FishState fishState,
        final Collection<Species> species
    ) {
        return species.stream().collect(toImmutableMap(
            Function.identity(),
            fishState::getTotalBiomass
        ));
    }

}
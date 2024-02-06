/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.scenarios;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceAggregatorProcess;
import uk.ac.ox.oxfish.biology.tuna.AbundanceExtractorProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.GlobalBiomassLogger;
import uk.ac.ox.oxfish.model.scenario.TestableScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class AbundanceAggregatorTest {
    private static Map<Species, Double> getBiomasses(
        final FishState fishState,
        final Collection<Species> species
    ) {
        return species.stream().collect(toImmutableMap(
            Function.identity(),
            fishState::getTotalBiomass
        ));
    }

    private static void compareBiomasses(
        final Map<Species, Double> expected,
        final Map<Species, Double> actual
    ) {
        expected.forEach((species, biomass) ->
            Assertions.assertEquals(biomass, actual.get(species), EPSILON)
        );
    }

    private static ImmutableMap<Species, Double> getBiomasses(
        final FishState fishState,
        final Collection<Species> species,
        final Collection<AbundanceLocalBiology> extractedBiologies
    ) {
        // noinspection UnstableApiUsage
        return Streams.zip(
            species.stream(),
            GlobalBiomassLogger.getBiomassesStream(fishState, extractedBiologies),
            FishStateUtilities::entry
        ).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Test
    public void testWithEpoAbundanceScenario() {
        final TestableScenario scenario = new EpoGravityAbundanceScenario();
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

}

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

package uk.ac.ox.oxfish.model.scenario;

import static uk.ac.ox.oxfish.model.FishState.DEFAULT_POPULATION_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.TunaAbundanceInitializerFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoScenario implements Scenario {

    private static final Path INPUT_PATH = Paths.get("inputs", "epo");

    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializerFactory =
        new TunaAbundanceInitializerFactory(
            INPUT_PATH.resolve("species_codes.csv"),
            INPUT_PATH.resolve("bins.csv")
        );

    private AlgorithmFactory<? extends MapInitializer> mapInitializerFactory =
        new FromFileMapInitializerFactory(
            INPUT_PATH.resolve("depth.csv"),
            101,
            0.5
        );

    /**
     * Just runs the scenario for a year.
     */
    public static void main(final String[] args) {
        final FishState fishState = new FishState();
        fishState.setScenario(new EpoScenario());
        fishState.start();
        while (fishState.getStep() < 365) {
            fishState.schedule.step(fishState);
        }
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializerFactory() {
        return biologyInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setBiologyInitializerFactory(
        final AlgorithmFactory<? extends BiologyInitializer> biologyInitializerFactory) {
        this.biologyInitializerFactory = biologyInitializerFactory;
    }

    @Override
    public ScenarioEssentials start(final FishState fishState) {

        final MersenneTwisterFast rng = fishState.getRandom();

        final BiologyInitializer biologyInitializer =
            biologyInitializerFactory.apply(fishState);

        final GlobalBiology globalBiology =
            biologyInitializer.generateGlobal(rng, fishState);

        final NauticalMap nauticalMap =
            mapInitializerFactory
                .apply(fishState)
                .makeMap(fishState.random, globalBiology, fishState);

        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));
        nauticalMap.initializeBiology(biologyInitializer, rng, globalBiology);
        biologyInitializer.processMap(globalBiology, nauticalMap, rng, fishState);

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {

        final List<Fisher> population = ImmutableList.of();
        final SocialNetwork network = new SocialNetwork(new EmptyNetworkBuilder());
        final Map<String, FisherFactory> fisherFactories =
            ImmutableMap.of(DEFAULT_POPULATION_NAME, new FisherFactory(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0
            ));

        return new ScenarioPopulation(population, network, fisherFactories);
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends MapInitializer> getMapInitializerFactory() {
        return mapInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setMapInitializerFactory(
        final AlgorithmFactory<? extends MapInitializer> mapInitializerFactory) {
        this.mapInitializerFactory = mapInitializerFactory;
    }
}

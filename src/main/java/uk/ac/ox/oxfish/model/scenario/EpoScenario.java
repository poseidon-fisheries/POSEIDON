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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.AbundanceReallocatorFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoScenario implements Scenario {

    private static final Path INPUT_PATH = Paths.get("inputs", "epo");
    private Set<AdditionalStartable> additionalStartables = new HashSet<>();
    private AbundanceInitializerFactory abundanceInitializerFactory =
        new AbundanceInitializerFactory(
            INPUT_PATH.resolve("species_codes.csv"),
            INPUT_PATH.resolve("bins.csv"),
            new AbundanceReallocatorFactory(
                INPUT_PATH.resolve("species_codes.csv"),
                INPUT_PATH.resolve("grids.csv"),
                365,
                ImmutableMap.of(
                    "Skipjack tuna", 14,
                    "Bigeye tuna", 8,
                    "Yellowfin tuna", 9
                )
            )
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
    public Set<AdditionalStartable> getAdditionalStartables() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return additionalStartables;
    }

    @SuppressWarnings("unused")
    public void setAdditionalStartables(final Set<AdditionalStartable> additionalStartables) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.additionalStartables = additionalStartables;
    }

    @SuppressWarnings("unused")
    public AbundanceInitializerFactory getAbundanceReallocatorInitializerFactory() {
        return abundanceInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceReallocatorInitializerFactory(
        final AbundanceInitializerFactory abundanceInitializerFactory
    ) {
        this.abundanceInitializerFactory = abundanceInitializerFactory;
    }

    @Override
    public ScenarioEssentials start(final FishState fishState) {

        final MersenneTwisterFast rng = fishState.getRandom();

        final NauticalMap nauticalMap =
            mapInitializerFactory
                .apply(fishState)
                .makeMap(fishState.random, null, fishState);

        abundanceInitializerFactory
            .getAbundanceReallocatorFactory()
            .setMapExtent(new MapExtent(nauticalMap));

        final AbundanceInitializer abundanceInitializer =
            abundanceInitializerFactory.apply(fishState);

        additionalStartables.add(abundanceInitializer.getAbundanceReallocator());

        final GlobalBiology globalBiology =
            abundanceInitializer.generateGlobal(rng, fishState);

        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));
        nauticalMap.initializeBiology(abundanceInitializer, rng, globalBiology);
        abundanceInitializer.processMap(globalBiology, nauticalMap, rng, fishState);

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

        additionalStartables.forEach(fishState::registerStartable);

        return new ScenarioPopulation(population, network, fisherFactories);
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends MapInitializer> getMapInitializerFactory() {
        return mapInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setMapInitializerFactory(
        final AlgorithmFactory<? extends MapInitializer> mapInitializerFactory
    ) {
        this.mapInitializerFactory = mapInitializerFactory;
    }
}

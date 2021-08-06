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

import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;
import static uk.ac.ox.oxfish.model.FishState.DEFAULT_POPULATION_NAME;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceReallocator;
import uk.ac.ox.oxfish.biology.tuna.AbundanceReallocatorFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceRestorerFactory;
import uk.ac.ox.oxfish.biology.tuna.RecruitmentProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.ScheduledAbundanceProcessesFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoScenario implements Scenario {

    private static final Path INPUT_PATH = Paths.get("inputs", "epo");
    private final SpeciesCodesFromFileFactory speciesCodesFactory =
        new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
    private RecruitmentProcessesFactory recruitmentProcessesFactory =
        new RecruitmentProcessesFactory(INPUT_PATH.resolve("recruitment_parameters.csv"));
    private ScheduledAbundanceProcessesFactory scheduledAbundanceProcessesFactory =
        new ScheduledAbundanceProcessesFactory(
            ImmutableList.of("2017-01-01", "2017-04-01", "2017-07-01", "2017-10-01")
        );
    private AbundanceReallocatorFactory abundanceReallocatorFactory =
        new AbundanceReallocatorFactory(
            INPUT_PATH.resolve("grids.csv"),
            ImmutableMap.of(
                "Skipjack tuna", 14,
                "Bigeye tuna", 8,
                "Yellowfin tuna", 9
            ),
            365
        );
    private AbundanceInitializerFactory abundanceInitializerFactory =
        new AbundanceInitializerFactory(INPUT_PATH.resolve("bins.csv"));
    private AbundanceRestorerFactory abundanceRestorerFactory =
        new AbundanceRestorerFactory(ImmutableMap.of(0, 364));
    private AlgorithmFactory<? extends MapInitializer> mapInitializerFactory =
        new FromFileMapInitializerFactory(
            INPUT_PATH.resolve("depth.csv"),
            101,
            0.5
        );
    private FadMapFactory fadMapFactory = new FadMapFactory(
        ImmutableMap.of(Y2017, INPUT_PATH.resolve("currents_2017.csv"))
    );

    /**
     * Just runs the scenario for a year.
     */
    public static void main(final String[] args) {
        final FishState fishState = new FishState();
        final Scenario scenario = new EpoScenario();
        try {
            new FishYAML().dump(scenario, new FileWriter(INPUT_PATH.resolve("epo.yaml").toFile()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        fishState.setScenario(scenario);
        fishState.start();
        while (fishState.getStep() < 365) {
            fishState.schedule.step(fishState);
        }
    }


    @SuppressWarnings("unused")
    public AbundanceInitializerFactory getAbundanceInitializerFactory() {
        return abundanceInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceInitializerFactory(
        final AbundanceInitializerFactory abundanceInitializerFactory
    ) {
        this.abundanceInitializerFactory = abundanceInitializerFactory;
    }

    @SuppressWarnings("unused")
    public AbundanceRestorerFactory getAbundanceRestorerFactory() {
        return abundanceRestorerFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceRestorerFactory(
        final AbundanceRestorerFactory abundanceRestorerFactory
    ) {
        this.abundanceRestorerFactory = abundanceRestorerFactory;
    }

    @SuppressWarnings("unused")
    public FadMapFactory getFadMapFactory() {
        return fadMapFactory;
    }

    @SuppressWarnings("unused")
    public void setFadMapFactory(final FadMapFactory fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
    }

    @SuppressWarnings("unused")
    public AbundanceReallocatorFactory getAbundanceReallocatorFactory() {
        return abundanceReallocatorFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceReallocatorFactory(
        final AbundanceReallocatorFactory abundanceReallocatorFactory
    ) {
        this.abundanceReallocatorFactory = abundanceReallocatorFactory;
    }

    @SuppressWarnings("unused")
    public RecruitmentProcessesFactory getRecruitmentProcessesFactory() {
        return recruitmentProcessesFactory;
    }

    @SuppressWarnings("unused")
    public void setRecruitmentProcessesFactory(
        final RecruitmentProcessesFactory recruitmentProcessesFactory
    ) {
        this.recruitmentProcessesFactory = recruitmentProcessesFactory;
    }

    @SuppressWarnings("unused")
    public ScheduledAbundanceProcessesFactory getScheduledAbundanceProcessesFactory() {
        return scheduledAbundanceProcessesFactory;
    }

    @SuppressWarnings("unused")
    public void setScheduledAbundanceProcessesFactory(
        final ScheduledAbundanceProcessesFactory scheduledAbundanceProcessesFactory
    ) {
        this.scheduledAbundanceProcessesFactory = scheduledAbundanceProcessesFactory;
    }

    @Override
    public ScenarioEssentials start(final FishState fishState) {

        final MersenneTwisterFast rng = fishState.getRandom();
        final SpeciesCodes speciesCodes = speciesCodesFactory.get();

        final NauticalMap nauticalMap =
            mapInitializerFactory
                .apply(fishState)
                .makeMap(fishState.random, null, fishState);

        abundanceReallocatorFactory.setMapExtent(new MapExtent(nauticalMap));
        abundanceReallocatorFactory.setSpeciesCodes(speciesCodes);
        final AbundanceReallocator reallocator =
            abundanceReallocatorFactory.apply(fishState);

        abundanceRestorerFactory.setAbundanceReallocator(reallocator);

        abundanceInitializerFactory.setAbundanceReallocator(reallocator);
        abundanceInitializerFactory.setSpeciesCodes(speciesCodes);
        final AbundanceInitializer abundanceInitializer =
            abundanceInitializerFactory.apply(fishState);

        final GlobalBiology globalBiology =
            abundanceInitializer.generateGlobal(rng, fishState);

        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));
        nauticalMap.initializeBiology(abundanceInitializer, rng, globalBiology);
        abundanceInitializer.processMap(globalBiology, nauticalMap, rng, fishState);

        recruitmentProcessesFactory.setSpeciesCodes(speciesCodes);
        recruitmentProcessesFactory.setGlobalBiology(globalBiology);
        final Map<Species, ? extends RecruitmentProcess> recruitmentProcesses =
            recruitmentProcessesFactory.apply(fishState);

        scheduledAbundanceProcessesFactory.setRecruitmentProcesses(recruitmentProcesses);
        scheduledAbundanceProcessesFactory.setAbundanceReallocator(reallocator);
        fishState.registerStartable(scheduledAbundanceProcessesFactory.apply(fishState));

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

        ImmutableList.of(
            abundanceRestorerFactory,
            fadMapFactory
        ).forEach(startableFactory ->
            fishState.registerStartable(startableFactory.apply(fishState))
        );

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

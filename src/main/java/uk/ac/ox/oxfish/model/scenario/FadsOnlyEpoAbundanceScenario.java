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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceReallocator;
import uk.ac.ox.oxfish.biology.tuna.AbundanceReallocatorFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceRestorerFactory;
import uk.ac.ox.oxfish.biology.tuna.RecruitmentProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.ScheduledAbundanceProcessesFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
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
public class FadsOnlyEpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {


    private final SpeciesCodesFromFileFactory speciesCodesFactory =
        new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
    private AlgorithmFactory<? extends AdditionalStartable> fadMakerFactory =
        new ExogenousFadMakerCSVFactory(
            INPUT_PATH.resolve("calibration").resolve("fad_deployments.csv").toString(), null
        );

    private AlgorithmFactory<? extends AdditionalStartable> fadSetterFactory =
        new ExogenousFadSetterCSVFactory(
            INPUT_PATH.resolve("calibration").resolve("fad_sets.csv").toString(), true
        );

    private RecruitmentProcessesFactory recruitmentProcessesFactory =
        new RecruitmentProcessesFactory(
            INPUT_PATH.resolve("abundance").resolve("recruitment_parameters.csv")
        );
    private ScheduledAbundanceProcessesFactory scheduledAbundanceProcessesFactory =
        new ScheduledAbundanceProcessesFactory(
            ImmutableList.of("2017-01-01", "2017-04-01", "2017-07-01", "2017-10-01")
        );
    private AlgorithmFactory<? extends AbundanceReallocator> abundanceReallocatorFactory =
        new AbundanceReallocatorFactory(
            INPUT_PATH.resolve("abundance").resolve("grids.csv"),
            ImmutableMap.of(
                "Skipjack tuna", 14,
                "Bigeye tuna", 8,
                "Yellowfin tuna", 9
            ),
            365
        );
    private AlgorithmFactory<? extends AbundanceInitializer> abundanceInitializerFactory =
        new AbundanceInitializerFactory(INPUT_PATH.resolve("abundance").resolve("bins.csv"));
    private AbundanceRestorerFactory abundanceRestorerFactory =
        new AbundanceRestorerFactory(ImmutableMap.of(0, 364));
    private AlgorithmFactory<? extends MapInitializer> mapInitializerFactory =
        new FromFileMapInitializerFactory(
            INPUT_PATH.resolve("depth.csv"),
            101,
            0.5
        );
    private AbundanceFadMapFactory fadMapFactory = new AbundanceFadMapFactory(
        ImmutableMap.of(Y2017, INPUT_PATH.resolve("currents").resolve("currents_2017.csv"))
    );
    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFactory(INPUT_PATH.resolve("abundance").resolve("selectivity.csv"));
    private AbundanceFadInitializerFactory fadInitializerFactory =
        new AbundanceFadInitializerFactory(
            "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
        );

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends AdditionalStartable> getFadMakerFactory() {
        return fadMakerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadMakerFactory(final AlgorithmFactory<? extends AdditionalStartable> fadMakerFactory) {
        this.fadMakerFactory = fadMakerFactory;
    }

    @SuppressWarnings("unused")
    public AbundanceFadInitializerFactory getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(final AbundanceFadInitializerFactory fadInitializerFactory) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends AbundanceInitializer> getAbundanceInitializerFactory() {
        return abundanceInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceInitializerFactory(
        final AlgorithmFactory<? extends AbundanceInitializer> abundanceInitializerFactory
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
    public AbundanceFadMapFactory getFadMapFactory() {
        return fadMapFactory;
    }

    @SuppressWarnings("unused")
    public void setFadMapFactory(final AbundanceFadMapFactory fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends AbundanceReallocator> getAbundanceReallocatorFactory() {
        return abundanceReallocatorFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceReallocatorFactory(
        final AlgorithmFactory<? extends AbundanceReallocator> abundanceReallocatorFactory
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

        final AbundanceReallocatorFactory abundanceReallocatorFactory =
            (AbundanceReallocatorFactory) this.abundanceReallocatorFactory;
        abundanceReallocatorFactory.setMapExtent(new MapExtent(nauticalMap));
        abundanceReallocatorFactory.setSpeciesCodes(speciesCodes);
        final AbundanceReallocator reallocator =
            this.abundanceReallocatorFactory.apply(fishState);

        abundanceRestorerFactory.setAbundanceReallocator(reallocator);

        final AbundanceInitializerFactory abundanceInitializerFactory =
            (AbundanceInitializerFactory) this.abundanceInitializerFactory;
        abundanceInitializerFactory.setAbundanceReallocator(reallocator);
        abundanceInitializerFactory.setSpeciesCodes(speciesCodes);
        final AbundanceInitializer abundanceInitializer =
            this.abundanceInitializerFactory.apply(fishState);

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

        initModel(fishState);

        abundanceFiltersFactory.setSpeciesCodes(speciesCodesFactory.get());
        final Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>>
            abundanceFilters =
            abundanceFiltersFactory.apply(fishState);
        final Monitors monitors = new Monitors(fishState);
        monitors.getMonitors().forEach(fishState::registerStartable);

        fadInitializerFactory.setSpeciesCodes(speciesCodesFactory.get());
        fadInitializerFactory.setSelectivityFilters(abundanceFilters.get(FadSetAction.class));
        ((ExogenousFadMakerCSVFactory) fadMakerFactory).setFadInitializer(fadInitializerFactory);

        ImmutableList.of(
            abundanceRestorerFactory,
            fadMakerFactory,
            fadSetterFactory
        ).forEach(startableFactory ->
            fishState.registerStartable(startableFactory.apply(fishState))
        );

        return new ScenarioPopulation(
            ImmutableList.of(),
            new SocialNetwork(new EmptyNetworkBuilder()),
            ImmutableMap.of() // no entry in the fishery so no need to pass factory here
        );
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

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends AdditionalStartable> getFadSetterFactory() {
        return fadSetterFactory;
    }

    @SuppressWarnings("unused")
    public void setFadSetterFactory(
        final AlgorithmFactory<? extends AdditionalStartable> fadSetterFactory
    ) {
        this.fadSetterFactory = fadSetterFactory;
    }
}

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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2016;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.nio.file.Path;
import java.util.Map;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.*;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishUntilFullFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadMakerCSVFactory;
import uk.ac.ox.oxfish.geography.fads.ExogenousFadSetterCSVFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.PluggableSelectivity;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class FadsOnlyEpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private boolean fadSettingActive = true;

    private final SpeciesCodesFromFileFactory speciesCodesFactory =
        new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
    private AlgorithmFactory<? extends AdditionalStartable> fadMakerFactory =
        new ExogenousFadMakerCSVFactory(
            INPUT_PATH.resolve("calibration").resolve("fad_deployments.csv").toString(),
            new AbundanceFadInitializerFactory()
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
            365
        );
    private AlgorithmFactory<? extends AbundanceInitializer> abundanceInitializerFactory =
        new AbundanceInitializerFactory(INPUT_PATH.resolve("abundance").resolve("bins.csv"));
    private AbundanceRestorerFactory abundanceRestorerFactory =
        new AbundanceRestorerFactory(ImmutableMap.of(0, 365));
    private AlgorithmFactory<? extends MapInitializer> mapInitializerFactory =
        new FromFileMapInitializerFactory(
            INPUT_PATH.resolve("depth.csv"),
            101,
            0.5
        );
    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFactory(INPUT_PATH.resolve("abundance").resolve("selectivity.csv"));
    private AlgorithmFactory<? extends FadInitializer>
        fadInitializerFactory =
        new AbundanceFadInitializerFactory(
            "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
        );
    private WeightGroupsFactory weightGroupsFactory = new WeightGroupsFactory(
        speciesCodesFactory.get().getSpeciesNames().stream().collect(
            toImmutableMap(identity(), __ -> ImmutableList.of("small", "medium", "large"))
        ),
        ImmutableMap.of(
            "Bigeye tuna", ImmutableList.of(2.5, 15.0),
            // use the last two bins of SKJ as "large"
            "Skipjack tuna", ImmutableList.of(2.5, 11.501600),
            "Yellowfin tuna", ImmutableList.of(2.5, 15.0)
        )

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

    public FadsOnlyEpoAbundanceScenario() {
        this.setFadMapFactory(new AbundanceFadMapFactory(currentFiles));
        this.setFishingStrategyFactory(new FishUntilFullFactory());
    }

    @Override
    public ScenarioEssentials start(final FishState fishState) {

        logCurrentTime(fishState);
        fishState.scheduleEveryDay(TunaCalibrator::logCurrentTime, StepOrder.DAWN);

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
        abundanceInitializerFactory.setWeightGroupsPerSpecies(weightGroupsFactory.get());
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
        if (scheduledAbundanceProcessesFactory.getAbundanceMortalityProcessFactory()
            instanceof AbundanceMortalityProcessFromFileFactory) {
            ((AbundanceMortalityProcessFromFileFactory)
                scheduledAbundanceProcessesFactory.getAbundanceMortalityProcessFactory())
                .setSpeciesCodes(speciesCodes);
        }

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {

        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        abundanceFiltersFactory.setSpeciesCodes(speciesCodesFactory.get());
        final Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>>
            abundanceFilters =
            abundanceFiltersFactory.apply(fishState);

        if (fadInitializerFactory instanceof AbundanceFadInitializerFactory) {
            ((FadInitializerFactory<AbundanceLocalBiology, AbundanceFad>) fadInitializerFactory)
                .setSpeciesCodes(speciesCodesFactory.get());
        }
        ((PluggableSelectivity) fadInitializerFactory).setSelectivityFilters(abundanceFilters.get(
            FadSetAction.class));
        ((ExogenousFadMakerCSVFactory) fadMakerFactory).setFadInitializer(fadInitializerFactory);

        fishState.registerStartable(scheduledAbundanceProcessesFactory.apply(fishState));
        fishState.registerStartable(abundanceRestorerFactory.apply(fishState));
        fishState.registerStartable(fadMakerFactory.apply(fishState));
        if(fadSettingActive)
            fishState.registerStartable(fadSetterFactory.apply(fishState));

        return scenarioPopulation;
    }

    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @Override
    public void useDummyData(final Path testPath) {
        super.useDummyData(testPath);
        ((ExogenousFadMakerCSVFactory) fadMakerFactory).setPathToFile(
            testPath.resolve("dummy_fad_deployments.csv").toString()
        );
        ((ExogenousFadSetterCSVFactory) fadSetterFactory).setPathToFile(
            testPath.resolve("dummy_fad_sets.csv").toString()
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

    @SuppressWarnings("unused")
    public WeightGroupsFactory getWeightGroupsFactory() {
        return weightGroupsFactory;
    }

    @SuppressWarnings("unused")
    public void setWeightGroupsFactory(WeightGroupsFactory weightGroupsFactory) {
        this.weightGroupsFactory = weightGroupsFactory;
    }

    public boolean isFadSettingActive() {
        return fadSettingActive;
    }

    public void setFadSettingActive(boolean fadSettingActive) {
        this.fadSettingActive = fadSettingActive;
    }

}

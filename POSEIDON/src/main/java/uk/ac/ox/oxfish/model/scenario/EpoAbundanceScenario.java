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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.*;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.*;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private RecruitmentProcessesFactory recruitmentProcessesFactory =
        new RecruitmentProcessesFactory(
            getSpeciesCodesSupplier(),
            new InputFile(getInputFolder(), Paths.get("abundance", "recruitment_parameters.csv"))
        );

    private ScheduledAbundanceProcessesFactory scheduledAbundanceProcessesFactory =
        new ScheduledAbundanceProcessesFactory(
            getSpeciesCodesSupplier(),
            ImmutableList.of("2017-01-01", "2017-04-01", "2017-07-01", "2017-10-01"),
            new InputFile(getInputFolder(), Paths.get("abundance", "mortality.csv"))
        );
    private AlgorithmFactory<? extends AbundanceReallocator> abundanceReallocatorFactory =
        new AbundanceReallocatorFactory(
            new InputFile(getInputFolder(), Paths.get("abundance", "grids.csv")),
            365
        );

    private WeightGroupsFactory weightGroupsFactory = new WeightGroupsFactory(
        getSpeciesCodesSupplier().get().getSpeciesNames().stream().collect(
            toImmutableMap(identity(), __ -> ImmutableList.of("small", "medium", "large"))
        ),
        ImmutableMap.of(
            "Bigeye tuna", ImmutableList.of(2.5, 15.0),
            // use the last two bins of SKJ as "medium" and "large"
            "Skipjack tuna", ImmutableList.of(2.5, 11.5019),
            "Yellowfin tuna", ImmutableList.of(2.5, 15.0)
        )

    );
    private AlgorithmFactory<? extends AbundanceInitializer> abundanceInitializerFactory =
        new AbundanceInitializerFactory(
            new InputFile(getInputFolder(), Paths.get("abundance", "bins.csv"))
        );
    private AbundanceRestorerFactory abundanceRestorerFactory =
        new AbundanceRestorerFactory(ImmutableMap.of(0, 365));

    private AlgorithmFactory<? extends FadInitializer>
        fadInitializerFactory =
        new LinearAbundanceFadInitializerFactory(
            "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
        );
    private GravityDestinationStrategyFactory gravityDestinationStrategyFactory =
        new GravityDestinationStrategyFactory(
            new InputFile(getInputFolder(), "action_weights.csv"),
            getVesselsFile()
        );

    public EpoAbundanceScenario() {
        setFadMapFactory(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
        final InputFile maxCurrentSpeedsFile = new InputFile(getInputFolder(), "max_current_speeds.csv");
        setFishingStrategyFactory(
            new PurseSeinerAbundanceFishingStrategyFactory(
                getSpeciesCodesSupplier(),
                new InputFile(getInputFolder(), "action_weights.csv"),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    new AbundanceFiltersFactory(
                        new InputFile(getInputFolder(), Paths.get("abundance", "selectivity.csv")),
                        getSpeciesCodesSupplier()
                    ),
                    new InputFile(getInputFolder(), "set_samples.csv")
                ),
                new SetDurationSamplersFactory(
                    new InputFile(getInputFolder(), "set_durations.csv")
                ),
                maxCurrentSpeedsFile,
                new InputFile(getInputFolder(), "set_compositions.csv")
            )
        );
        setPurseSeineGearFactory(new AbundancePurseSeineGearFactory(
            new InputFile(getInputFolder(), "location_values.csv"),
            maxCurrentSpeedsFile
        ));
    }

    /**
     * Just runs the scenario for a year.
     */
    public static void main(final String[] args) {
        final FishState fishState = new FishState();
        final Scenario scenario = new EpoAbundanceScenario();

        try {
            final File scenarioFile =
                Paths.get(
                    System.getProperty("user.home"),
                    "workspace", "tuna", "calibration", "results",
                    "cenv0729", "2022-06-01_18.14.02_global_calibration",
                    "calibrated_scenario.yaml"
                ).toFile();

            //addCsvLogger(Level.DEBUG, "potential_actions", "action,initial,modulated,weighted");
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
            fishState.setScenario(scenario);
            fishState.start();
            while (fishState.getStep() < 365) {
                System.out.println("Step: " + fishState.getStep());
                fishState.schedule.step(fishState);
            }
            System.out.println("Done.");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public GravityDestinationStrategyFactory getGravityDestinationStrategyFactory() {
        return gravityDestinationStrategyFactory;
    }

    public void setGravityDestinationStrategyFactory(final GravityDestinationStrategyFactory gravityDestinationStrategyFactory) {
        this.gravityDestinationStrategyFactory = gravityDestinationStrategyFactory;
    }

    @SuppressWarnings("unused")
    public WeightGroupsFactory getWeightGroupsFactory() {
        return weightGroupsFactory;
    }

    @SuppressWarnings("unused")
    public void setWeightGroupsFactory(WeightGroupsFactory weightGroupsFactory) {
        this.weightGroupsFactory = weightGroupsFactory;
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

    @Override
    public ScenarioEssentials start(final FishState fishState) {
        logCurrentTime(fishState);
        fishState.scheduleEveryDay(TunaCalibrator::logCurrentTime, StepOrder.DAWN);

        final MersenneTwisterFast rng = fishState.getRandom();
        final SpeciesCodes speciesCodes = getSpeciesCodesSupplier().get();

        final NauticalMap nauticalMap =
            getMapInitializerFactory()
                .apply(fishState)
                .makeMap(fishState.random, null, fishState);

        final AbundanceReallocatorFactory abundanceReallocatorFactory =
            (AbundanceReallocatorFactory) this.abundanceReallocatorFactory;
        abundanceReallocatorFactory.setMapExtent(nauticalMap.getMapExtent());
        abundanceReallocatorFactory.setSpeciesCodes(speciesCodes);
        final AbundanceReallocator reallocator =
            this.abundanceReallocatorFactory.apply(fishState);

        abundanceRestorerFactory.setAbundanceReallocator(reallocator);

        final AbundanceInitializerFactory abundanceInitializerFactory =
            (AbundanceInitializerFactory) this.abundanceInitializerFactory;
        abundanceInitializerFactory.setAbundanceReallocator(reallocator);
        abundanceInitializerFactory.setSpeciesCodes(speciesCodes);
        abundanceInitializerFactory.assignWeightGroupsPerSpecies(weightGroupsFactory.apply(fishState));
        final AbundanceInitializer abundanceInitializer =
            this.abundanceInitializerFactory.apply(fishState);

        final GlobalBiology globalBiology =
            abundanceInitializer.generateGlobal(rng, fishState);

        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));
        nauticalMap.initializeBiology(abundanceInitializer, rng, globalBiology);
        abundanceInitializer.processMap(globalBiology, nauticalMap, rng, fishState);

        recruitmentProcessesFactory.setGlobalBiology(globalBiology);
        final Map<Species, ? extends RecruitmentProcess> recruitmentProcesses =
            recruitmentProcessesFactory.apply(fishState);

        scheduledAbundanceProcessesFactory.setRecruitmentProcesses(recruitmentProcesses);
        scheduledAbundanceProcessesFactory.setAbundanceReallocator(reallocator);
        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {

        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        if (fadInitializerFactory instanceof AbundanceFadInitializerFactory) {
            ((FadInitializerFactory<AbundanceLocalBiology, AbundanceFad>) fadInitializerFactory)
                .setSpeciesCodes(getSpeciesCodesSupplier().get());
        }
        ((PluggableSelectivity) fadInitializerFactory).setSelectivityFilters(
            ((AbundanceCatchSamplersFactory) ((PurseSeinerAbundanceFishingStrategyFactory)
                getFishingStrategyFactory()).getCatchSamplersFactory())
                .getAbundanceFiltersFactory()
                .apply(fishState)
                .get(FadSetAction.class)
        );

        getPurseSeineGearFactory().setFadInitializerFactory(fadInitializerFactory);

        final FisherFactory fisherFactory = makeFisherFactory(
            fishState,
            getRegulationsFactory(),
            gravityDestinationStrategyFactory
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(
                getVesselsFile().get(),
                TARGET_YEAR,
                fisherFactory,
                buildPorts(fishState)
            ).apply(fishState);

        ImmutableList.of(
            scheduledAbundanceProcessesFactory,
            abundanceRestorerFactory
        ).forEach(startableFactory ->
            fishState.registerStartable(startableFactory.apply(fishState))
        );

        plugins.forEach(plugin -> fishState.registerStartable(plugin.apply(fishState)));

        scenarioPopulation.getPopulation().addAll(fishers);
        return scenarioPopulation;
    }

    @SuppressWarnings("unused")
    @Override
    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    @Override
    public void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        this.gravityDestinationStrategyFactory.setActionWeightsFile(
            new InputFile(testFolder(), "dummy_action_weights.csv")
        );
        this.gravityDestinationStrategyFactory.setMaxTripDurationFile(
            new InputFile(testFolder(), "dummy_boats.csv")
        );
    }

}

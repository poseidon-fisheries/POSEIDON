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

import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2016;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.io.File;
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
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceMortalityProcessFromFileFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceReallocator;
import uk.ac.ox.oxfish.biology.tuna.AbundanceReallocatorFactory;
import uk.ac.ox.oxfish.biology.tuna.AbundanceRestorerFactory;
import uk.ac.ox.oxfish.biology.tuna.RecruitmentProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.ScheduledAbundanceProcessesFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.PluggableSelectivity;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortInitializer;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.MarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private final SpeciesCodesFromFileFactory speciesCodesFactory =
        new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
    private final PortInitializer portInitializer =
        new FromSimpleFilePortInitializer(TARGET_YEAR, INPUT_PATH.resolve("ports.csv"));
    private final MarketMapFromPriceFileFactory marketMapFromPriceFileFactory =
        new MarketMapFromPriceFileFactory(INPUT_PATH.resolve("prices.csv"), TARGET_YEAR);
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
        new AbundanceRestorerFactory(ImmutableMap.of(0, 365));
    private AlgorithmFactory<? extends MapInitializer> mapInitializerFactory =
        new FromFileMapInitializerFactory(
            INPUT_PATH.resolve("depth.csv"),
            101,
            0.5
        );
    private AbundanceFadMapFactory fadMapFactory = new AbundanceFadMapFactory(
        ImmutableMap.of(
            Y2016, INPUT_PATH.resolve("currents").resolve("currents_2016.csv"),
            Y2017, INPUT_PATH.resolve("currents").resolve("currents_2017.csv")
        )
    );
    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFactory(INPUT_PATH.resolve("abundance").resolve("selectivity.csv"));
    private AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>
        fadInitializerFactory =
        new AbundanceFadInitializerFactory(
            "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
        );
    private AlgorithmFactory<? extends Regulation> regulationsFactory =
        new StandardIattcRegulationsFactory();

    public EpoAbundanceScenario() {
        setCatchSamplersFactory(new AbundanceCatchSamplersFactory());
        setFishingStrategyFactory(new PurseSeinerAbundanceFishingStrategyFactory());
        setPurseSeineGearFactory(new AbundancePurseSeineGearFactory());
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
                    "nicolas", "2022-05-12_16.56.21_global_calibration",
                    "calibrated_scenario.yaml"
                ).toFile();
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
            fishState.setScenario(scenario);
            fishState.start();
            while (fishState.getStep() < 365) {
                System.out.println("Step: " + fishState.getStep());
                fishState.schedule.step(fishState);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends Regulation> getRegulationsFactory() {
        return regulationsFactory;
    }

    @SuppressWarnings("unused")
    public void setRegulationsFactory(final AlgorithmFactory<? extends Regulation> regulationsFactory) {
        this.regulationsFactory = regulationsFactory;
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
            abundanceFilters = abundanceFiltersFactory.apply(fishState);
        if (getCatchSamplersFactory() instanceof AbundanceCatchSamplersFactory) {
            ((AbundanceCatchSamplersFactory) getCatchSamplersFactory())
                .setAbundanceFilters(abundanceFilters);
        }

        marketMapFromPriceFileFactory.setSpeciesCodes(speciesCodesFactory.get());
        final MarketMap marketMap = marketMapFromPriceFileFactory.apply(fishState);

        portInitializer.buildPorts(
            fishState.getMap(),
            fishState.random,
            seaTile -> marketMap,
            fishState,
            new FixedGasPrice(0)
        );
        final List<Port> ports = fishState.getMap().getPorts();

        if (fadInitializerFactory instanceof AbundanceFadInitializerFactory) {
            ((FadInitializerFactory<AbundanceLocalBiology, AbundanceFad>) fadInitializerFactory)
                .setSpeciesCodes(speciesCodesFactory.get());
        }
        ((PluggableSelectivity) fadInitializerFactory)
            .setSelectivityFilters(abundanceFilters.get(FadSetAction.class));

        getPurseSeineGearFactory().setFadInitializerFactory(fadInitializerFactory);

        final GravityDestinationStrategyFactory gravityDestinationStrategyFactory =
            new GravityDestinationStrategyFactory();
        gravityDestinationStrategyFactory.setAttractionWeightsFile(getAttractionWeightsFile());
        gravityDestinationStrategyFactory.setMaxTripDurationFile(getVesselsFilePath());

        final FisherFactory fisherFactory = makeFisherFactory(
            fishState,
            regulationsFactory,
            gravityDestinationStrategyFactory
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(
                getVesselsFilePath(),
                TARGET_YEAR,
                fisherFactory,
                ports
            ).apply(fishState);

        ImmutableList.of(
            scheduledAbundanceProcessesFactory,
            abundanceRestorerFactory
        ).forEach(startableFactory ->
            fishState.registerStartable(startableFactory.apply(fishState))
        );

        scenarioPopulation.getPopulation().addAll(fishers);
        return scenarioPopulation;
    }

    @SuppressWarnings("unused")
    public AbundanceFadMapFactory getFadMapFactory() {
        return fadMapFactory;
    }

    @SuppressWarnings("unused")
    @Override
    public AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    @Override
    public void setFadInitializerFactory(
        final AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @Override
    public void useDummyData(final Path testPath) {
        super.useDummyData(testPath);
        setAttractionWeightsFile(
            testPath.resolve("dummy_action_weights.csv")
        );
        setLocationValuesFilePath(
            testPath.resolve("dummy_location_values.csv")
        );
    }

    @SuppressWarnings("unused")
    public void setFadMapFactory(final AbundanceFadMapFactory fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
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

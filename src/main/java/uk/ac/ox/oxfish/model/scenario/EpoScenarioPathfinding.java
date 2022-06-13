package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.*;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.*;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2016;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

public class EpoScenarioPathfinding extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private final SpeciesCodesFromFileFactory speciesCodesFactory =
            new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
    private final PortInitializer portInitializer =
            new FromSimpleFilePortInitializer(TARGET_YEAR, INPUT_PATH.resolve("ports.csv"));
    private final MarketMapFromPriceFileFactory marketMapFromPriceFileFactory =
            new MarketMapFromPriceFileFactory(INPUT_PATH.resolve("prices.csv"), TARGET_YEAR);
    private Path attractionWeightsFile = INPUT_PATH.resolve("action_weights.csv");
    private Path locationValuesFilePath = INPUT_PATH.resolve("location_values.csv");
    private RecruitmentProcessesFactory recruitmentProcessesFactory =
            new RecruitmentProcessesFactory(
                    INPUT_PATH.resolve("abundance").resolve("recruitment_parameters.csv")
            );
    private AbundanceMortalityProcessFromFileFactory abundanceMortalityProcessFactory =
            new AbundanceMortalityProcessFromFileFactory(
                    INPUT_PATH.resolve("abundance").resolve("mortality.csv"),
                    ImmutableList.of("natural", "obj_class_1_5", "noa_class_1_5", "longline")
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
    private FadMapFactory fadMapFactory = new AbundanceFadMapFactory(
            ImmutableMap.of(
                    Y2016, INPUT_PATH.resolve("currents").resolve("currents_2016.csv"),
                    Y2017, INPUT_PATH.resolve("currents").resolve("currents_2017.csv")
            )
    );
    private AbundanceFiltersFactory abundanceFiltersFactory =
            new AbundanceFiltersFactory(INPUT_PATH.resolve("abundance").resolve("selectivity.csv"));
    private AbundanceCatchSamplersFactory abundanceCatchSamplersFactory =
            new AbundanceCatchSamplersFactory();
    private DefaultToDestinationStrategyFishingStrategyFactory fishingStrategyFactory =
            new DefaultToDestinationStrategyFishingStrategyFactory();
    private AbundancePurseSeineGearFactory abundancePurseSeineGearFactory =
            new AbundancePurseSeineGearFactory();
    private AlgorithmFactory<? extends FadInitializer> fadInitializerFactory =
            new LastMomentAbundanceFadInitalizerFactory();
//            new AbundanceFadInitializerFactory(
//                    "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
//            );

    private AlgorithmFactory<? extends Regulation> regulationsFactory =
            new StandardIattcRegulationsFactory();

    private boolean zapper = false;
    private boolean zapperAge = false;

    /**
     * Just runs the scenario for a year.
     */
    public static void main(final String[] args) {
        final FishState fishState = new FishState();
        final Scenario scenario = new EpoAbundanceScenario();
        try {
            final File scenarioFile =
                    INPUT_PATH.resolve("abundance").resolve("scenario.yaml").toFile();
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        fishState.setScenario(scenario);
        fishState.start();
        while (fishState.getStep() < 365) {
            System.out.println("Step: " + fishState.getStep());
            fishState.schedule.step(fishState);
        }
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<AbundanceMortalityProcess> getAbundanceMortalityProcessFactory() {
        return abundanceMortalityProcessFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceMortalityProcessFactory(final AbundanceMortalityProcessFromFileFactory abundanceMortalityProcessFactory) {
        this.abundanceMortalityProcessFactory = abundanceMortalityProcessFactory;
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
    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public AbundancePurseSeineGearFactory getAbundancePurseSeineGearFactory() {
        return abundancePurseSeineGearFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundancePurseSeineGearFactory(
            final AbundancePurseSeineGearFactory abundancePurseSeineGearFactory
    ) {
        this.abundancePurseSeineGearFactory = abundancePurseSeineGearFactory;
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
    public AbundanceCatchSamplersFactory getAbundanceCatchSamplersFactory() {
        return abundanceCatchSamplersFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceCatchSamplersFactory(
            final AbundanceCatchSamplersFactory abundanceCatchSamplersFactory
    ) {
        this.abundanceCatchSamplersFactory = abundanceCatchSamplersFactory;
    }


    public DefaultToDestinationStrategyFishingStrategyFactory getFishingStrategyFactory() {
        return fishingStrategyFactory;
    }

    public void setFishingStrategyFactory(DefaultToDestinationStrategyFishingStrategyFactory fishingStrategyFactory) {
        this.fishingStrategyFactory = fishingStrategyFactory;
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
    public FadMapFactory getFadMapFactory() {
        return fadMapFactory;
    }

    @SuppressWarnings("unused")
    public void setFadMapFactory(final FadMapFactory fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
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

        abundanceMortalityProcessFactory.setSpeciesCodes(speciesCodes);
        scheduledAbundanceProcessesFactory.setRecruitmentProcesses(recruitmentProcesses);
        scheduledAbundanceProcessesFactory.setAbundanceReallocator(reallocator);
        scheduledAbundanceProcessesFactory.setAbundanceMortalityProcessFactory(
                abundanceMortalityProcessFactory
        );

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    private EPOPlannedStrategyFactory destinationStrategy =
            new EPOPlannedStrategyFactory();

    public EpoScenarioPathfinding() {

    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        super.setCatchSamplersFactory(abundanceCatchSamplersFactory);
        super.setFishingStrategyFactory(fishingStrategyFactory);
        super.setPurseSeineGearFactory(abundancePurseSeineGearFactory);
        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        abundanceFiltersFactory.setSpeciesCodes(speciesCodesFactory.get());
        final Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>>
                abundanceFilters = abundanceFiltersFactory.apply(fishState);

        abundanceCatchSamplersFactory.setAbundanceFilters(abundanceFilters);


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

        if(fadInitializerFactory instanceof AbstractAbundanceFadInitializerFactory)
            ((AbstractAbundanceFadInitializerFactory) fadInitializerFactory).setSpeciesCodes(speciesCodesFactory.get());
        ((PluggableSelectivity) fadInitializerFactory).setSelectivityFilters(abundanceFilters.get(FadSetAction.class));

        abundancePurseSeineGearFactory.setFadInitializerFactory(fadInitializerFactory);


        destinationStrategy.setAttractionWeightsFile(getAttractionWeightsFile());
        destinationStrategy.setMaxTripDurationFile(getVesselsFilePath());

        destinationStrategy.setCatchSamplersFactory(abundanceCatchSamplersFactory);
        destinationStrategy.setAttractionWeightsFile(attractionWeightsFile);
        final FisherFactory fisherFactory = makeFisherFactory(
                fishState,
                regulationsFactory,
                abundancePurseSeineGearFactory,
                destinationStrategy,
                fishingStrategyFactory,
                new PurseSeinerDepartingStrategyFactory(false)
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

        if(zapper) {
            Predicate<AbstractFad> predicate = zapperAge?
                    fad -> fad.getLocation().getGridX() <= 20 :
                    fad -> fad.getLocation().getGridX() <= 20 || fishState.getStep() - fad.getStepDeployed() > 150;
            fishState.registerStartable(
                    new FadZapper(
                            predicate
                    )
            );
        }
        scenarioPopulation.getPopulation().addAll(fishers);
        return scenarioPopulation;
    }

    @SuppressWarnings("WeakerAccess")
    public Path getLocationValuesFilePath() {
        return locationValuesFilePath;
    }

    @SuppressWarnings("WeakerAccess")
    public void setLocationValuesFilePath(final Path locationValuesFilePath) {
        this.locationValuesFilePath = locationValuesFilePath;
    }

    @SuppressWarnings("WeakerAccess")
    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    @SuppressWarnings("WeakerAccess")
    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
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

    public EPOPlannedStrategyFactory getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(EPOPlannedStrategyFactory destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public boolean isZapper() {
        return zapper;
    }

    public void setZapper(boolean zapper) {
        this.zapper = zapper;
    }

    public boolean isZapperAge() {
        return zapperAge;
    }

    public void setZapperAge(boolean zapperAge) {
        this.zapperAge = zapperAge;
    }
}



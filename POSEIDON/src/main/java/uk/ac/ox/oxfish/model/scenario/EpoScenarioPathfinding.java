package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.*;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LocalizedActionCounter;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.*;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

public class EpoScenarioPathfinding extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

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

    private DefaultToDestinationStrategyFishingStrategyFactory fishingStrategyFactory =
        new DefaultToDestinationStrategyFishingStrategyFactory();
    private AlgorithmFactory<? extends FadInitializer> fadInitializerFactory =
        new LastMomentAbundanceFadInitalizerFactory();

    private AlgorithmFactory<? extends Regulation> regulationsFactory =
        new StandardIattcRegulationsFactory();

    private WeightGroupsFactory weightGroupsFactory = new WeightGroupsFactory(
        speciesCodesSupplier.get().getSpeciesNames().stream().collect(
            toImmutableMap(identity(), __ -> ImmutableList.of("small", "medium", "large"))
        ),
        ImmutableMap.of(
            "Bigeye tuna", ImmutableList.of(12.0, 15.0),
            // use the last two bins of SKJ as "medium" and "large"
            "Skipjack tuna", ImmutableList.of(11.5016, 11.5019),
            "Yellowfin tuna", ImmutableList.of(12.0, 15.0)
        )
    );

    private boolean zapper = false;
    private boolean zapperAge = false;

    // private boolean galapagosZapper = false;
    private EPOPlannedStrategyFlexibleFactory destinationStrategy =
        new EPOPlannedStrategyFlexibleFactory(
            new AbundanceCatchSamplersFactory(),
            new InputFile(getInputFolder(), "action_weights.csv"),
            getVesselsFile()
        );

    public EpoScenarioPathfinding() {
        setPurseSeineGearFactory(new AbundancePurseSeineGearFactory(
            new InputFile(getInputFolder(), "location_values.csv")
        ));
    }

    /**
     * Just runs the scenario for a year.
     */
    public static void main(final String[] args) {
        final Path scenarioPath = Paths.get(
            System.getProperty("user.home"),
            "tmp", "calibrated_scenario.yaml"
        );
        new Runner<>(EpoScenarioPathfinding.class, scenarioPath).run(1, 1);
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
    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public DefaultToDestinationStrategyFishingStrategyFactory getFishingStrategyFactory() {
        return fishingStrategyFactory;
    }

    public void setFishingStrategyFactory(final DefaultToDestinationStrategyFishingStrategyFactory fishingStrategyFactory) {
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

    @Override
    public void useDummyData(final Path testPath) {
        super.useDummyData(testPath);
        getDestinationStrategy().setActionWeightsFile(
            new InputFile(new InputFolder(testPath), "dummy_action_weights.csv")
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
        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();

        final NauticalMap nauticalMap =
            mapInitializerFactory
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

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        super.setFishingStrategyFactory(fishingStrategyFactory);
        final LocalizedActionCounter calzone1 = new LocalizedActionCounter(
            abstractFadSetAction -> {
                final Coordinate coordinates = fishState.getMap().getCoordinates(abstractFadSetAction.getLocation());
                return coordinates.x <= -140;
            },
            "calzone1"
        );
        getPurseSeineGearFactory().getFadSetObservers().add(calzone1);
        final LocalizedActionCounter calzone2 = new LocalizedActionCounter(
            abstractFadSetAction -> {
                final Coordinate coordinates = fishState.getMap().getCoordinates(abstractFadSetAction.getLocation());
                return coordinates.x <= -90 & coordinates.x >= -130 & coordinates.y > 0;
            },
            "calzone2"
        );
        getPurseSeineGearFactory().getFadSetObservers().add(calzone2);

        //filter(lon_n>-140 & lon_n<=-110 & lat_n< 0)
        final LocalizedActionCounter thegap = new LocalizedActionCounter(
            abstractFadSetAction -> {
                final Coordinate coordinates = fishState.getMap().getCoordinates(abstractFadSetAction.getLocation());
                return coordinates.x <= -110 & coordinates.x > -140 & coordinates.y < 0;
            },
            "thegap"
        );
        getPurseSeineGearFactory().getFadSetObservers().add(thegap);
        //filter(lon_n>= -90 & lat_n <= -10)
        final LocalizedActionCounter calzone3 = new LocalizedActionCounter(
            abstractFadSetAction -> {
                final Coordinate coordinates = fishState.getMap().getCoordinates(abstractFadSetAction.getLocation());
                return coordinates.x >= -90 & coordinates.y <= -10;
            },
            "calzone3"
        );
        getPurseSeineGearFactory().getFadSetObservers().add(calzone3);


        fishState.registerStartable(calzone1);
        fishState.registerStartable(calzone2);
        fishState.registerStartable(calzone3);
        fishState.registerStartable(thegap);

        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        if (fadInitializerFactory instanceof AbstractAbundanceFadInitializerFactory)
            ((AbstractAbundanceFadInitializerFactory) fadInitializerFactory).setSpeciesCodes(speciesCodesSupplier.get());
        ((PluggableSelectivity) fadInitializerFactory).setSelectivityFilters(
            ((AbundanceCatchSamplersFactory) getDestinationStrategy().getCatchSamplersFactory())
                .getAbundanceFiltersFactory()
                .apply(fishState)
                .get(FadSetAction.class)
        );

        getPurseSeineGearFactory().setFadInitializerFactory(fadInitializerFactory);

        final FisherFactory fisherFactory = makeFisherFactory(
            fishState,
            regulationsFactory,
            getPurseSeineGearFactory(),
            destinationStrategy,
            fishingStrategyFactory,
            new PurseSeinerDepartingStrategyFactory(false)
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(
                getVesselsFile().get(),
                TARGET_YEAR,
                fisherFactory,
                buildPorts(fishState)
            ).
                apply(fishState);

        ImmutableList.of(
            scheduledAbundanceProcessesFactory,
            abundanceRestorerFactory
        ).forEach(startableFactory ->
            fishState.registerStartable(startableFactory.apply(fishState))
        );

        if (zapper) {
            final Predicate<AbstractFad> predicate = zapperAge ?
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

    public EPOPlannedStrategyFlexibleFactory getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(final EPOPlannedStrategyFlexibleFactory destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public boolean isZapper() {
        return zapper;
    }

    public void setZapper(final boolean zapper) {
        this.zapper = zapper;
    }

    public boolean isZapperAge() {
        return zapperAge;
    }

    public void setZapperAge(final boolean zapperAge) {
        this.zapperAge = zapperAge;
    }

    @SuppressWarnings("unused")
    public WeightGroupsFactory getWeightGroupsFactory() {
        return weightGroupsFactory;
    }

    @SuppressWarnings("unused")
    public void setWeightGroupsFactory(final WeightGroupsFactory weightGroupsFactory) {
        this.weightGroupsFactory = weightGroupsFactory;
    }

}



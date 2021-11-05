/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;
import static uk.ac.ox.oxfish.utility.Measures.DOLLAR;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.tuna.BiomassInitializer;
import uk.ac.ox.oxfish.biology.tuna.BiomassInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassReallocator;
import uk.ac.ox.oxfish.biology.tuna.BiomassReallocatorFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassRestorerFactory;
import uk.ac.ox.oxfish.biology.tuna.ScheduledBiomassProcessesFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.BiomassCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadMapFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.MarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The biomass-based IATTC tuna simulation scenario.
 */
public class EpoBiomassScenario extends EpoScenario<BiomassLocalBiology, BiomassFad> {

    private final FromSimpleFilePortInitializer portInitializer =
        new FromSimpleFilePortInitializer(TARGET_YEAR, INPUT_PATH.resolve("ports.csv"));
    private final List<AlgorithmFactory<? extends AdditionalStartable>> plugins = new ArrayList<>();
    private Path attractionWeightsFile = INPUT_PATH.resolve("action_weights.csv");
    private Path mapFile = INPUT_PATH.resolve("depth.csv");
    private Path boatsFile = INPUT_PATH.resolve("boats.csv");
    private boolean fadMortalityIncludedInExogenousCatches = true;
    private final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory =
        new BiomassDrivenTimeSeriesExogenousCatchesFactory(
            INPUT_PATH.resolve("biomass").resolve("exogenous_catches.csv"),
            TARGET_YEAR,
            fadMortalityIncludedInExogenousCatches
        );
    private FromFileMapInitializerFactory mapInitializer =
        new FromFileMapInitializerFactory(mapFile, 101, 0.5);
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
        new ConstantWeatherFactory();
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private FisherDefinition fisherDefinition = new FisherDefinition();
    private MarketMapFromPriceFileFactory marketMapFromPriceFileFactory =
        new MarketMapFromPriceFileFactory(INPUT_PATH.resolve("prices.csv"), TARGET_YEAR);

    private final BiomassReallocatorFactory biomassReallocatorFactory =
        new BiomassReallocatorFactory(
            INPUT_PATH.resolve("biomass").resolve("biomass_distributions.csv"),
            365
        );

    private BiomassInitializerFactory biomassInitializerFactory = new BiomassInitializerFactory();
    private BiomassRestorerFactory biomassRestorerFactory = new BiomassRestorerFactory();
    private ScheduledBiomassProcessesFactory
        scheduledBiomassProcessesFactory = new ScheduledBiomassProcessesFactory();
    private BiomassFadMapFactory fadMapFactory = new BiomassFadMapFactory(currentFiles);

    public EpoBiomassScenario() {

        final BiomassPurseSeineGearFactory
            purseSeineGearFactory = new BiomassPurseSeineGearFactory();

        final BiomassFadInitializerFactory fadInitializerFactory =
            (BiomassFadInitializerFactory) purseSeineGearFactory.getFadInitializerFactory();

        // By setting all coefficients to zero, we'll get a 0.5 probability of attraction
        fadInitializerFactory.setAttractionIntercepts(ImmutableMap.of(
            "Bigeye tuna", new FixedDoubleParameter(0.0),
            "Yellowfin tuna", new FixedDoubleParameter(0.0),
            "Skipjack tuna", new FixedDoubleParameter(0.0)
        ));
        fadInitializerFactory.setTileBiomassCoefficients(ImmutableMap.of(
            "Bigeye tuna", new FixedDoubleParameter(0.0),
            "Yellowfin tuna", new FixedDoubleParameter(0.0),
            "Skipjack tuna", new FixedDoubleParameter(0.0)
        ));
        fadInitializerFactory.setBiomassInteractionsCoefficients(ImmutableMap.of(
            "Bigeye tuna", new FixedDoubleParameter(0.0),
            "Yellowfin tuna", new FixedDoubleParameter(0.0),
            "Skipjack tuna", new FixedDoubleParameter(0.0)
        ));
        fadInitializerFactory.setGrowthRates(ImmutableMap.of(
            "Bigeye tuna", new FixedDoubleParameter(0.1),
            "Yellowfin tuna", new FixedDoubleParameter(0.1),
            "Skipjack tuna", new FixedDoubleParameter(0.1)
        ));

        final AlgorithmFactory<? extends Regulation> standardRegulations =
            new StandardIattcRegulationsFactory();

        fisherDefinition.setRegulation(standardRegulations);
        fisherDefinition.setGear(purseSeineGearFactory);
        fisherDefinition.setGearStrategy(new FadRefillGearStrategyFactory());

        final PurseSeinerBiomassFishingStrategyFactory
            fishingStrategy = new PurseSeinerBiomassFishingStrategyFactory();
        fishingStrategy.setCatchSamplersFactory(new BiomassCatchSamplersFactory());
        fisherDefinition.setFishingStrategy(fishingStrategy);

        final GravityDestinationStrategyFactory gravityDestinationStrategyFactory =
            new GravityDestinationStrategyFactory();
        gravityDestinationStrategyFactory.setAttractionWeightsFile(getAttractionWeightsFile());
        fisherDefinition.setDestinationStrategy(gravityDestinationStrategyFactory);

        fisherDefinition.setDepartingStrategy(new PurseSeinerDepartingStrategyFactory());

    }

    @SuppressWarnings("WeakerAccess")
    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    @SuppressWarnings("unused")
    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    public static String getBoatId(final Fisher fisher) {
        return fisher.getTags().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Boat id not set for " + fisher));
    }

    public static int dayOfYear(final Month month, final int dayOfMonth) {
        return LocalDate.of(TARGET_YEAR, month, dayOfMonth)
            .getDayOfYear();
    }

    @SuppressWarnings("unused")
    public BiomassRestorerFactory getMultiSpeciesBiomassRestorerFactory() {
        return biomassRestorerFactory;
    }

    @SuppressWarnings("unused")
    public void setMultiSpeciesBiomassRestorerFactory(final BiomassRestorerFactory biomassRestorerFactory) {
        this.biomassRestorerFactory = biomassRestorerFactory;
    }

    @SuppressWarnings("unused")
    public ScheduledBiomassProcessesFactory getBiomassReallocatorFactory() {
        return scheduledBiomassProcessesFactory;
    }

    @SuppressWarnings("unused")
    public void setBiomassReallocatorFactory(final ScheduledBiomassProcessesFactory scheduledBiomassProcessesFactory) {
        this.scheduledBiomassProcessesFactory = scheduledBiomassProcessesFactory;
    }

    @SuppressWarnings("unused")
    public MarketMapFromPriceFileFactory getMarketMapFromPriceFileFactory() {
        return marketMapFromPriceFileFactory;
    }

    @SuppressWarnings("unused")
    public void setMarketMapFromPriceFileFactory(final MarketMapFromPriceFileFactory marketMapFromPriceFileFactory) {
        this.marketMapFromPriceFileFactory = marketMapFromPriceFileFactory;
    }

    @SuppressWarnings("unused")
    public Path getMapFile() {
        return mapFile;
    }

    @SuppressWarnings("unused")
    public void setMapFile(final Path mapFile) {
        this.mapFile = mapFile;
    }

    @SuppressWarnings("unused")
    public Path getBoatsFile() {
        return boatsFile;
    }

    public void setBoatsFile(final Path boatsFile) {
        this.boatsFile = boatsFile;
    }

    public BiomassDrivenTimeSeriesExogenousCatchesFactory getExogenousCatchesFactory() {
        return exogenousCatchesFactory;
    }

    @SuppressWarnings("unused")
    public Path getPortFilePath() {
        return portInitializer.getFilePath();
    }

    public FromFileMapInitializerFactory getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
        final FromFileMapInitializerFactory mapInitializer
    ) {
        this.mapInitializer = mapInitializer;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    @SuppressWarnings("unused")
    public void setWeatherInitializer(
        final AlgorithmFactory<? extends WeatherInitializer> weatherInitializer
    ) {
        this.weatherInitializer = weatherInitializer;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(final DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    @SuppressWarnings("unused")
    public FisherDefinition getFisherDefinition() {
        return fisherDefinition;
    }

    @SuppressWarnings("unused")
    public void setFisherDefinition(final FisherDefinition fisherDefinition) {
        this.fisherDefinition = fisherDefinition;
    }

    @Override
    public ScenarioEssentials start(final FishState model) {

        System.out.println("Starting model...");

        final NauticalMap nauticalMap =
            mapInitializer.apply(model).makeMap(model.random, null, model);

        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();
        biomassReallocatorFactory.setSpeciesCodes(speciesCodes);
        biomassReallocatorFactory.setMapExtent(new MapExtent(nauticalMap));
        final BiomassReallocator biomassReallocator = biomassReallocatorFactory.apply(model);
        scheduledBiomassProcessesFactory.setBiomassReallocator(biomassReallocator);

        plugins.add(scheduledBiomassProcessesFactory);

        biomassRestorerFactory.setBiomassReallocator(biomassReallocator);
        plugins.add(biomassRestorerFactory);

        biomassInitializerFactory.setBiomassReallocator(biomassReallocator);

        final BiomassInitializer biologyInitializer = biomassInitializerFactory.apply(model);
        final GlobalBiology globalBiology = biologyInitializer.generateGlobal(model.random, model);
        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));

        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(
            nauticalMap,
            model.random,
            biologyInitializer,
            this.weatherInitializer.apply(model),
            globalBiology,
            model
        );

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    @Override
    public ScenarioPopulation populateModel(final FishState model) {

        initModel(model);

        final Double gasPrice = gasPricePerLiter.apply(model.random);
        final GasPriceMaker gasPriceMaker = new FixedGasPrice(gasPrice);

        marketMapFromPriceFileFactory.setSpeciesCodes(speciesCodesSupplier.get());
        final MarketMap marketMap = marketMapFromPriceFileFactory.apply(model);
        portInitializer
            .buildPorts(model.getMap(), model.random, seaTile -> marketMap, model, gasPriceMaker)
            .forEach(port -> port.setGasPricePerLiter(gasPrice));

        final List<Port> ports = model.getMap().getPorts();
        checkState(!ports.isEmpty());

        final BiomassPurseSeineGearFactory purseSeineGearFactory =
            (BiomassPurseSeineGearFactory) fisherDefinition.getGear();
        final FisherFactory fisherFactory = makeFisherFactory(
            model,
            purseSeineGearFactory,
            (GravityDestinationStrategyFactory) fisherDefinition.getDestinationStrategy(),
            fisherDefinition.getFishingStrategy()
        );

        final Monitors monitors = new Monitors(model);
        monitors.getMonitors().forEach(model::registerStartable);

        purseSeineGearFactory.getFadDeploymentObservers()
            .addAll(monitors.getFadDeploymentMonitors());
        purseSeineGearFactory.getFadSetObservers().addAll(monitors.getFadSetMonitors());
        purseSeineGearFactory.getNonAssociatedSetObservers()
            .addAll(monitors.getNonAssociatedSetMonitors());
        purseSeineGearFactory.getDolphinSetObservers().addAll(monitors.getDolphinSetMonitors());
        purseSeineGearFactory.setBiomassLostMonitor(monitors.getBiomassLostMonitor());

        model.getYearlyDataSet().registerGatherer(
            "Total profits",
            fishState -> fishState.getFishers()
                .stream()
                .mapToDouble(fisher -> fisher.getLatestYearlyObservation("Profits"))
                .sum(),
            Double.NaN,
            DOLLAR,
            "Profits"
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(boatsFile, TARGET_YEAR, fisherFactory, ports).apply(model);

        // Mutate the fisher factory back into a random boat generator
        // TODO: we don't have boat entry in the tuna model for now, but when we do, this
        //  shouldn't be entirely random
        fisherFactory.setBoatSupplier(fisherDefinition.makeBoatSupplier(model.random));
        fisherFactory.setHoldSupplier(fisherDefinition.makeHoldSupplier(
            model.random,
            model.getBiology()
        ));
        fisherFactory.setPortSupplier(() -> oneOf(ports, model.random));

        final Map<String, FisherFactory> fisherFactories =
            ImmutableMap.of(FishState.DEFAULT_POPULATION_NAME, fisherFactory);

        final SocialNetwork network = new SocialNetwork(new EmptyNetworkBuilder());

        exogenousCatchesFactory.setSpeciesCodes(speciesCodesSupplier.get());
        final ExogenousCatches exogenousCatches = exogenousCatchesFactory.apply(model);
        model.registerStartable(exogenousCatches);

        plugins.forEach(plugin -> model.registerStartable(plugin.apply(model)));

        return new ScenarioPopulation(
            fishers,
            network,
            fisherFactories
        );

    }


    @SuppressWarnings("unused")
    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public void addPlugin(final AlgorithmFactory<? extends AdditionalStartable> plugin) {
        plugins.add(plugin);
    }

    @SuppressWarnings("unused")
    public boolean isFadMortalityIncludedInExogenousCatches() {
        return fadMortalityIncludedInExogenousCatches;
    }

    @SuppressWarnings("unused")
    public void setFadMortalityIncludedInExogenousCatches(final boolean fadMortalityIncludedInExogenousCatches) {
        this.fadMortalityIncludedInExogenousCatches = fadMortalityIncludedInExogenousCatches;
    }

    @SuppressWarnings("unused")
    public BiomassInitializerFactory getBiomassReallocatorInitializerFactory() {
        return biomassInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setBiomassReallocatorInitializerFactory(final BiomassInitializerFactory biomassInitializerFactory) {
        this.biomassInitializerFactory = biomassInitializerFactory;
    }

    public BiomassFadMapFactory getFadMapFactory() {
        return fadMapFactory;
    }

    @SuppressWarnings("unused")
    public void setFadMapFactory(final BiomassFadMapFactory fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
    }

}

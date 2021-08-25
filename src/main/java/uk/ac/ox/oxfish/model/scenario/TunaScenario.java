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
import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static si.uom.NonSI.KNOT;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EARNINGS;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.VARIABLE_COSTS;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;
import static uk.ac.ox.oxfish.utility.Measures.DOLLAR;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import ec.util.MersenneTwisterFast;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Volume;
import sim.engine.Steppable;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassInitializer;
import uk.ac.ox.oxfish.biology.tuna.BiomassInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassReallocator;
import uk.ac.ox.oxfish.biology.tuna.BiomassReallocatorFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassRestorerFactory;
import uk.ac.ox.oxfish.biology.tuna.ScheduledBiomassProcessesFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedRestTimeDepartingStrategy;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.fads.BiomassFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.MarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromCoordinatesFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromShapeFileFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The biomass-based IATTC tuna simulation scenario.
 */
@SuppressWarnings("UnstableApiUsage")
public class TunaScenario implements Scenario {

    public static final int TARGET_YEAR = 2017;
    public static final AlgorithmFactory<TemporaryRegulation> closureAReg =
        new TemporaryRegulationFactory(
            dayOfYear(JULY, 29), dayOfYear(OCTOBER, 8),
            new NoFishingFactory()
        );
    public static final AlgorithmFactory<TemporaryRegulation> closureBReg =
        new TemporaryRegulationFactory(
            dayOfYear(NOVEMBER, 9), dayOfYear(JANUARY, 19),
            new NoFishingFactory()
        );
    public static final AlgorithmFactory<TemporaryRegulation> elCorralitoReg =
        new TemporaryRegulationFactory(
            dayOfYear(OCTOBER, 9), dayOfYear(NOVEMBER, 8),
            new SpecificProtectedAreaFromCoordinatesFactory(4, -110, -3, -96)
        );
    private static final Path INPUT_DIRECTORY = Paths.get("inputs", "tuna");
    public static final ImmutableMap<CurrentPattern, Path> currentFiles =
        new ImmutableMap.Builder<CurrentPattern, Path>()
            //.put(Y2015, input("currents_2015.csv"))
            //.put(Y2016, input("currents_2016.csv"))
            .put(Y2017, input("currents_2017.csv"))
            //.put(Y2018, input("currents_2018.csv"))
            //.put(NEUTRAL, input("currents_neutral.csv"))
            //.put(EL_NINO, input("currents_el_nino.csv"))
            //.put(LA_NINA, input("currents_la_nina.csv"))
            .build();
    public static final SpeciesCodesFromFileFactory speciesCodesSupplier =
        new SpeciesCodesFromFileFactory(input("species_codes.csv"));

    private static final Path GALAPAGOS_EEZ_SHAPE_FILE = input("galapagos_eez").resolve("eez.shp");
    public static final AlgorithmFactory<SpecificProtectedArea> galapagosEezReg =
        new SpecificProtectedAreaFromShapeFileFactory(GALAPAGOS_EEZ_SHAPE_FILE);
    private final FromSimpleFilePortInitializer portInitializer =
        new FromSimpleFilePortInitializer(TARGET_YEAR, input("ports.csv"));
    private final List<AlgorithmFactory<? extends AdditionalStartable>> plugins = new ArrayList<>();
    private Path attractionWeightsFile = input("action_weights.csv");
    private Path mapFile = input("depth.csv");
    private Path boatsFile = input("boats.csv");
    private Path costsFile = input("costs.csv");
    private boolean fadMortalityIncludedInExogenousCatches = true;
    private final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory =
        new BiomassDrivenTimeSeriesExogenousCatchesFactory(
            input("exogenous_catches.csv"),
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
        new MarketMapFromPriceFileFactory(input("prices.csv"), TARGET_YEAR);

    private BiomassReallocatorFactory biomassReallocatorFactory =
        new BiomassReallocatorFactory(
            input("biomass_distributions.csv"),
            365
        );

    private BiomassInitializerFactory biomassInitializerFactory = new BiomassInitializerFactory();
    private BiomassRestorerFactory biomassRestorerFactory = new BiomassRestorerFactory();
    private ScheduledBiomassProcessesFactory
        scheduledBiomassProcessesFactory = new ScheduledBiomassProcessesFactory();
    private BiomassFadMapFactory fadMapFactory = new BiomassFadMapFactory(currentFiles);

    public TunaScenario() {

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
        fadInitializerFactory.setFadBiomassCoefficients(ImmutableMap.of(
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
            new MultipleRegulationsFactory(ImmutableMap.of(
                galapagosEezReg, TAG_FOR_ALL,
                elCorralitoReg, TAG_FOR_ALL,
                closureAReg, "closure A",
                closureBReg, "closure B"
            ));

        fisherDefinition.setRegulation(standardRegulations);
        fisherDefinition.setGear(purseSeineGearFactory);
        fisherDefinition.setGearStrategy(new FadRefillGearStrategyFactory());
        final AlgorithmFactory<PurseSeinerFishingStrategy<BiomassLocalBiology, BiomassFad>>
            fishingStrategy = new PurseSeinerBiomassFishingStrategyFactory();
        fisherDefinition.setFishingStrategy(fishingStrategy);
        fisherDefinition.setDestinationStrategy(new GravityDestinationStrategyFactory());
        fisherDefinition.setDepartingStrategy(new PurseSeinerDepartingStrategyFactory());

    }

    public static Path input(final String filename) {
        return INPUT_DIRECTORY.resolve(filename);
    }

    public static int dayOfYear(final Month month, final int dayOfMonth) {
        return LocalDate.of(TARGET_YEAR, month, dayOfMonth)
            .getDayOfYear();
    }

    public static String getBoatId(final Fisher fisher) {
        return fisher.getTags().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Boat id not set for " + fisher));
    }

    /**
     * Recursively find fixed rest time departing strategies and set minimumHoursToWait
     */
    private static void setFixedRestTime(
        final DepartingStrategy departingStrategy,
        final double minimumHoursToWait
    ) {
        if (departingStrategy instanceof FixedRestTimeDepartingStrategy) {
            ((FixedRestTimeDepartingStrategy) departingStrategy).setMinimumHoursToWait(
                minimumHoursToWait);
        } else if (departingStrategy instanceof CompositeDepartingStrategy) {
            ((CompositeDepartingStrategy) departingStrategy).getStrategies()
                .forEach(s -> setFixedRestTime(s, minimumHoursToWait));
        }
    }

    private static void scheduleClosurePeriodChoice(final FishState model, final Fisher fisher) {
        // Every year, on July 15th, purse seine vessels must choose which temporal closure
        // period they will observe.
        final int daysFromNow = 1 + dayOfYear(JULY, 15);
        final Steppable assignClosurePeriod = simState -> {
            if (fisher.getRegulation() instanceof MultipleRegulations) {
                chooseClosurePeriod(fisher, model.getRandom());
                ((MultipleRegulations) fisher.getRegulation()).reassignRegulations(model, fisher);
            }
        };
        model.scheduleOnceInXDays(assignClosurePeriod, StepOrder.DAWN, daysFromNow);
        model.scheduleOnceInXDays(
            simState -> model.scheduleEveryXDay(assignClosurePeriod, StepOrder.DAWN, 365),
            StepOrder.DAWN,
            daysFromNow
        );
    }

    private static void chooseClosurePeriod(final Fisher fisher, final MersenneTwisterFast rng) {
        final ImmutableList<String> periods = ImmutableList.of("closure A", "closure B");
        fisher.getTags().removeIf(periods::contains);
        fisher.getTags().add(oneOf(periods, rng));
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

    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    @SuppressWarnings("unused")
    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
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

        System.out.println("Populating model...");

        final FadMap fadMap = fadMapFactory.apply(model);
        model.setFadMap(fadMap);
        model.registerStartable(fadMap);

        final Double gasPrice = gasPricePerLiter.apply(model.random);
        final GasPriceMaker gasPriceMaker = new FixedGasPrice(gasPrice);

        marketMapFromPriceFileFactory.setSpeciesCodes(speciesCodesSupplier.get());
        final MarketMap marketMap = marketMapFromPriceFileFactory.apply(model);
        portInitializer
            .buildPorts(model.getMap(), model.random, seaTile -> marketMap, model, gasPriceMaker)
            .forEach(port -> port.setGasPricePerLiter(gasPrice));

        final List<Port> ports = model.getMap().getPorts();
        checkState(!ports.isEmpty());

        final FisherFactory fisherFactory = fisherDefinition.getFisherFactory(model, ports, 0);
        final BiomassPurseSeineGearFactory purseSeineGearFactory =
            (BiomassPurseSeineGearFactory) fisherDefinition.getGear();

        final Monitors monitors = new Monitors(model);
        monitors.getMonitors().forEach(model::registerStartable);

        purseSeineGearFactory.getFadDeploymentObservers()
            .addAll(monitors.getFadDeploymentMonitors());
        purseSeineGearFactory.getFadSetObservers().addAll(monitors.getFadSetMonitors());
        purseSeineGearFactory.getNonAssociatedSetObservers()
            .addAll(monitors.getNonAssociatedSetMonitors());
        purseSeineGearFactory.getDolphinSetObservers().addAll(monitors.getDolphinSetMonitors());
        purseSeineGearFactory.setBiomassLostMonitor(monitors.getBiomassLostMonitor());

        final Map<String, Port> portsByName =
            ports.stream().collect(toMap(Port::getName, identity()));
        final Supplier<FuelTank> fuelTankSupplier = () -> new FuelTank(Double.MAX_VALUE);

        fisherFactory.getAdditionalSetups().addAll(ImmutableList.of(
            addHourlyCosts(),
            fisher -> ((PurseSeineGear) fisher.getGear()).getFadManager().setFisher(fisher),
            fisher -> scheduleClosurePeriodChoice(model, fisher),
            fisher -> fisher.getYearlyData().registerGatherer(
                "Profits",
                fisher1 -> fisher1.getYearlyCounterColumn(EARNINGS)
                    - fisher1.getYearlyCounterColumn(VARIABLE_COSTS),
                Double.NaN
            ),
            fisher -> fisher.getYearlyCounter().addColumn("Distance travelled"),
            fisher -> fisher.getYearlyData().registerGatherer("Distance travelled", fisher1 ->
                fisher1.getYearlyCounterColumn("Distance travelled"), Double.NaN
            ),
            fisher -> fisher.addTripListener((tripRecord, fisher1) ->
                fisher1.getYearlyCounter()
                    .count("Distance travelled", tripRecord.getDistanceTravelled())
            )
        ));

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

        final Map<String, Fisher> fishersByBoatId = parseAllRecords(boatsFile).stream()
            .filter(record -> record.getInt("year") == TARGET_YEAR)
            .collect(toMap(
                record -> record.getString("boat_id"),
                record -> {
                    final String portName = record.getString("port_name");
                    final Double length = record.getDouble("length_in_m");
                    final Quantity<Mass> carryingCapacity =
                        getQuantity(record.getDouble("carrying_capacity_in_t"), TONNE);
                    final double carryingCapacityInKg = asDouble(carryingCapacity, KILOGRAM);
                    final Quantity<Volume> holdVolume =
                        getQuantity(record.getDouble("hold_volume_in_m3"), CUBIC_METRE);
                    final Quantity<Speed> speed =
                        getQuantity(record.getDouble("speed_in_knots"), KNOT);
                    final Engine engine = new Engine(
                        Double.NaN, // Unused
                        1.0, // This is not realistic, but fuel costs are wrapped into daily costs
                        asDouble(speed, KILOMETRE_PER_HOUR)
                    );
                    fisherFactory.setPortSupplier(() -> portsByName.get(portName));
                    // we don't have beam width in the data file, but it isn't used anyway
                    final double beam = 1.0;
                    fisherFactory.setBoatSupplier(() -> new Boat(
                        length,
                        beam,
                        engine,
                        fuelTankSupplier.get()
                    ));
                    fisherFactory.setHoldSupplier(() -> new Hold(
                        carryingCapacityInKg,
                        holdVolume,
                        model.getBiology()
                    ));
                    final String boatId = record.getString("boat_id");
                    final Fisher fisher = fisherFactory.buildFisher(model);
                    fisher.getTags().add(boatId);
                    setFixedRestTime(
                        fisher.getDepartingStrategy(),
                        record.getDouble("mean_time_at_port_in_hours")
                    );
                    chooseClosurePeriod(fisher, model.getRandom());
                    // TODO: setMaxTravelTime(fisher, record.getDouble
                    //  ("max_trip_duration_in_hours"));
                    return fisher;
                }
            ));

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
            new ArrayList<>(fishersByBoatId.values()),
            network,
            fisherFactories
        );

    }

    private Consumer<Fisher> addHourlyCosts() {
        final RangeMap<ComparableQuantity<Mass>, HourlyCost> hourlyCostsPerCarryingCapacity =
            parseAllRecords(costsFile).stream().collect(toImmutableRangeMap(
                r -> Range.openClosed(
                    getQuantity(r.getInt("lower_capacity"), TONNE),
                    getQuantity(r.getInt("upper_capacity"), TONNE)
                ),
                r -> new HourlyCost(r.getDouble("daily_cost") / 24.0)
            ));
        // Setup hourly costs as a function of capacity
        return fisher -> {
            final ComparableQuantity<Mass> capacity =
                getQuantity(fisher.getHold().getMaximumLoad(), KILOGRAM);
            final HourlyCost hourlyCost = hourlyCostsPerCarryingCapacity.get(capacity);
            fisher.getAdditionalTripCosts().add(hourlyCost);
        };
    }

    @SuppressWarnings("unused")
    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public void addPlugin(final AlgorithmFactory<? extends AdditionalStartable> plugin) {
        plugins.add(plugin);
    }

    @SuppressWarnings("unused")
    public Path getCostsFile() {
        return costsFile;
    }

    public void setCostsFile(final Path costsFile) {
        this.costsFile = costsFile;
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

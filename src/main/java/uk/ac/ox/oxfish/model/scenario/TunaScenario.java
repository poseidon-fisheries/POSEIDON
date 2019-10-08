package uk.ac.ox.oxfish.model.scenario;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.lang3.tuple.Triple;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.growers.FadAwareCommonLogisticGrowerInitializerFactory;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.CoordinateFileAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.FileBiomassAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.PolygonAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.SmootherFileAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBiomassNormalizedFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.destination.FadDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FadDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FadFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.fads.IATTC;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Volume;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static si.uom.NonSI.KNOT;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class TunaScenario implements Scenario {

    public static final Path INPUT_DIRECTORY = Paths.get("inputs", "tuna");
    public static final Path MAP_FILE = input("depth.csv");
    public static final Path DEPLOYMENT_VALUES_FILE = input("deployment_values.csv");
    private static final Path IATTC_SHAPE_FILE = input("shape").resolve("RFB_IATTC.shp");
    private static final Path PORTS_FILE = input("ports.csv");
    private static final Path PRICES_FILE = input("prices.csv");
    private static final Path COSTS_FILE = input("costs.csv");
    private static final Path BOATS_FILE = input("boats.csv");
    private static final Path BOAT_SPEEDS_FILE = input("boat_speeds.csv");
    private static final Path SPECIES_NAMES_FILE = input("species_names.csv");
    private static final Path SCHAEFER_PARAMS_FILE = input("schaefer_params.csv");
    private static final ImmutableMap<String, Path> biomassFiles = ImmutableMap.of(
        "BET", input("habitability_bet_2006-01-07.csv"),
        "SKJ", input("biomass_skj_2006-01-15.csv"),
        "YFT", input("biomass_yft_2006-01-15.csv")
    );
    private static final BiMap<String, String> speciesNames = parseAllRecords(SPECIES_NAMES_FILE).stream().collect(toImmutableBiMap(
        r -> r.getString("code"),
        r -> r.getString("name")
    ));
    private static final Path CURRENTS_FILE = input("currents.csv");
    private final FromSimpleFilePortInitializer portInitializer = new FromSimpleFilePortInitializer(PORTS_FILE);
    private FromFileMapInitializerFactory mapInitializer = new FromFileMapInitializerFactory(MAP_FILE, 94, 0.5);
    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializers = new TunaSpeciesBiomassInitializerFactory();
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer = new ConstantWeatherFactory();
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private FisherDefinition fisherDefinition = new FisherDefinition();
    private int targetYear = 2018;

    TunaScenario() {
        fisherDefinition.setGear(new PurseSeineGearFactory());
        fisherDefinition.setFishingStrategy(new FadFishingStrategyFactory());
        fisherDefinition.setDestinationStrategy(new FadDestinationStrategyFactory());
    }

    private static Path input(String filename) { return INPUT_DIRECTORY.resolve(filename); }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializers() {
        return biologyInitializers;
    }

    @SuppressWarnings("unused")
    public void setBiologyInitializers(AlgorithmFactory<? extends BiologyInitializer> biologyInitializers) {
        this.biologyInitializers = biologyInitializers;
    }

    @SuppressWarnings("unused")
    public Path getPortFilePath() {
        return portInitializer.getFilePath();
    }

    @SuppressWarnings("unused")
    public void setPortFilePath(Path filePath) {
        portInitializer.setFilePath(filePath);
    }

    public FromFileMapInitializerFactory getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
        FromFileMapInitializerFactory mapInitializer
    ) {
        this.mapInitializer = mapInitializer;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    @SuppressWarnings("unused")
    public void setWeatherInitializer(
        AlgorithmFactory<? extends WeatherInitializer> weatherInitializer
    ) {
        this.weatherInitializer = weatherInitializer;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    @SuppressWarnings("unused")
    public FisherDefinition getFisherDefinition() {
        return fisherDefinition;
    }

    @SuppressWarnings("unused")
    public void setFisherDefinition(FisherDefinition fisherDefinition) {
        this.fisherDefinition = fisherDefinition;
    }

    @Override
    public ScenarioEssentials start(FishState model) {
        final BiologyInitializer biologyInitializer = biologyInitializers.apply(model);
        final GlobalBiology globalBiology = biologyInitializer.generateGlobal(model.random, model);
        final WeatherInitializer weatherInitializer = this.weatherInitializer.apply(model);
        final NauticalMap nauticalMap = mapInitializer.apply(model).makeMap(model.random, globalBiology, model);
        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));

        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(
            nauticalMap, model.random, biologyInitializer, weatherInitializer, globalBiology, model
        );

        final Double gasPrice = gasPricePerLiter.apply(model.random);
        final FixedGasPrice fixedGasPrice = new FixedGasPrice(gasPrice);

        final MarketMap marketMap = makeMarketMap(globalBiology);
        portInitializer
            .buildPorts(nauticalMap, model.random, seaTile -> marketMap, model, fixedGasPrice)
            .forEach(port -> port.setGasPricePerLiter(gasPrice));

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    private MarketMap makeMarketMap(GlobalBiology globalBiology) {
        Map<String, Double> prices = parseAllRecords(PRICES_FILE).stream()
            .filter(
                r -> r.getInt("year") == targetYear
            )
            .collect(toMap(
                r -> r.getString("species_code"),
                r -> r.getDouble("price_per_tonne") / 1000.0 // convert to price / kg
            ));
        final MarketMap marketMap = new MarketMap(globalBiology);
        globalBiology.getSpecies().forEach(species -> {
            final String speciesCode = speciesNames.inverse().get(species.getName());
            marketMap.addMarket(species, new FixedPriceMarket(prices.get(speciesCode)));
        });
        return marketMap;
    }

    @Override
    public ScenarioPopulation populateModel(FishState model) {

        FadMapFactory fadMapFactory = new FadMapFactory(CURRENTS_FILE);
        final FadMap fadMap = fadMapFactory.apply(model);
        model.setFadMap(fadMap);
        model.registerStartable(fadMap);

        final LinkedList<Port> ports = model.getMap().getPorts();
        Preconditions.checkState(!ports.isEmpty());

        final RangeMap<ComparableQuantity<Mass>, HourlyCost> hourlyCostsPerCarryingCapacity =
            parseAllRecords(COSTS_FILE).stream().collect(toImmutableRangeMap(
                r -> Range.openClosed(
                    getQuantity(r.getInt("lower_capacity"), TONNE),
                    getQuantity(r.getInt("upper_capacity"), TONNE)
                ),
                r -> new HourlyCost(r.getDouble("daily_cost") / 24.0)
            ));

        FisherFactory fisherFactory = fisherDefinition.getFisherFactory(model, ports, 0);
        fisherFactory.getAdditionalSetups().add(fisher -> {
            final ComparableQuantity<Mass> capacity = getQuantity(fisher.getHold().getMaximumLoad(), KILOGRAM);
            final HourlyCost hourlyCost = hourlyCostsPerCarryingCapacity.get(capacity);
            fisher.getAdditionalTripCosts().add(hourlyCost);
        });
        fisherFactory.getAdditionalSetups().add(fisher ->
            ((PurseSeineGear) fisher.getGear()).getFadManager().setFisher(fisher)
        );

        final Map<String, Port> portsByName = ports.stream().collect(toMap(Port::getName, identity()));

        final Supplier<FuelTank> fuelTankSupplier = () -> new FuelTank(Double.POSITIVE_INFINITY);

        final Map<Integer, Quantity<Speed>> speedsPerClass =
            parseAllRecords(BOAT_SPEEDS_FILE).stream().collect(toMap(
                r -> r.getInt("class"),
                r -> getQuantity(r.getDouble("speed"), KNOT))
            );

        final Map<String, Fisher> fishersByBoatId =
            parseAllRecords(BOATS_FILE).stream()
                .filter(record -> record.getInt("year") == targetYear)
                .limit(10)
                .collect(toMap(
                    record -> record.getString("boat_id"),
                    record -> {
                        final String portName = record.getString("port_name");
                        final Double length = record.getDouble("length_in_m");
                        final double beam = 1.0; // we don't have beam width in the data file, but it isn't used anyway
                        final Quantity<Mass> carryingCapacity = getQuantity(record.getDouble("carrying_capacity_in_t"), TONNE);
                        final Quantity<Volume> holdVolume = getQuantity(record.getDouble("hold_volume_in_m3"), CUBIC_METRE);
                        final int capacityClass = IATTC.capacityClass(holdVolume);
                        final Engine engine = new Engine(
                            Double.NaN, // Unused
                            0.0, // TODO
                            asDouble(speedsPerClass.get(capacityClass), KILOMETRE_PER_HOUR)
                        );
                        fisherFactory.setPortSupplier(() -> portsByName.get(portName));
                        fisherFactory.setBoatSupplier(() -> new Boat(length, beam, engine, fuelTankSupplier.get()));
                        fisherFactory.setHoldSupplier(() -> new Hold(asDouble(carryingCapacity, KILOGRAM), holdVolume, model.getBiology()));
                        final Fisher fisher = fisherFactory.buildFisher(model);
                        fisher.getTags().add(record.getString("boat_id"));
                        return fisher;
                    }));

        assignDeploymentLocationValues(model.getMap(), fishersByBoatId);

        // Mutate the fisher factory back into a random boat generator
        // TODO: we don't have boat entry in the tuna model for now, but when we do, this shouldn't be entirely random
        fisherFactory.setBoatSupplier(fisherDefinition.makeBoatSupplier(model.random));
        fisherFactory.setHoldSupplier(fisherDefinition.makeHoldSupplier(model.random, model.getBiology()));
        fisherFactory.setPortSupplier(() -> oneOf(ports, model.random));

        final Map<String, FisherFactory> fisherFactories =
            ImmutableMap.of(FishState.DEFAULT_POPULATION_NAME, fisherFactory);

        final SocialNetwork network = new SocialNetwork(new EmptyNetworkBuilder());

        return new ScenarioPopulation(fishersByBoatId.values().stream().collect(toList()), network, fisherFactories);

    }

    private void assignDeploymentLocationValues(NauticalMap nauticalMap, Map<String, Fisher> fishersByBoatId) {
        final Map<String, Map<SeaTile, Double>> deploymentValuesPerBoatId =
            parseAllRecords(DEPLOYMENT_VALUES_FILE).stream()
                .filter(record -> record.getInt("year") == targetYear)
                .map(record -> Triple.of( // oh, how I long for case classes...
                    record.getString("boat_id"),
                    nauticalMap.getSeaTile(new Coordinate(record.getDouble("lon"), record.getDouble("lat"))),
                    record.getDouble("value")
                ))
                .filter(triple -> triple.getMiddle() != null) // make sure the entry falls on a sea tile
                .collect(groupingBy(Triple::getLeft, toMap(Triple::getMiddle, Triple::getRight)));

        final Map<SeaTile, Double> defaultDeploymentValues =
            deploymentValuesPerBoatId.values().stream()
                .flatMap(map -> map.keySet().stream())
                .distinct()
                .collect(toMap(identity(), x -> 0.0));

        fishersByBoatId.forEach((boatId, fisher) -> {
            if (fisher.getDestinationStrategy() instanceof FadDestinationStrategy) {
                final Map<SeaTile, Double> deploymentValues =
                    deploymentValuesPerBoatId.getOrDefault(boatId, defaultDeploymentValues);
                ((FadDestinationStrategy) fisher.getDestinationStrategy())
                    .getFadDeploymentDestinationStrategy()
                    .setDeploymentLocationValues(deploymentValues);
            }
        });
    }

    public int getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(int targetYear) {
        this.targetYear = targetYear;
    }

    public static class TunaSpeciesBiomassInitializerFactory
        implements AlgorithmFactory<MultipleIndependentSpeciesBiomassInitializer> {

        private Map<String, SingleSpeciesBiomassNormalizedFactory> biomassInitializers =
            parseAllRecords(SCHAEFER_PARAMS_FILE).stream().collect(toMap(
                r -> r.getString("species_code"),
                r -> makeBiomassInitializerFactory(
                    r.getString("species_code"),
                    r.getDouble("logistic_growth_rate"), // logistic growth rate (r)
                    getQuantity(r.getDouble("carrying_capacity_in_tonnes"), TONNE), // total carrying capacity (K)
                    getQuantity(r.getDouble("total_biomass_in_tonnes"), TONNE) // total biomass
                )));

        private SingleSpeciesBiomassNormalizedFactory bigeyeBiomassInitializer = biomassInitializers.get("BET");
        private SingleSpeciesBiomassNormalizedFactory yellowfinBiomassInitializer = biomassInitializers.get("YFT");
        private SingleSpeciesBiomassNormalizedFactory skipjackBiomassInitializer = biomassInitializers.get("SKJ");

        private SingleSpeciesBiomassNormalizedFactory makeBiomassInitializerFactory(
            String speciesCode,
            double logisticGrowthRate,
            Quantity<Mass> totalCarryingCapacity,
            Quantity<Mass> totalBiomass
        ) {
            final SingleSpeciesBiomassNormalizedFactory factory = new SingleSpeciesBiomassNormalizedFactory();
            factory.setSpeciesName(speciesNames.get(speciesCode));
            factory.setGrower(new FadAwareCommonLogisticGrowerInitializerFactory(logisticGrowthRate));
            factory.setCarryingCapacity(new FixedDoubleParameter(asDouble(totalCarryingCapacity, KILOGRAM)));
            factory.setBiomassSuppliedPerCell(false);

            final double biomassRatio = totalBiomass.divide(totalCarryingCapacity).getValue().doubleValue();
            factory.setInitialBiomassAllocator(new ConstantAllocatorFactory(biomassRatio));

            final FileBiomassAllocatorFactory initialCapacityAllocator =
                speciesCode.equals("BET") ? new CoordinateFileAllocatorFactory() : new SmootherFileAllocatorFactory();
            initialCapacityAllocator.setBiomassPath(biomassFiles.get(speciesCode));
            initialCapacityAllocator.setInputFileHasHeader(true);
            final PolygonAllocatorFactory polygonAllocatorFactory = new PolygonAllocatorFactory();
            polygonAllocatorFactory.setShapeFile(IATTC_SHAPE_FILE);
            polygonAllocatorFactory.setDelegate(initialCapacityAllocator);
            factory.setInitialCapacityAllocator(polygonAllocatorFactory);

            return factory;
        }

        @SuppressWarnings("unused")
        public SingleSpeciesBiomassNormalizedFactory getBigeyeBiomassInitializer() {
            return bigeyeBiomassInitializer;
        }

        @SuppressWarnings("unused")
        public SingleSpeciesBiomassNormalizedFactory getYellowfinBiomassInitializer() {
            return yellowfinBiomassInitializer;
        }

        @SuppressWarnings("unused")
        public SingleSpeciesBiomassNormalizedFactory getSkipjackBiomassInitializer() {
            return skipjackBiomassInitializer;
        }

        @Override
        public MultipleIndependentSpeciesBiomassInitializer apply(FishState fishState) {
            final List<SingleSpeciesBiomassInitializer> biomassInitializers = Stream.of(
                bigeyeBiomassInitializer,
                yellowfinBiomassInitializer,
                skipjackBiomassInitializer
            ).map(factory -> factory.apply(fishState)).collect(toList());
            return new MultipleIndependentSpeciesBiomassInitializer(
                biomassInitializers, false, false
            );
        }
    }
}

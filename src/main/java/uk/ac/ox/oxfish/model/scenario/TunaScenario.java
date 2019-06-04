package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import org.apache.sis.measure.Quantities;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.growers.FadAwareCommonLogisticGrowerInitializerFactory;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.CoordinateFileAllocatorFactory;
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
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomPlanFadDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FollowPlanFadFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.fads.IATTC;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.sis.measure.Units.CUBIC_METRE;
import static org.apache.sis.measure.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;
import static uk.ac.ox.oxfish.utility.Measures.TONNE;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class TunaScenario implements Scenario {

    public static final Path INPUT_DIRECTORY = Paths.get("inputs", "tuna");
    public static final Path MAP_FILE = INPUT_DIRECTORY.resolve("depth.csv");
    private static final Path PORTS_FILE = INPUT_DIRECTORY.resolve("ports.csv");
    private static final Path BOATS_FILE = INPUT_DIRECTORY.resolve("boats.csv");
    private static final Path CURRENTS_FILE = INPUT_DIRECTORY.resolve("currents.csv");
    private static final Path HABITABILITY_BET_FILE = INPUT_DIRECTORY.resolve("habitability_bet_2006-01-07.csv");

    private final FromSimpleFilePortInitializer portInitializer = new FromSimpleFilePortInitializer(PORTS_FILE);
    private FromFileMapInitializerFactory mapInitializer =
        new FromFileMapInitializerFactory(MAP_FILE, 120);
    private FadInitializerFactory fadInitializer = new FadInitializerFactory();
    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializers = new TunaSpeciesBiomassInitializerFactory();
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer = new ConstantWeatherFactory();
    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private FisherDefinition fisherDefinition = new FisherDefinition();

    TunaScenario() {
        fisherDefinition.setGear(new PurseSeineGearFactory());
        fisherDefinition.setFishingStrategy(new FollowPlanFadFishingStrategyFactory());
        fisherDefinition.setDestinationStrategy(new RandomPlanFadDestinationStrategyFactory());
    }

    @SuppressWarnings("unused")
    public FadInitializerFactory getFadInitializer() { return fadInitializer; }

    @SuppressWarnings("unused")
    public void setFadInitializer(FadInitializerFactory fadInitializer) { this.fadInitializer = fadInitializer; }

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

    public AlgorithmFactory<? extends Market> getMarket() {
        return market;
    }

    public void setMarket(AlgorithmFactory<? extends Market> market) {
        this.market = market;
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

        final MarketMap marketMap = new MarketMap(globalBiology);
        globalBiology.getSpecies().forEach(species ->
            marketMap.addMarket(species, market.apply(model))
        );

        final Double gasPrice = gasPricePerLiter.apply(model.random);
        final FixedGasPrice fixedGasPrice = new FixedGasPrice(gasPrice);

        portInitializer
            .buildPorts(nauticalMap, model.random, seaTile -> marketMap, model, fixedGasPrice)
            .forEach(port -> port.setGasPricePerLiter(gasPrice));

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    @Override
    public ScenarioPopulation populateModel(FishState model) {

        FadMapFactory fadMapFactory = new FadMapFactory(CURRENTS_FILE, fadInitializer.apply(model));
        final FadMap fadMap = fadMapFactory.apply(model);
        model.setFadMap(fadMap);
        model.registerStartable(fadMap);

        final LinkedList<Port> ports = model.getMap().getPorts();

        FisherFactory fisherFactory = fisherDefinition.getFisherFactory(model, ports, 0);
        fisherFactory.getAdditionalSetups().add(fisher ->
            ((PurseSeineGear) fisher.getGear()).getFadManager().setFisher(fisher)
        );

        final Map<String, Port> portsByName = ports.stream().collect(toMap(Port::getName, identity()));

        final Supplier<Engine> engineSupplier = fisherDefinition.makeEngineSupplier(model.random);
        final Supplier<FuelTank> fuelTankSupplier = () -> new FuelTank(Double.POSITIVE_INFINITY);

        final List<Fisher> fishers =
            parseAllRecords(BOATS_FILE).stream().map(record -> {
                final String portName = record.getString("port_name");
                final Double length = record.getDouble("length_in_m");
                final Double beam = record.getDouble("beam_in_m");
                final Mass carryingCapacity = Quantities.create(record.getDouble("carrying_capacity_in_t"), TONNE);
                final Volume holdVolume = Quantities.create(record.getDouble("hold_volume_in_m3"), CUBIC_METRE);
                fisherFactory.setPortSupplier(() -> portsByName.get(portName));
                fisherFactory.setBoatSupplier(() -> new Boat(length, beam, engineSupplier.get(), fuelTankSupplier.get()));
                fisherFactory.setHoldSupplier(() -> new Hold(asDouble(carryingCapacity, KILOGRAM), holdVolume, model.getBiology()));
                return fisherFactory.buildFisher(model);
            }).collect(toList());

        // Mutate the fisher factory back into a random boat generator
        // TODO: we don't have boat entry in the tuna model for now, but when we do, this shouldn't be entirely random
        fisherFactory.setBoatSupplier(fisherDefinition.makeBoatSupplier(model.random));
        fisherFactory.setHoldSupplier(fisherDefinition.makeHoldSupplier(model.random, model.getBiology()));
        fisherFactory.setPortSupplier(() ->
            oneOf(ports, model.random).orElseThrow(() -> new RuntimeException("No ports!"))
        );

        final Map<String, FisherFactory> fisherFactories =
            ImmutableMap.of(FishState.DEFAULT_POPULATION_NAME, fisherFactory);

        final SocialNetwork network = new SocialNetwork(new EmptyNetworkBuilder());

        return new ScenarioPopulation(fishers, network, fisherFactories);

    }

    public static class TunaSpeciesBiomassInitializerFactory
        implements AlgorithmFactory<MultipleIndependentSpeciesBiomassInitializer> {
        // Current parameter source is: `POSEIDON Tuna Team Folder/Surplus production model/Total_OCIATTC_PT_results_n=2.csv`
        private SingleSpeciesBiomassNormalizedFactory bigeyeBiomassInitializer = makeBiomassInitializerFactory(
            "Bigeye",
            0.519167947920457, // logistic growth rate (r) (TODO: confirm number)
            Quantities.create(694817, TONNE), // total carrying capacity (K) (TODO: confirm number)
            Quantities.create(400000, TONNE), // total biomass (TODO: get real number instead of eyeballing from the plot)
            HABITABILITY_BET_FILE
        );
        // TODO: private SingleSpeciesBiomassNormalizedFactory yellowfinBiomassInitializer = makeBiomassInitializerFactory("Yellowfin", ...);
        // TODO: private SingleSpeciesBiomassNormalizedFactory skipjackBiomassInitializer = makeBiomassInitializerFactory("Skipjack", ...);

        private SingleSpeciesBiomassNormalizedFactory makeBiomassInitializerFactory(
            String speciesName,
            double logisticGrowthRate,
            Mass totalCarryingCapacity,
            Mass totalBiomass,
            Path initialCapacityFile
        ) {
            final SingleSpeciesBiomassNormalizedFactory factory = new SingleSpeciesBiomassNormalizedFactory();
            factory.setSpeciesName(speciesName);
            factory.setGrower(new FadAwareCommonLogisticGrowerInitializerFactory(logisticGrowthRate));
            factory.setCarryingCapacity(new FixedDoubleParameter(asDouble(totalCarryingCapacity, KILOGRAM)));
            factory.setBiomassSuppliedPerCell(false);

            final double biomassRatio = totalBiomass.divide(totalCarryingCapacity).getValue().doubleValue();
            factory.setInitialBiomassAllocator(new ConstantAllocatorFactory(biomassRatio));

            final CoordinateFileAllocatorFactory initialCapacityAllocator = new CoordinateFileAllocatorFactory();
            initialCapacityAllocator.setBiomassPath(initialCapacityFile);
            initialCapacityAllocator.setInputFileHasHeader(true);
            factory.setInitialCapacityAllocator(initialCapacityAllocator);

            return factory;
        }

        @SuppressWarnings("unused")
        public SingleSpeciesBiomassNormalizedFactory getBigeyeBiomassInitializer() {
            return bigeyeBiomassInitializer;
        }

        @Override
        public MultipleIndependentSpeciesBiomassInitializer apply(FishState fishState) {
            final List<SingleSpeciesBiomassInitializer> biomassInitializers =
                Stream.of(bigeyeBiomassInitializer) // TODO: yellowfinBiomassInitializer, skipjackBiomassInitializer
                    .map(factory -> factory.apply(fishState)).collect(toList());
            return new MultipleIndependentSpeciesBiomassInitializer(
                biomassInitializers, false, false
            );
        }
    }
}

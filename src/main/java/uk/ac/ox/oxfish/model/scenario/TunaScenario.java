package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.CoordinateFileAllocatorFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.PolygonAllocatorFactory;
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
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class TunaScenario implements Scenario {

    public static final Path INPUT_DIRECTORY = Paths.get("inputs", "tuna");
    public static final Path MAP_FILE = INPUT_DIRECTORY.resolve("depth.csv");
    public static final Path PORTS_FILE = INPUT_DIRECTORY.resolve("ports.csv");
    public static final Path BOATS_FILE = INPUT_DIRECTORY.resolve("boats.csv");
    public static final Path CURRENTS_FILE = INPUT_DIRECTORY.resolve("currents.csv");

    private final FromSimpleFilePortInitializer portInitializer = new FromSimpleFilePortInitializer(PORTS_FILE);
    private FromFileMapInitializerFactory mapInitializer =
        new FromFileMapInitializerFactory(MAP_FILE, 120);
    private FadMapFactory fadMap = new FadMapFactory(CURRENTS_FILE);
    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer = new DiffusingLogisticFactory();
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer = new ConstantWeatherFactory();
    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private FisherDefinition fisherDefinition = new FisherDefinition();

    public TunaScenario() {
        fisherDefinition.setGear(new PurseSeineGearFactory());
        fisherDefinition.setFishingStrategy(new FollowPlanFadFishingStrategyFactory());
        fisherDefinition.setDestinationStrategy(new RandomPlanFadDestinationStrategyFactory());
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

    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(
        AlgorithmFactory<? extends BiologyInitializer> biologyInitializer
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

        final FadMap fadMap = this.fadMap.apply(model);
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
                fisherFactory.setPortSupplier(() -> portsByName.get(record.getString("port_name")));
                fisherFactory.setBoatSupplier(() -> new Boat(
                    record.getDouble("length_in_m"),
                    record.getDouble("beam_in_m"),
                    engineSupplier.get(),
                    fuelTankSupplier.get()
                ));
                fisherFactory.setHoldSupplier(() -> new Hold(
                    record.getDouble("carrying_capacity_in_t") * 1000,
                    record.getInt("hold_volume_in_m3"),
                    model.getBiology()
                ));
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

}

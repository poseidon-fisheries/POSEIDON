package uk.ac.ox.oxfish.model.scenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.Gears;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.DestinationStrategies;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomPlanFadDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishingStrategies;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FollowPlanFadFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.RandomFadFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectMover;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectMoverFactory;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.FromFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class TunaScenario implements Scenario {

    public static final Path INPUT_DIRECTORY = Paths.get("inputs", "tuna");
    public static final Path MAP_FILE = INPUT_DIRECTORY.resolve("depth.csv");
    public static final Path PORTS_FILE = INPUT_DIRECTORY.resolve("ports.csv");
    public static final Path CURRENTS_FILE = INPUT_DIRECTORY.resolve("currents.csv");

    private final FromFilePortInitializer portInitializer = new FromFilePortInitializer(PORTS_FILE);
    private FromFileMapInitializerFactory mapInitializer =
        new FromFileMapInitializerFactory(MAP_FILE, 120);
    private DriftingObjectMoverFactory driftingObjectMover =
        new DriftingObjectMoverFactory(CURRENTS_FILE);
    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
        new DiffusingLogisticFactory();
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
        new ConstantWeatherFactory();
    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private int numberOfFishersPerPort = 300;
    private FisherDefinition fisherDefinition = new FisherDefinition();

    public TunaScenario() {

        fisherDefinition.setGear(new PurseSeineGearFactory());
        fisherDefinition.setFishingStrategy(new FollowPlanFadFishingStrategyFactory());
        fisherDefinition.setDestinationStrategy(new RandomPlanFadDestinationStrategyFactory());

    }

    public Path getPortFilePath() {
        return portInitializer.getFilePath();
    }

    public void setPortFilePath(Path filePath) {
        portInitializer.setFilePath(filePath);
    }

    public FromFileMapInitializerFactory getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
        FromFileMapInitializerFactory mapInitializer) {
        this.mapInitializer = mapInitializer;
    }

    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(
        AlgorithmFactory<? extends BiologyInitializer> biologyInitializer
    ) {
        this.biologyInitializer = biologyInitializer;
    }

    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

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

    public int getNumberOfFishersPerPort() {
        return numberOfFishersPerPort;
    }

    public void setNumberOfFishersPerPort(int numberOfFishersPerPort) {
        this.numberOfFishersPerPort = numberOfFishersPerPort;
    }

    public FisherDefinition getFisherDefinition() {
        return fisherDefinition;
    }

    public void setFisherDefinition(FisherDefinition fisherDefinition) {
        this.fisherDefinition = fisherDefinition;
    }

    @Override
    public ScenarioEssentials start(FishState model) {

        final BiologyInitializer biologyInitializer = this.biologyInitializer.apply(model);
        final GlobalBiology globalBiology = biologyInitializer.generateGlobal(model.random, model);
        final WeatherInitializer weatherInitializer = this.weatherInitializer.apply(model);
        final NauticalMap map = mapInitializer.apply(model)
            .makeMap(model.random, globalBiology, model);

        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(
            map, model.random, biologyInitializer, weatherInitializer, globalBiology, model
        );

        final MarketMap marketMap = new MarketMap(globalBiology);
        globalBiology.getSpecies().forEach(species ->
            marketMap.addMarket(species, market.apply(model))
        );

        final FixedGasPrice fixedGasPrice = new FixedGasPrice(gasPricePerLiter.apply(model.random));
        final LinkedHashMap<String, Integer> initialFishersPerPort = fisherDefinition
            .getInitialFishersPerPort();
        portInitializer
            .buildPorts(map, model.random, seaTile -> marketMap, model, fixedGasPrice)
            .forEach(port -> {
                port.setGasPricePerLiter(gasPricePerLiter.apply(model.random));
                initialFishersPerPort.put(port.getName(), portInitializer.getFishersPerPort(port));
            });

        return new ScenarioEssentials(globalBiology, map);
    }

    @Override
    public ScenarioPopulation populateModel(FishState model) {

        // TODO: turn this into a FadFactory class
        final Function<FadManager, Fad> fadFactory = (owner) -> {
            final VariableBiomassBasedBiology fadBiology =
                new BiomassLocalBiology(1.0, model.getBiology().getSize(), model.random);
            return new Fad(owner, fadBiology, 0.01);
        };

        final DriftingObjectMover driftingObjectMover = this.driftingObjectMover.apply(model);
        final FadMap fadMap =
            new FadMap(model.getMap(), model.getBiology(), driftingObjectMover, fadFactory);
        model.setFadMap(fadMap);
        model.registerStartable(fadMap);

        final LinkedList<Port> ports = model.getMap().getPorts();

        Pair<FisherFactory, List<Fisher>> generated =
            fisherDefinition.instantiateFishers(model, ports, 0);

        final FisherFactory fisherFactory = generated.getFirst();
        final List<Fisher> fishers = generated.getSecond();

        Consumer<Fisher> assignFisherAsFadOwner = fisher ->
            ((PurseSeineGear) fisher.getGear()).getFadManager().setFisher(fisher);

        fishers.forEach(assignFisherAsFadOwner);
        fisherFactory.getAdditionalSetups().add(assignFisherAsFadOwner);
        final Map<String, FisherFactory> fisherFactories = new HashMap<>();
        fisherFactories.put(FishState.DEFAULT_POPULATION_NAME, fisherFactory);

        final SocialNetwork network = new SocialNetwork(new EmptyNetworkBuilder());

        return new ScenarioPopulation(fishers, network, fisherFactories);

    }

    public DriftingObjectMoverFactory getDriftingObjectMover() {
        return driftingObjectMover;
    }
}

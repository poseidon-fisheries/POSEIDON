package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.RememberedProfitsExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SNALSARutilities;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializer;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.GeneralizedCognitiveStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.FixedGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.RandomPortInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.model.restrictions.ReputationalRestrictions;
import uk.ac.ox.oxfish.model.restrictions.factory.OneReligiousHolidayFactory;
import uk.ac.ox.oxfish.model.restrictions.factory.RandomTerritoryFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.FixedMap;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class GeneralizedScenario implements Scenario {

    /**
     * number of ports
     */
    private int ports = 1;


    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
        new DiffusingLogisticFactory(1000);

    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
        new ConstantWeatherFactory();


    private AlgorithmFactory<? extends MapInitializer> mapInitializer =
        new SimpleMapInitializerFactory(50, 50, 0, 1000000, 10);


    /**
     * the number of fishers
     */
    private int fishers = 100;


    /**
     * are agents allowed to cheat?
     */
    private boolean cheaters = true;


    /**
     * when this flag is true, agents use their memory to predict future catches and profits. It is necessary
     * for ITQs to work
     */
    private boolean usePredictors = false;


    /**
     * the X position of the port on the grid. If null or a negative number the position is randomized
     */
    private Integer portPositionX = 40;
    /**
     * the Y position of the port on the grid. If null or a negative number the position is randomized
     */
    private Integer portPositionY = 25;
    /**
     * boat speed
     */
    private DoubleParameter speedInKmh = new FixedDoubleParameter(5);
    /**
     * hold size
     */
    private DoubleParameter holdSize = new FixedDoubleParameter(500);
    /**
     * efficiency
     */
    private AlgorithmFactory<? extends Gear> gear = new RandomCatchabilityTrawlFactory();
    private DoubleParameter enginePower = new NormalDoubleParameter(5000, 100);
    private DoubleParameter fuelTankSize = new FixedDoubleParameter(100000);
    private DoubleParameter literPerKilometer = new FixedDoubleParameter(10);
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy =
        new MaximumStepsFactory();
    private AlgorithmFactory<? extends GearStrategy> gearStrategy =
        new FixedGearStrategyFactory();
    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy = new NoDiscardingFactory();
    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy =
        new IgnoreWeatherFactory();
    private AlgorithmFactory<? extends Regulation> regulation = new ProtectedAreasOnlyFactory();
    private NetworkBuilder networkBuilder =
        new EquidegreeBuilder();
    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();
    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();
    private List<StartingMPA> startingMPAs = new LinkedList<>();
    /**
     * if this is not NaN then it is used as the random seed to feed into the map-making function. This allows for randomness
     * in the biology/fishery
     */
    private Long mapMakerDedicatedRandomSeed = null;
    private AlgorithmFactory<? extends LogbookInitializer> logbook =
        new NoLogbookFactory();
    private List<AlgorithmFactory<? extends AdditionalStartable>> plugins =
        new LinkedList<>();
    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy =
        new FixedRestTimeDepartingFactory();
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy =
        new GeneralizedCognitiveStrategyFactory();
    private AlgorithmFactory<? extends ReputationalRestrictions> reputationalRestriction = new RandomTerritoryFactory();
    private AlgorithmFactory<? extends RegionalRestrictions> communalRestriction = new OneReligiousHolidayFactory();

    {
        //best first: startingMPAs.add(new StartingMPA(5,33,35,18));
        //best third:
        startingMPAs.add(new StartingMPA(10, 10, 20, 30));
    }

    {
//        plugins.add(
//                new BiomassResetterFactory()
//        );
    }

    public GeneralizedScenario() {
    }

    /**
     * to use if you really want to port to be somewhere specific
     */
    public void forcePortPosition(final int[] forcedPortPosition) {
        portPositionX = forcedPortPosition[0];
        portPositionY = forcedPortPosition[1];
    }

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy
    ) {
        this.destinationStrategy = destinationStrategy;
    }

    public AlgorithmFactory<? extends ReputationalRestrictions> getReputationalRestriction() {
        return reputationalRestriction;
    }

    public void setReputationalRestriction(final AlgorithmFactory<? extends ReputationalRestrictions> restriction) {
        this.reputationalRestriction = restriction;
    }

    public AlgorithmFactory<? extends RegionalRestrictions> getCommunalRestriction() {
        return communalRestriction;
    }

    public void setCommunalRestriction(final AlgorithmFactory<? extends RegionalRestrictions> restriction) {
        this.communalRestriction = restriction;
    }

    @Override
    public ScenarioEssentials start(final FishState model) {
        final MersenneTwisterFast random = model.random;

        MersenneTwisterFast mapMakerRandom = model.random;
        if (mapMakerDedicatedRandomSeed != null)
            mapMakerRandom = new MersenneTwisterFast(mapMakerDedicatedRandomSeed);
        //force the mapMakerRandom as the new random until the start is completed.
        model.random = mapMakerRandom;


        final BiologyInitializer biology = biologyInitializer.apply(model);
        final WeatherInitializer weather = weatherInitializer.apply(model);

        //create global biology
        final GlobalBiology global = biology.generateGlobal(mapMakerRandom, model);


        final MapInitializer mapMaker = mapInitializer.apply(model);
        final NauticalMap map = mapMaker.makeMap(mapMakerRandom, global, model);

        //set habitats
        final HabitatInitializer habitat = habitatInitializer.apply(model);
        habitat.applyHabitats(map, mapMakerRandom, model);


        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(map, mapMakerRandom, biology,
            weather,
            global, model
        );


        //create fixed price market
        final MarketMap marketMap = new MarketMap(global);
        /*
      market prices for each species
     */


        for (final Species species : global.getSpecies())
            marketMap.addMarket(species, market.apply(model));

        //create random ports, all sharing the same market
        if (portPositionX == null || portPositionX < 0)
            RandomPortInitializer.addRandomPortsToMap(map, ports, seaTile -> marketMap, mapMakerRandom,
                new FixedGasPrice(
                    gasPricePerLiter.applyAsDouble(mapMakerRandom)),
                model
            );
        else {
            final Port port = new Port("Port 0", map.getSeaTile(portPositionX, portPositionY),
                marketMap, 0
            );
            map.addPort(port);
        }

        //create initial mpas
        if (startingMPAs != null)
            for (final StartingMPA mpa : startingMPAs) {
                Logger.getGlobal().info("building MPA at " + mpa.getTopLeftX() + ", " + mpa.getTopLeftY());
                mpa.buildMPA(map);
            }

        //todo make sure the mapmaker randomizer is dead everywhere

        //substitute back the original randomizer
        model.random = random;

        return new ScenarioEssentials(global, map);
    }

    @Override
    public ScenarioPopulation populateModel(final FishState model) {
        final LinkedList<Fisher> fisherList = new LinkedList<>();
        final NauticalMap map = model.getMap();
        final GlobalBiology biology = model.getBiology();
        final MersenneTwisterFast random = model.random;


        final Port[] ports = map.getPorts().toArray(new Port[map.getPorts().size()]);
        for (final Port port : ports)
            port.setGasPricePerLiter(gasPricePerLiter.applyAsDouble(random));

        //create logbook initializer
        final LogbookInitializer log = logbook.apply(model);
        log.start(model);


        //adds predictors to the fisher if the usepredictors flag is up.
        //without predictors agents do not participate in ITQs
        final Consumer<Fisher> predictorSetup = FishStateUtilities.predictorSetup(usePredictors, biology);

        //create the fisher factory object, it will be used by the fishstate object to create and kill fishers
        //while the model is running
        final FisherFactory fisherFactory = new FisherFactory(
            () -> ports[random.nextInt(ports.length)],
            regulation,
            reputationalRestriction,
            communalRestriction,
            departingStrategy,
            destinationStrategy,
            fishingStrategy,
            discardingStrategy,
            gearStrategy,
            weatherStrategy,
            () -> new Boat(10, 10, new Engine(
                enginePower.applyAsDouble(random),
                literPerKilometer.applyAsDouble(random),
                speedInKmh.applyAsDouble(random)
            ),
                new FuelTank(fuelTankSize.applyAsDouble(random))
            ),
            () -> new Hold(holdSize.applyAsDouble(random), biology),
            gear,

            0
        );
        //add predictor setup to the factory
        fisherFactory.getAdditionalSetups().add(predictorSetup);
        fisherFactory.getAdditionalSetups().add(fisher -> log.add(fisher, model));

        //add snalsar info which should be moved elsewhere at some point
        fisherFactory.getAdditionalSetups().add(fisher -> {
            fisher.setCheater(cheaters);
            //todo move this somewhere else
            fisher.addFeatureExtractor(
                SNALSARutilities.PROFIT_FEATURE,
                new RememberedProfitsExtractor(true)
            );
            fisher.addFeatureExtractor(
                FeatureExtractor.AVERAGE_PROFIT_FEATURE,
                (toRepresent, model1, fisher1) -> {
                    final double averageProfits = model1.getLatestDailyObservation(
                        FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                    return new FixedMap<>(
                        averageProfits,
                        toRepresent
                    );
                }
            );
        });


        //call the factory to keep creating fishers
        for (int i = 0; i < fishers; i++) {
            final Fisher newFisher = fisherFactory.buildFisher(model);
            fisherList.add(newFisher);
        }

        assert fisherList.size() == fishers;
        assert fisherFactory.getNextID() == fishers;


        //start additional elements
        for (final AlgorithmFactory<? extends AdditionalStartable> additionalElement : plugins) {
            model.registerStartable(
                additionalElement.apply(model)
            );

        }

        final HashMap<String, FisherFactory> factory = new HashMap<>();
        factory.put(
            FishState.DEFAULT_POPULATION_NAME,
            fisherFactory
        );

        if (fisherList.size() <= 1)
            return new ScenarioPopulation(fisherList, new SocialNetwork(new EmptyNetworkBuilder()),

                factory
            );
        else {
            return new ScenarioPopulation(fisherList, new SocialNetwork(networkBuilder), factory);
        }
    }

    public int getPorts() {
        return ports;
    }

    public void setPorts(final int ports) {
        this.ports = ports;
    }

    public DoubleParameter getSpeedInKmh() {
        return speedInKmh;
    }

    public void setSpeedInKmh(final DoubleParameter speedInKmh) {
        this.speedInKmh = speedInKmh;
    }


    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
        final AlgorithmFactory<? extends Regulation> regulation
    ) {
        this.regulation = regulation;
    }


    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    public void setDepartingStrategy(
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategy
    ) {
        this.departingStrategy = departingStrategy;
    }

    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategy
    ) {
        this.fishingStrategy = fishingStrategy;
    }


    public DoubleParameter getHoldSize() {
        return holdSize;
    }

    public void setHoldSize(final DoubleParameter holdSize) {
        this.holdSize = holdSize;
    }

    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(
        final AlgorithmFactory<? extends BiologyInitializer> biologyInitializer
    ) {
        this.biologyInitializer = biologyInitializer;
    }


    public NetworkBuilder getNetworkBuilder() {
        return networkBuilder;
    }

    public void setNetworkBuilder(
        final NetworkBuilder networkBuilder
    ) {
        this.networkBuilder = networkBuilder;
    }

    public DoubleParameter getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(final DoubleParameter enginePower) {
        this.enginePower = enginePower;
    }

    public DoubleParameter getFuelTankSize() {
        return fuelTankSize;
    }

    public void setFuelTankSize(final DoubleParameter fuelTankSize) {
        this.fuelTankSize = fuelTankSize;
    }

    public DoubleParameter getLiterPerKilometer() {
        return literPerKilometer;
    }

    public void setLiterPerKilometer(final DoubleParameter literPerKilometer) {
        this.literPerKilometer = literPerKilometer;
    }


    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }

    public void setGear(
        final AlgorithmFactory<? extends Gear> gear
    ) {
        this.gear = gear;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(final DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }


    public AlgorithmFactory<? extends Market> getMarket() {
        return market;
    }

    public void setMarket(final AlgorithmFactory<? extends Market> market) {
        this.market = market;
    }

    public boolean isUsePredictors() {
        return usePredictors;
    }

    public void setUsePredictors(final boolean usePredictors) {
        this.usePredictors = usePredictors;
    }

    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    public void setWeatherInitializer(
        final AlgorithmFactory<? extends WeatherInitializer> weatherInitializer
    ) {
        this.weatherInitializer = weatherInitializer;
    }

    public int getFishers() {
        return fishers;
    }

    public void setFishers(final int fishers) {
        this.fishers = fishers;
    }

    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getWeatherStrategy() {
        return weatherStrategy;
    }

    public void setWeatherStrategy(
        final AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy
    ) {
        this.weatherStrategy = weatherStrategy;
    }

    public Long getMapMakerDedicatedRandomSeed() {
        return mapMakerDedicatedRandomSeed;
    }

    public void setMapMakerDedicatedRandomSeed(final Long mapMakerDedicatedRandomSeed) {
        this.mapMakerDedicatedRandomSeed = mapMakerDedicatedRandomSeed;
    }

    public AlgorithmFactory<? extends HabitatInitializer> getHabitatInitializer() {
        return habitatInitializer;
    }

    public void setHabitatInitializer(
        final AlgorithmFactory<? extends HabitatInitializer> habitatInitializer
    ) {
        this.habitatInitializer = habitatInitializer;
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
        final AlgorithmFactory<? extends MapInitializer> mapInitializer
    ) {
        this.mapInitializer = mapInitializer;
    }

    public List<StartingMPA> getStartingMPAs() {
        return startingMPAs;
    }

    public void setStartingMPAs(final List<StartingMPA> startingMPAs) {
        this.startingMPAs = startingMPAs;
    }

    public Integer getPortPositionX() {
        return portPositionX;
    }

    public void setPortPositionX(final Integer portPositionX) {
        this.portPositionX = portPositionX;
    }

    public Integer getPortPositionY() {
        return portPositionY;
    }

    public void setPortPositionY(final Integer portPositionY) {
        this.portPositionY = portPositionY;
    }


    /**
     * Getter for property 'gearStrategy'.
     *
     * @return Value for property 'gearStrategy'.
     */
    public AlgorithmFactory<? extends GearStrategy> getGearStrategy() {
        return gearStrategy;
    }

    /**
     * Setter for property 'gearStrategy'.
     *
     * @param gearStrategy Value to set for property 'gearStrategy'.
     */
    public void setGearStrategy(
        final AlgorithmFactory<? extends GearStrategy> gearStrategy
    ) {
        this.gearStrategy = gearStrategy;
    }

    /**
     * Getter for property 'cheaters'.
     *
     * @return Value for property 'cheaters'.
     */
    public boolean isCheaters() {
        return cheaters;
    }

    /**
     * Setter for property 'cheaters'.
     *
     * @param cheaters Value to set for property 'cheaters'.
     */
    public void setCheaters(final boolean cheaters) {
        this.cheaters = cheaters;
    }

    /**
     * Getter for property 'logbook'.
     *
     * @return Value for property 'logbook'.
     */
    public AlgorithmFactory<? extends LogbookInitializer> getLogbook() {
        return logbook;
    }

    /**
     * Setter for property 'logbook'.
     *
     * @param logbook Value to set for property 'logbook'.
     */
    public void setLogbook(
        final AlgorithmFactory<? extends LogbookInitializer> logbook
    ) {
        this.logbook = logbook;
    }

    /**
     * Getter for property 'discardingStrategy'.
     *
     * @return Value for property 'discardingStrategy'.
     */
    public AlgorithmFactory<? extends DiscardingStrategy> getDiscardingStrategy() {
        return discardingStrategy;
    }

    /**
     * Setter for property 'discardingStrategy'.
     *
     * @param discardingStrategy Value to set for property 'discardingStrategy'.
     */
    public void setDiscardingStrategy(
        final AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy
    ) {
        this.discardingStrategy = discardingStrategy;
    }

    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return plugins;
    }

    public void setPlugins(final List<AlgorithmFactory<? extends AdditionalStartable>> plugins) {
        this.plugins = plugins;
    }

}

/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassResetterFactory;
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
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
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
import uk.ac.ox.oxfish.geography.SeaTile;
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
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.FixedMap;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is the most general conceptual scenario we have. Almost everything is pluggable
 * Created by carrknight on 4/20/15.
 */
public class PrototypeScenario implements Scenario {


    /**
     * number of ports
     */
    private int ports = 1;



    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
            new DiffusingLogisticFactory();

    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
            new ConstantWeatherFactory();


    private AlgorithmFactory<? extends MapInitializer> mapInitializer =
            new SimpleMapInitializerFactory();



    /**
     * the number of fishers
     */
    private int fishers = 100;


    /**
     * are agents allowed to cheat?
     */
    private boolean cheaters = false;



    /**
     * when this flag is true, agents use their memory to predict future catches and profits. It is necessary
     * for ITQs to work
     */
    private boolean usePredictors = false;


    /**
     * the X position of the port on the grid. If null or a negative number the position is randomized
     */
    private Integer portPositionX = -1;
    /**
     * the X position of the port on the grid. If null or a negative number the position is randomized
     */
    private Integer portPositionY = -1;

    /**
     * to use if you really want to port to be somewhere specific
     */
    public void forcePortPosition(int[] forcedPortPosition) {
        portPositionX = forcedPortPosition[0];
        portPositionY = forcedPortPosition[1];
    }

    /**
     * boat speed
     */
    private DoubleParameter speedInKmh = new FixedDoubleParameter(5);

    /**
     * hold size
     */
    private DoubleParameter holdSize = new FixedDoubleParameter(100);

    /**
     * efficiency
     */
    private AlgorithmFactory<? extends Gear> gear = new RandomCatchabilityTrawlFactory();


    private DoubleParameter enginePower = new NormalDoubleParameter(5000, 100);

    private DoubleParameter fuelTankSize = new FixedDoubleParameter(100000);


    private DoubleParameter literPerKilometer = new FixedDoubleParameter(10);


    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy =
            new FixedRestTimeDepartingFactory();

    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy =
            new PerTripImitativeDestinationFactory();
    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy =
            new MaximumStepsFactory();

    private AlgorithmFactory<? extends GearStrategy> gearStrategy =
            new FixedGearStrategyFactory();

    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy = new NoDiscardingFactory();

    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy =
            new IgnoreWeatherFactory();

    private AlgorithmFactory<? extends Regulation> regulation =  new ProtectedAreasOnlyFactory();


    private NetworkBuilder networkBuilder =
            new EquidegreeBuilder();


    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();


    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();


    private List<StartingMPA> startingMPAs  = new LinkedList<>();
    {
        //best first: startingMPAs.add(new StartingMPA(5,33,35,18));
        //best third:
        //startingMPAs.add(new StartingMPA(0,26,34,40));
    }

    /**
     * if this is not NaN then it is used as the random seed to feed into the map-making function. This allows for randomness
     * in the biology/fishery
     */
    private Long mapMakerDedicatedRandomSeed =  null;


    private AlgorithmFactory<? extends LogbookInitializer> logbook =
            new NoLogbookFactory();


    private List<AlgorithmFactory<? extends AdditionalStartable>> plugins =
            new LinkedList<>();
    {
//        plugins.add(
//                new BiomassResetterFactory()
//        );
    }

    public PrototypeScenario() {
    }


    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {

        MersenneTwisterFast random = model.random;

        MersenneTwisterFast mapMakerRandom = model.random;
        if(mapMakerDedicatedRandomSeed != null)
            mapMakerRandom = new MersenneTwisterFast(mapMakerDedicatedRandomSeed);
        //force the mapMakerRandom as the new random until the start is completed.
        model.random = mapMakerRandom;




        BiologyInitializer biology = biologyInitializer.apply(model);
        WeatherInitializer weather = weatherInitializer.apply(model);

        //create global biology
        GlobalBiology global = biology.generateGlobal(mapMakerRandom,model);


        MapInitializer mapMaker = mapInitializer.apply(model);
        NauticalMap map = mapMaker.makeMap(mapMakerRandom,global,model);

        //set habitats
        HabitatInitializer habitat = habitatInitializer.apply(model);
        habitat.applyHabitats(map, mapMakerRandom, model);


        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(map, mapMakerRandom, biology,
                weather,
                global, model);


        //create fixed price market
        MarketMap marketMap = new MarketMap(global);
        /*
      market prices for each species
     */



        for(Species species : global.getSpecies())
            marketMap.addMarket(species, market.apply(model));

        //create random ports, all sharing the same market
        if(portPositionX == null || portPositionX < 0)
            RandomPortInitializer.addRandomPortsToMap(map, ports, seaTile -> marketMap, mapMakerRandom,
                    new FixedGasPrice(
                            gasPricePerLiter.apply(mapMakerRandom)),
                    model);
        else
        {
            Port port = new Port("Port 0", map.getSeaTile(portPositionX, portPositionY),
                    marketMap, 0);
            map.addPort(port);
        }

        //create initial mpas
        if(startingMPAs != null)
            for(StartingMPA mpa : startingMPAs)
            {
                if(Log.INFO)
                    Log.info("building MPA at " + mpa.getTopLeftX() + ", " + mpa.getTopLeftY());
                mpa.buildMPA(map);
            }

        //todo make sure the mapmaker randomizer is dead everywhere

        //substitute back the original randomizer
        model.random = random;




        return new ScenarioEssentials(global,map);
    }


    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {

        LinkedList<Fisher> fisherList = new LinkedList<>();
        final NauticalMap map = model.getMap();
        final GlobalBiology biology = model.getBiology();
        final MersenneTwisterFast random = model.random;



        Port[] ports =map.getPorts().toArray(new Port[map.getPorts().size()]);
        for(Port port : ports)
            port.setGasPricePerLiter(gasPricePerLiter.apply(random));

        //create logbook initializer
        LogbookInitializer log = logbook.apply(model);
        log.start(model);


        //adds predictors to the fisher if the usepredictors flag is up.
        //without predictors agents do not participate in ITQs
        Consumer<Fisher> predictorSetup = FishStateUtilities.predictorSetup(usePredictors, biology);

        //create the fisher factory object, it will be used by the fishstate object to create and kill fishers
        //while the model is running
        FisherFactory fisherFactory = new FisherFactory(
                () -> ports[random.nextInt(ports.length)],
                regulation,
                departingStrategy,
                destinationStrategy,
                fishingStrategy,
                discardingStrategy,
                gearStrategy,
                weatherStrategy,
                (Supplier<Boat>) () -> new Boat(10, 10, new Engine(enginePower.apply(random),
                        literPerKilometer.apply(random),
                        speedInKmh.apply(random)),
                        new FuelTank(fuelTankSize.apply(random))),
                (Supplier<Hold>) () -> new Hold(holdSize.apply(random),biology),
                gear,

                0);
        //add predictor setup to the factory
        fisherFactory.getAdditionalSetups().add(predictorSetup);
        fisherFactory.getAdditionalSetups().add(new Consumer<Fisher>() {
            @Override
            public void accept(Fisher fisher) {
                log.add(fisher,model);
            }
        });

        //add snalsar info which should be moved elsewhere at some point
        fisherFactory.getAdditionalSetups().add(new Consumer<Fisher>() {
            @Override
            public void accept(Fisher fisher) {
                fisher.setCheater(cheaters);
                //todo move this somewhere else
                fisher.addFeatureExtractor(
                        SNALSARutilities.PROFIT_FEATURE,
                        new RememberedProfitsExtractor(true)
                );
                fisher.addFeatureExtractor(
                        FeatureExtractor.AVERAGE_PROFIT_FEATURE,
                        new FeatureExtractor<SeaTile>() {
                            @Override
                            public HashMap<SeaTile, Double> extractFeature(
                                    Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {
                                double averageProfits = model.getLatestDailyObservation(
                                        FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                                return new FixedMap<>(averageProfits,
                                        toRepresent) ;
                            }
                        }
                );
            }
        });



        //call the factory to keep creating fishers
        for(int i=0;i<fishers;i++)
        {
            Fisher newFisher = fisherFactory.buildFisher(model);
            fisherList.add(newFisher);
        }

        assert fisherList.size()==fishers;
        assert fisherFactory.getNextID()==fishers;


        //start additional elements
        for (AlgorithmFactory<? extends AdditionalStartable> additionalElement : plugins) {
            model.registerStartable(
                    additionalElement.apply(model)
            );

        }

        HashMap<String, FisherFactory> factory = new HashMap<>();
        factory.put(FishState.DEFAULT_POPULATION_NAME,
                    fisherFactory);

        if(fisherList.size() <=1)
            return new ScenarioPopulation(fisherList,new SocialNetwork(new EmptyNetworkBuilder()),

                                          factory );
        else {
            return new ScenarioPopulation(fisherList, new SocialNetwork(networkBuilder), factory);
        }
    }




    public int getPorts() {
        return ports;
    }

    public void setPorts(int ports) {
        this.ports = ports;
    }

    public DoubleParameter getSpeedInKmh() {
        return speedInKmh;
    }

    public void setSpeedInKmh(DoubleParameter speedInKmh) {
        this.speedInKmh = speedInKmh;
    }


    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
            AlgorithmFactory<? extends Regulation> regulation) {
        this.regulation = regulation;
    }


    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    public void setDepartingStrategy(
            AlgorithmFactory<? extends DepartingStrategy> departingStrategy) {
        this.departingStrategy = departingStrategy;
    }

    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(
            AlgorithmFactory<? extends FishingStrategy> fishingStrategy) {
        this.fishingStrategy = fishingStrategy;
    }


    public DoubleParameter getHoldSize() {
        return holdSize;
    }

    public void setHoldSize(DoubleParameter holdSize) {
        this.holdSize = holdSize;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(
            AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(
            AlgorithmFactory<? extends BiologyInitializer> biologyInitializer) {
        this.biologyInitializer = biologyInitializer;
    }


    public NetworkBuilder getNetworkBuilder() {
        return networkBuilder;
    }

    public void setNetworkBuilder(
            NetworkBuilder networkBuilder) {
        this.networkBuilder = networkBuilder;
    }

    public DoubleParameter getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(DoubleParameter enginePower) {
        this.enginePower = enginePower;
    }

    public DoubleParameter getFuelTankSize() {
        return fuelTankSize;
    }

    public void setFuelTankSize(DoubleParameter fuelTankSize) {
        this.fuelTankSize = fuelTankSize;
    }

    public DoubleParameter getLiterPerKilometer() {
        return literPerKilometer;
    }

    public void setLiterPerKilometer(DoubleParameter literPerKilometer) {
        this.literPerKilometer = literPerKilometer;
    }


    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }

    public void setGear(
            AlgorithmFactory<? extends Gear> gear) {
        this.gear = gear;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }


    public AlgorithmFactory<? extends Market> getMarket() {
        return market;
    }

    public void setMarket(AlgorithmFactory<? extends Market> market) {
        this.market = market;
    }

    public boolean isUsePredictors() {
        return usePredictors;
    }

    public void setUsePredictors(boolean usePredictors) {
        this.usePredictors = usePredictors;
    }

    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    public void setWeatherInitializer(
            AlgorithmFactory<? extends WeatherInitializer> weatherInitializer) {
        this.weatherInitializer = weatherInitializer;
    }

    public int getFishers() {
        return fishers;
    }

    public void setFishers(int fishers) {
        this.fishers = fishers;
    }

    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getWeatherStrategy() {
        return weatherStrategy;
    }

    public void setWeatherStrategy(
            AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy) {
        this.weatherStrategy = weatherStrategy;
    }

    public Long getMapMakerDedicatedRandomSeed() {
        return mapMakerDedicatedRandomSeed;
    }

    public void setMapMakerDedicatedRandomSeed(Long mapMakerDedicatedRandomSeed) {
        this.mapMakerDedicatedRandomSeed = mapMakerDedicatedRandomSeed;
    }

    public AlgorithmFactory<? extends HabitatInitializer> getHabitatInitializer() {
        return habitatInitializer;
    }

    public void setHabitatInitializer(
            AlgorithmFactory<? extends HabitatInitializer> habitatInitializer) {
        this.habitatInitializer = habitatInitializer;
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
            AlgorithmFactory<? extends MapInitializer> mapInitializer) {
        this.mapInitializer = mapInitializer;
    }

    public List<StartingMPA> getStartingMPAs() {
        return startingMPAs;
    }

    public void setStartingMPAs(List<StartingMPA> startingMPAs) {
        this.startingMPAs = startingMPAs;
    }

    public Integer getPortPositionX() {
        return portPositionX;
    }

    public void setPortPositionX(Integer portPositionX) {
        this.portPositionX = portPositionX;
    }

    public Integer getPortPositionY() {
        return portPositionY;
    }

    public void setPortPositionY(Integer portPositionY) {
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
            AlgorithmFactory<? extends GearStrategy> gearStrategy) {
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
    public void setCheaters(boolean cheaters) {
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
            AlgorithmFactory<? extends LogbookInitializer> logbook) {
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
            AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy) {
        this.discardingStrategy = discardingStrategy;
    }

    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<AlgorithmFactory<? extends AdditionalStartable>> plugins) {
        this.plugins = plugins;
    }
}

package uk.ac.ox.oxfish.model.scenario;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.TimeSeriesWeatherFactory;
import uk.ac.ox.oxfish.fisher.DockingListener;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializer;
import uk.ac.ox.oxfish.fisher.log.initializers.LogisticLogbookFactory;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.HandlineFloridaLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LonglineFloridaLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.BarebonesFloridaDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FloridaLogitDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FloridaLogitReturnFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.FixedGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseBoundedMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.utility.*;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The scenario to lspiRun Osmose WFS.
 * Created by carrknight on 11/17/16.
 */
public class OsmoseWFSScenario implements Scenario{


    public final static Path mainDirectory = Paths.get("temp_wfs");

    private final OsmoseBiologyFactory biologyInitializer = new OsmoseBiologyFactory();

    private LinkedHashMap<Port, Integer> longlinersPerPort;

    private LinkedHashMap<Port, Integer> handlinersPerPort;


    private AlgorithmFactory<? extends LogbookInitializer> longlineLogbook =
            new LogisticLogbookFactory();
    {
        //intercept", "distance", "habit", "fuel_price", "wind_speed"
        CentroidMapFileFactory discretizer = new CentroidMapFileFactory();
        discretizer.setFilePath(Paths.get("temp_wfs", "areas.txt").toString());
        ((LogisticLogbookFactory) longlineLogbook).setDiscretization(discretizer);
        ((LogisticLogbookFactory) longlineLogbook).setIntercept(true);
        ((LogisticLogbookFactory) longlineLogbook).setPortDistance(true);
        ((LogisticLogbookFactory) longlineLogbook).setPeriodHabit(90);
        ((LogisticLogbookFactory) longlineLogbook).setGasPrice(true);
        ((LogisticLogbookFactory) longlineLogbook).setWindSpeed(true);
        ((LogisticLogbookFactory) longlineLogbook).setIdentifier("longline_");


    }

    private AlgorithmFactory<? extends LogbookInitializer> handlineLogbook =
            new NoLogbookFactory();


    {
        biologyInitializer.setIndexOfSpeciesToBeManagedByThisModel("2,3,4");
        biologyInitializer.setOsmoseConfigurationFile(mainDirectory.resolve("wfs").resolve("osm_all-parameters.csv").toAbsolutePath().toString());
        biologyInitializer.setPreInitializedConfiguration(false);
        biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(114*12);
        biologyInitializer.setScalingFactor(new FixedDoubleParameter(1000d));
        //biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(10);
        //this should take care of selectivityi
        biologyInitializer.getRecruitmentAges().put(2,2);
        biologyInitializer.getRecruitmentAges().put(3,2);
        biologyInitializer.getRecruitmentAges().put(4,1);
    }

    private final OsmoseBoundedMapInitializerFactory mapInitializer = new OsmoseBoundedMapInitializerFactory();
    {
        mapInitializer.setLowRightEasting(584600.702);

        mapInitializer.setLowRightNorthing(2791787.489);

        mapInitializer.setUpLeftEasting(-73291.664);

        mapInitializer.setUpLeftNorthing(3445097.299);
    }


    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
            new TimeSeriesWeatherFactory(
                    Paths.get("temp_wfs","steve","Fleet","PriceWSgas.txt").toString(),
                    true,
                    '$',
                    10
            );

    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();


    private AlgorithmFactory<? extends Gear> longlinerGear = new RandomTrawlStringFactory("2:0.01,3:0.01,4:0.01");

    private AlgorithmFactory<? extends Gear> handlinerGear = new RandomTrawlStringFactory("2:0.01,3:0.01,4:0.01");


    //comes as 95th percentile from Steve's data on hold-sizes
    private DoubleParameter longlinerHoldSize = new FixedDoubleParameter(6500); // in kg!

    //comes as 95th percentile from Steve's data on hold-sizes
    private DoubleParameter handlinerHoldSize = new FixedDoubleParameter(2200); // in kg!


    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> handlinerDepartingStrategy =
            new HandlineFloridaLogisticDepartingFactory();


    private AlgorithmFactory<? extends DiscardingStrategy> handlinerDiscardingStrategy = new NoDiscardingFactory();


    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> handlinerDestinationStrategy =
            new BarebonesFloridaDestinationFactory();
    {
        ((BarebonesFloridaDestinationFactory) handlinerDestinationStrategy).setHabitIntercept(
                new FixedDoubleParameter(2.53163185)
        );
        ((BarebonesFloridaDestinationFactory) handlinerDestinationStrategy).setDistanceInKm(
                new FixedDoubleParameter(-0.00759009)
        );

        CentroidMapFileFactory discretizer = new CentroidMapFileFactory();
        discretizer.setFilePath(Paths.get("temp_wfs", "areas.txt").toString());
        discretizer.setxColumnName("eastings");
        discretizer.setyColumnName("northings");
        ((BarebonesFloridaDestinationFactory) handlinerDestinationStrategy).setDiscretizer(discretizer);

    }


    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> handlinerFishingStrategy =
            new FloridaLogitReturnFactory();
    {
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setIntercept(new FixedDoubleParameter(-3.47701));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setPriceRedGrouper(new FixedDoubleParameter(0.92395));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setPriceGagGrouper(new FixedDoubleParameter(-0.65122));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setRatioCatchToFishHold(new FixedDoubleParameter(-4.37828));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setWeekendDummy(new FixedDoubleParameter(-0.24437));


    }

    private AlgorithmFactory<? extends GearStrategy> handlinerGearStrategy =
            new FixedGearStrategyFactory();


    private AlgorithmFactory<? extends WeatherEmergencyStrategy> handlinerWeatherStrategy =
            new IgnoreWeatherFactory();

    private AlgorithmFactory<? extends Regulation> handlinerRegulations =  new ProtectedAreasOnlyFactory();


    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> longlinerDepartingStrategy =
            new LonglineFloridaLogisticDepartingFactory();

    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> longlinerDestinationStrategy =
            new FloridaLogitDestinationFactory();



    private AlgorithmFactory<? extends DiscardingStrategy> longlinerDiscardingStrategy = new NoDiscardingFactory();

    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> longlinerFishingStrategy =
            new FloridaLogitReturnFactory();

    {
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setIntercept(new FixedDoubleParameter(-2.74969d));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setPriceRedGrouper(new FixedDoubleParameter(-0.12476));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setPriceGagGrouper(new FixedDoubleParameter(-0.28232));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setRatioCatchToFishHold(new FixedDoubleParameter(4.53092));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setWeekendDummy(new FixedDoubleParameter(-0.15556));

    }

    private AlgorithmFactory<? extends GearStrategy> longlinerGearStrategy =
            new FixedGearStrategyFactory();


    private AlgorithmFactory<? extends WeatherEmergencyStrategy> longlinerWeatherStrategy =
            new IgnoreWeatherFactory();

    private AlgorithmFactory<? extends Regulation> longlinerRegulations =  new ProtectedAreasOnlyFactory();


    private NetworkBuilder networkBuilder =
            new EquidegreeBuilder();


    private DoubleParameter longlinerTravellingCosts = new FixedDoubleParameter(0);

    private DoubleParameter handlinerTravellingCosts = new FixedDoubleParameter(0);


    private DoubleParameter longlinerSpeedKph =  new FixedDoubleParameter(16.0661); //this is 8.675 knots from the data request

    private DoubleParameter  handlinerSpeedKph =  new FixedDoubleParameter(16.0661); //this is 8.675 knots from the data request


    public static MapDiscretization createDiscretization(FishState state, String centroidFile) {
        CsvColumnsToLists reader = new CsvColumnsToLists(
                centroidFile,
                ',',
                new String[]{"eastings", "northings"}
        );

        LinkedList<Double>[] lists = reader.readColumns();
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < lists[0].size(); i++)
            coordinates.add(new Coordinate(lists[0].get(i),
                                           lists[1].get(i),
                                           0));

        CentroidMapDiscretizer discretizer = new CentroidMapDiscretizer(
                coordinates);
        MapDiscretization discretization = new MapDiscretization(
                discretizer);
        discretization.discretize(state.getMap());
        return discretization;
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
        try {

            BiologyInitializer biology = biologyInitializer.apply(model);
            WeatherInitializer weather = weatherInitializer.apply(model);


            //create global biology
            GlobalBiology global = biology.generateGlobal(model.getRandom(), model);


            MapInitializer mapMaker = mapInitializer.apply(model);
            NauticalMap map = mapMaker.makeMap(model.getRandom(), global, model);

            //set habitats
            HabitatInitializer habitat = habitatInitializer.apply(model);
            habitat.applyHabitats(map, model.getRandom(), model);


            //this next static method calls biology.initialize, weather.initialize and the like
            NauticalMapFactory.initializeMap(map, model.getRandom(), biology,
                                             weather,
                                             global, model);




            PortReader reader = new PortReader();

            longlinersPerPort = reader.readFile(
                    mainDirectory.resolve("longline_ports.csv"),
                    map,
                    () -> {
                        MarketMap markets = new MarketMap(global);
                        for(Species species : global.getSpecies())
                            markets.addMarket(species, new FixedPriceMarket(1));

                        return markets;
                    },
                    0.1234);


            handlinersPerPort = reader.readFile(mainDirectory.resolve("handline_ports.csv"),
                                                map,
                                                () -> {
                                                    MarketMap markets = new MarketMap(global);
                                                    for(Species species : global.getSpecies())
                                                        markets.addMarket(species, new FixedPriceMarket(1));

                                                    return markets;
                                                },
                                                0.1234);

            for(Port port : reader.getPorts())
                map.addPort(port);


            //todo put this somewhere else
            CsvColumnToList gasPrices = new CsvColumnToList(Paths.get("temp_wfs","steve","Fleet","PriceWSgas.txt").toString(),
                                                            true,
                                                            '$',
                                                            13);
            LinkedList<Double> prices = gasPrices.readColumn();
            TimeSeriesActuator gasActuator = TimeSeriesActuator.gasPriceDailySchedule(prices, new ArrayList<>(map.getPorts()));
            gasActuator.step(model);
            model.scheduleEveryDay(gasActuator, StepOrder.POLICY_UPDATE);

            return new ScenarioEssentials(global,map);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read file");
        }
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {

        int fisherCounter=0;
        LinkedList<Fisher> fisherList = new LinkedList<>();


        GlobalBiology biology = model.getBiology();
        MersenneTwisterFast random = model.getRandom();



        Consumer<Fisher> predictorSetup = FishStateUtilities.predictorSetup(true,
                                                                            biology);

        //start the logbooks!
        LogbookInitializer longlineLog = longlineLogbook.apply(model);
        longlineLog.start(model);
        LogbookInitializer handlineLog = handlineLogbook.apply(model);
        handlineLog.start(model);

        //longliners
        for(Map.Entry<Port,Integer> entry : longlinersPerPort.entrySet())

            for(int i=0;i<entry.getValue();i++)
            {
                final double speed = longlinerSpeedKph.apply(random);
                final double engineWeight = 1;
                final double mileage = 0.7518591; //this is gallon per km and it comes from the california estimate of 1.21 gallons a mile
                final double fuelCapacity = 100000000;

                Gear fisherGear = longlinerGear.apply(model);


                Fisher newFisher = new Fisher(fisherCounter, entry.getKey(),
                                              model.getRandom(),
                                              longlinerRegulations.apply(model),
                                              longlinerDepartingStrategy.apply(model),
                                              longlinerDestinationStrategy.apply(model),
                                              longlinerFishingStrategy.apply(model),
                                              longlinerGearStrategy.apply(model),
                                              longlinerDiscardingStrategy.apply(model),
                                              longlinerWeatherStrategy.apply(model),
                                              new Boat(10, 10,
                                                       new Engine(engineWeight,
                                                                  mileage,
                                                                  speed),
                                                       new FuelTank(fuelCapacity)),
                                              new Hold(
                                                      longlinerHoldSize.apply(random),
                                                      biology.getSize()),
                                              fisherGear, model.getSpecies().size());
                newFisher.getTags().add("large");
                newFisher.getTags().add("longline");
                newFisher.getTags().add("ship");
                newFisher.getTags().add("blue");
                fisherCounter++;



                longlineLog.add(newFisher,model);
                predictorSetup.accept(newFisher);


                fisherList.add(newFisher);

                //add other trip costs
                newFisher.addDockingListener(
                        (DockingListener) (fisher, port1) -> {
                            if (fisher.getHoursAtSea() > 0)
                                fisher.spendForTrip(longlinerTravellingCosts.apply(model.getRandom())
                                                            /
                                                            fisher.getHoursAtSea());
                        });
            }

        //handliners
        for(Map.Entry<Port,Integer> entry : handlinersPerPort.entrySet())

            for(int i=0;i<entry.getValue();i++)
            {
                final double speed = handlinerSpeedKph.apply(random);
                final double engineWeight = 1;
                final double mileage = 0.7518591; //this is gallon per km and it comes
                // from the california estimate of 1.21 gallons a mile
                final double fuelCapacity = 100000000;

                Gear fisherGear = handlinerGear.apply(model);


                Fisher newFisher = new Fisher(fisherCounter, entry.getKey(),
                                              model.getRandom(),
                                              handlinerRegulations.apply(model),
                                              handlinerDepartingStrategy.apply(model),
                                              handlinerDestinationStrategy.apply(model),
                                              handlinerFishingStrategy.apply(model),
                                              handlinerGearStrategy.apply(model),
                                              handlinerDiscardingStrategy.apply(model   ),
                                              handlinerWeatherStrategy.apply(model),
                                              new Boat(10, 10,
                                                       new Engine(engineWeight,
                                                                  mileage,
                                                                  speed),
                                                       new FuelTank(fuelCapacity)), new Hold(
                        handlinerHoldSize.apply(random),
                        biology.getSize()),
                                              fisherGear, model.getSpecies().size());
                newFisher.getTags().add("small");
                newFisher.getTags().add("handline");
                newFisher.getTags().add("boat");
                newFisher.getTags().add("red");
                fisherCounter++;



                handlineLog.add(newFisher,model);
                predictorSetup.accept(newFisher);


                fisherList.add(newFisher);

                //add other trip costs
                newFisher.addDockingListener(
                        (DockingListener) (fisher, port1) -> {
                            if (fisher.getHoursAtSea() > 0)
                                fisher.spendForTrip(handlinerTravellingCosts.apply(model.getRandom())
                                                            /
                                                            fisher.getHoursAtSea());
                        });
            }



        //make friends only within the same port
        networkBuilder.addPredicate(new NetworkPredicate() {
            @Override
            public boolean test(Fisher from, Fisher to) {
                return from.getHomePort().equals(to.getHomePort());
            }
        });
        //make friends only between same boats
        networkBuilder.addPredicate(new NetworkPredicate() {
            @Override
            public boolean test(Fisher from, Fisher to) {
                if(from.getTags().contains("large"))
                    return to.getTags().contains("large");
                else
                {
                    assert from.getTags().contains("small");
                    return to.getTags().contains("small");
                }
            }
        });

        return new ScenarioPopulation(
                fisherList,
                new SocialNetwork(networkBuilder),
                null
        );
    }

    /**
     * Getter for property 'biologyInitializer'.
     *
     * @return Value for property 'biologyInitializer'.
     */
    public OsmoseBiologyFactory getBiologyInitializer() {
        return biologyInitializer;
    }

    /**
     * Getter for property 'mapInitializer'.
     *
     * @return Value for property 'mapInitializer'.
     */
    public OsmoseBoundedMapInitializerFactory getMapInitializer() {
        return mapInitializer;
    }


    /**
     * Getter for property 'longlinerHoldSize'.
     *
     * @return Value for property 'longlinerHoldSize'.
     */
    public DoubleParameter getLonglinerHoldSize() {
        return longlinerHoldSize;
    }

    /**
     * Setter for property 'longlinerHoldSize'.
     *
     * @param longlinerHoldSize Value to set for property 'longlinerHoldSize'.
     */
    public void setLonglinerHoldSize(DoubleParameter longlinerHoldSize) {
        this.longlinerHoldSize = longlinerHoldSize;
    }

    /**
     * Getter for property 'longlinerDepartingStrategy'.
     *
     * @return Value for property 'longlinerDepartingStrategy'.
     */
    public AlgorithmFactory<? extends DepartingStrategy> getLonglinerDepartingStrategy() {
        return longlinerDepartingStrategy;
    }

    /**
     * Setter for property 'longlinerDepartingStrategy'.
     *
     * @param longlinerDepartingStrategy Value to set for property 'longlinerDepartingStrategy'.
     */
    public void setLonglinerDepartingStrategy(
            AlgorithmFactory<? extends DepartingStrategy> longlinerDepartingStrategy) {
        this.longlinerDepartingStrategy = longlinerDepartingStrategy;
    }

    /**
     * Getter for property 'longlinerDestinationStrategy'.
     *
     * @return Value for property 'longlinerDestinationStrategy'.
     */
    public AlgorithmFactory<? extends DestinationStrategy> getLonglinerDestinationStrategy() {
        return longlinerDestinationStrategy;
    }

    /**
     * Setter for property 'longlinerDestinationStrategy'.
     *
     * @param longlinerDestinationStrategy Value to set for property 'longlinerDestinationStrategy'.
     */
    public void setLonglinerDestinationStrategy(
            AlgorithmFactory<? extends DestinationStrategy> longlinerDestinationStrategy) {
        this.longlinerDestinationStrategy = longlinerDestinationStrategy;
    }

    /**
     * Getter for property 'longlinerFishingStrategy'.
     *
     * @return Value for property 'longlinerFishingStrategy'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getLonglinerFishingStrategy() {
        return longlinerFishingStrategy;
    }

    /**
     * Setter for property 'longlinerFishingStrategy'.
     *
     * @param longlinerFishingStrategy Value to set for property 'longlinerFishingStrategy'.
     */
    public void setLonglinerFishingStrategy(
            AlgorithmFactory<? extends FishingStrategy> longlinerFishingStrategy) {
        this.longlinerFishingStrategy = longlinerFishingStrategy;
    }

    /**
     * Getter for property 'longlinerGearStrategy'.
     *
     * @return Value for property 'longlinerGearStrategy'.
     */
    public AlgorithmFactory<? extends GearStrategy> getLonglinerGearStrategy() {
        return longlinerGearStrategy;
    }

    /**
     * Setter for property 'longlinerGearStrategy'.
     *
     * @param longlinerGearStrategy Value to set for property 'longlinerGearStrategy'.
     */
    public void setLonglinerGearStrategy(
            AlgorithmFactory<? extends GearStrategy> longlinerGearStrategy) {
        this.longlinerGearStrategy = longlinerGearStrategy;
    }

    /**
     * Getter for property 'longlinerWeatherStrategy'.
     *
     * @return Value for property 'longlinerWeatherStrategy'.
     */
    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getLonglinerWeatherStrategy() {
        return longlinerWeatherStrategy;
    }

    /**
     * Setter for property 'longlinerWeatherStrategy'.
     *
     * @param longlinerWeatherStrategy Value to set for property 'longlinerWeatherStrategy'.
     */
    public void setLonglinerWeatherStrategy(
            AlgorithmFactory<? extends WeatherEmergencyStrategy> longlinerWeatherStrategy) {
        this.longlinerWeatherStrategy = longlinerWeatherStrategy;
    }

    /**
     * Getter for property 'longlinerRegulations'.
     *
     * @return Value for property 'longlinerRegulations'.
     */
    public AlgorithmFactory<? extends Regulation> getLonglinerRegulations() {
        return longlinerRegulations;
    }

    /**
     * Setter for property 'longlinerRegulations'.
     *
     * @param longlinerRegulations Value to set for property 'longlinerRegulations'.
     */
    public void setLonglinerRegulations(
            AlgorithmFactory<? extends Regulation> longlinerRegulations) {
        this.longlinerRegulations = longlinerRegulations;
    }

    /**
     * Getter for property 'longlinerGear'.
     *
     * @return Value for property 'longlinerGear'.
     */
    public AlgorithmFactory<? extends Gear> getLonglinerGear() {
        return longlinerGear;
    }

    /**
     * Setter for property 'longlinerGear'.
     *
     * @param longlinerGear Value to set for property 'longlinerGear'.
     */
    public void setLonglinerGear(AlgorithmFactory<? extends Gear> longlinerGear) {
        this.longlinerGear = longlinerGear;
    }





    /**
     * Getter for property 'cruiseSpeedInKph'.
     *
     * @return Value for property 'cruiseSpeedInKph'.
     */
    public DoubleParameter getLonglinerSpeedKph() {
        return longlinerSpeedKph;
    }

    /**
     * Setter for property 'cruiseSpeedInKph'.
     *
     * @param longlinerSpeedKph Value to set for property 'cruiseSpeedInKph'.
     */
    public void setLonglinerSpeedKph(DoubleParameter longlinerSpeedKph) {
        this.longlinerSpeedKph = longlinerSpeedKph;
    }

    public AlgorithmFactory<? extends LogbookInitializer> getLonglineLogbook() {
        return longlineLogbook;
    }

    public void setLonglineLogbook(
            AlgorithmFactory<? extends LogbookInitializer> longlineLogbook) {
        this.longlineLogbook = longlineLogbook;
    }

    public AlgorithmFactory<? extends LogbookInitializer> getHandlineLogbook() {
        return handlineLogbook;
    }

    public void setHandlineLogbook(
            AlgorithmFactory<? extends LogbookInitializer> handlineLogbook) {
        this.handlineLogbook = handlineLogbook;
    }

    public AlgorithmFactory<? extends Gear> getHandlinerGear() {
        return handlinerGear;
    }

    public void setHandlinerGear(
            AlgorithmFactory<? extends Gear> handlinerGear) {
        this.handlinerGear = handlinerGear;
    }

    public DoubleParameter getHandlinerHoldSize() {
        return handlinerHoldSize;
    }

    public void setHandlinerHoldSize(DoubleParameter handlinerHoldSize) {
        this.handlinerHoldSize = handlinerHoldSize;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getHandlinerDepartingStrategy() {
        return handlinerDepartingStrategy;
    }

    public void setHandlinerDepartingStrategy(
            AlgorithmFactory<? extends DepartingStrategy> handlinerDepartingStrategy) {
        this.handlinerDepartingStrategy = handlinerDepartingStrategy;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getHandlinerDestinationStrategy() {
        return handlinerDestinationStrategy;
    }

    public void setHandlinerDestinationStrategy(
            AlgorithmFactory<? extends DestinationStrategy> handlinerDestinationStrategy) {
        this.handlinerDestinationStrategy = handlinerDestinationStrategy;
    }

    public AlgorithmFactory<? extends FishingStrategy> getHandlinerFishingStrategy() {
        return handlinerFishingStrategy;
    }

    public void setHandlinerFishingStrategy(
            AlgorithmFactory<? extends FishingStrategy> handlinerFishingStrategy) {
        this.handlinerFishingStrategy = handlinerFishingStrategy;
    }

    public AlgorithmFactory<? extends GearStrategy> getHandlinerGearStrategy() {
        return handlinerGearStrategy;
    }

    public void setHandlinerGearStrategy(
            AlgorithmFactory<? extends GearStrategy> handlinerGearStrategy) {
        this.handlinerGearStrategy = handlinerGearStrategy;
    }

    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getHandlinerWeatherStrategy() {
        return handlinerWeatherStrategy;
    }

    public void setHandlinerWeatherStrategy(
            AlgorithmFactory<? extends WeatherEmergencyStrategy> handlinerWeatherStrategy) {
        this.handlinerWeatherStrategy = handlinerWeatherStrategy;
    }

    public AlgorithmFactory<? extends Regulation> getHandlinerRegulations() {
        return handlinerRegulations;
    }

    public void setHandlinerRegulations(
            AlgorithmFactory<? extends Regulation> handlinerRegulations) {
        this.handlinerRegulations = handlinerRegulations;
    }

    public DoubleParameter getLonglinerTravellingCosts() {
        return longlinerTravellingCosts;
    }

    public void setLonglinerTravellingCosts(DoubleParameter longlinerTravellingCosts) {
        this.longlinerTravellingCosts = longlinerTravellingCosts;
    }

    public DoubleParameter getHandlinerTravellingCosts() {
        return handlinerTravellingCosts;
    }

    public void setHandlinerTravellingCosts(DoubleParameter handlinerTravellingCosts) {
        this.handlinerTravellingCosts = handlinerTravellingCosts;
    }

    public DoubleParameter getHandlinerSpeedKph() {
        return handlinerSpeedKph;
    }

    public void setHandlinerSpeedKph(DoubleParameter handlinerSpeedKph) {
        this.handlinerSpeedKph = handlinerSpeedKph;
    }
}

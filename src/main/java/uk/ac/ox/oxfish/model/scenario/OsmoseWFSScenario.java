package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.TimeSeriesWeatherFactory;
import uk.ac.ox.oxfish.fisher.DockingListener;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.MovingAveragePredictor;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LonglineFloridaLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FloridaLogitDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
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
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseBoundedMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.*;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnToList;
import uk.ac.ox.oxfish.utility.TimeSeriesActuator;
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

/**
 * The scenario to run Osmose WFS.
 * Created by carrknight on 11/17/16.
 */
public class OsmoseWFSScenario implements Scenario{


    public final static Path mainDirectory = Paths.get("temp_wfs");

    private final OsmoseBiologyFactory biologyInitializer = new OsmoseBiologyFactory();
    private LinkedHashMap<Port, Integer> numberOfFishersPerPort;

    {
        biologyInitializer.setIndexOfSpeciesToBeManagedByThisModel("2");
        biologyInitializer.setOsmoseConfigurationFile(mainDirectory.resolve("wfs").resolve("osm_all-parameters.csv").toAbsolutePath().toString());
        biologyInitializer.setPreInitializedConfiguration(false);
        //biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(114*12);
        biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(10);
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


    private AlgorithmFactory<? extends Gear> gear = new RandomCatchabilityTrawlFactory();



    private DoubleParameter longlinerHoldSize = new FixedDoubleParameter(140175.5);


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
    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> longlinerFishingStrategy =
            new MaximumStepsFactory();

    private AlgorithmFactory<? extends GearStrategy> longlinerGearStrategy =
            new FixedGearStrategyFactory();


    private AlgorithmFactory<? extends WeatherEmergencyStrategy> longlinerWeatherStrategy =
            new IgnoreWeatherFactory();

    private AlgorithmFactory<? extends Regulation> longlinerRegulations =  new ProtectedAreasOnlyFactory();


    private NetworkBuilder networkBuilder =
            new EquidegreeBuilder();


    private DoubleParameter hourlyTravellingCosts = new FixedDoubleParameter(0);


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


            numberOfFishersPerPort = PortReader.readFile(
                    mainDirectory.resolve("longline_ports.csv"),
                    map,
                    () -> {
                        MarketMap markets = new MarketMap(global);
                        for(Species species : global.getSpecies())
                            markets.addMarket(species, new FixedPriceMarket(1));

                        return markets;
                    },
                    0.1234);


            for(Port port : numberOfFishersPerPort.keySet())
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
        NauticalMap map = model.getMap();
        MersenneTwisterFast random = model.getRandom();



        //longliners
        for(Map.Entry<Port,Integer> entry : numberOfFishersPerPort.entrySet())

            for(int id=0;id<entry.getValue();id++)
            {
                final double speed = 1;
                final double engineWeight = 1;
                final double mileage = 1;
                final double fuelCapacity = 100000000;

                Gear fisherGear = gear.apply(model);


                Fisher newFisher = new Fisher(fisherCounter, entry.getKey(),
                                              model.getRandom(),
                                              longlinerRegulations.apply(model),
                                              longlinerDepartingStrategy.apply(model),
                                              longlinerDestinationStrategy.apply(model),
                                              longlinerFishingStrategy.apply(model),
                                              longlinerGearStrategy.apply(model),
                                              longlinerWeatherStrategy.apply(model),
                                              new Boat(10, 10,
                                                       new Engine(engineWeight,
                                                                  mileage,
                                                                  speed),
                                                       new FuelTank(fuelCapacity)),
                                              new Hold(
                                                      longlinerHoldSize.apply(random),
                                                      biology.getSize()), fisherGear,
                                              model.getSpecies().size());
                newFisher.getTags().add("large");
                newFisher.getTags().add("ship");
                newFisher.getTags().add("blue");
                fisherCounter++;





                //predictors
                for(Species species : model.getSpecies())
                {

                    //create the predictors

                    newFisher.setDailyCatchesPredictor(species.getIndex(),
                                                       MovingAveragePredictor.dailyMAPredictor(
                                                               "Predicted Daily Catches of " + species,
                                                               fisher1 ->
                                                                       //check the daily counter but do not input new values
                                                                       //if you were not allowed at sea
                                                                       fisher1.getDailyCounter().getLandingsPerSpecie(
                                                                               species.getIndex())

                                                               ,
                                                               365));




                    newFisher.setProfitPerUnitPredictor(species.getIndex(), MovingAveragePredictor.perTripMAPredictor(
                            "Predicted Unit Profit " + species,
                            fisher1 -> fisher1.getLastFinishedTrip().getUnitProfitPerSpecie(species.getIndex()),
                            30));



                }


                //daily profits predictor
                newFisher.assignDailyProfitsPredictor(
                        MovingAveragePredictor.dailyMAPredictor("Predicted Daily Profits",
                                                                fisher ->
                                                                        //check the daily counter but do not input new values
                                                                        //if you were not allowed at sea
                                                                        fisher.isAllowedAtSea() ?
                                                                                fisher.getDailyCounter().
                                                                                        getColumn(
                                                                                                YearlyFisherTimeSeries.CASH_FLOW_COLUMN)
                                                                                :
                                                                                Double.NaN
                                ,

                                                                7));

                fisherList.add(newFisher);

                //add other trip costs
                newFisher.addDockingListener(
                        (DockingListener) (fisher, port1) -> {
                            if (fisher.getHoursAtSea() > 0)
                                fisher.spendForTrip(hourlyTravellingCosts.apply(model.getRandom())
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
}

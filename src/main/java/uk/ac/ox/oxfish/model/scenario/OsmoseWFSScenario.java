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
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FloridaLogisticDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.BarebonesFloridaDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FloridaLogitDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingAllUnsellableFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FloridaLogitReturnFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.FixedGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
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
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.event.BiomassDrivenFixedExogenousCatches;
import uk.ac.ox.oxfish.model.event.OsmoseBoundedExogenousCatches;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.TimeSeriesGasPriceMaker;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.network.NetworkPredicate;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.WeakMultiTACStringFactory;
import uk.ac.ox.oxfish.utility.*;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NullParameter;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The scenario to lspiRun Osmose WFS.
 * Created by carrknight on 11/17/16.
 */
public class OsmoseWFSScenario implements Scenario{


    public final static Path mainDirectory = Paths.get("temp_wfs");

    private OsmoseBiologyFactory biologyInitializer = new OsmoseBiologyFactory();


    {
        biologyInitializer.setIndexOfSpeciesToBeManagedByThisModel("2,3,4");
        biologyInitializer.setOsmoseConfigurationFile(mainDirectory.resolve("wfs").resolve("osm_all-parameters.csv").toAbsolutePath().toString());
        biologyInitializer.setPreInitializedConfigurationDirectory( mainDirectory.resolve("wfs").resolve("randomStarts").toAbsolutePath().toString());
        biologyInitializer.setPreInitializedConfiguration(true);
        biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(114*12);
        biologyInitializer.setScalingFactor(new FixedDoubleParameter(1000d));
        //biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(10);
        //this should take care of selectivityi
        biologyInitializer.getRecruitmentAges().put(2,2);
        biologyInitializer.getRecruitmentAges().put(3,2);
        biologyInitializer.getRecruitmentAges().put(4,1);
        biologyInitializer.getDiscardMortalityRate().put(2,.374); //red grouper SEDAR 42, table 2.12 (assuming the most common depth of fishing)
        biologyInitializer.getDiscardMortalityRate().put(3,.5); //gag grouper SEDAR 33, figure 6.1 (although it really is a function)
        biologyInitializer.getDiscardMortalityRate().put(4,0.82875); //red snapper SEDAR 31, table 5.1, average no-venting mortality
    }

    private LinkedHashMap<Port, Integer> longlinersPerPort;

    private LinkedHashMap<Port, Integer> handlinersPerPort;


    /**
     * price per kg grabbed from noaa annual west florida landings
     */

    private DoubleParameter redSnapperPrice = new FixedDoubleParameter(5.424372922);

    private DoubleParameter redGrouperPrice = new FixedDoubleParameter(5.616258398);

    private DoubleParameter gagGrouperPrice = new FixedDoubleParameter(4.414031175);



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


    private AlgorithmFactory<? extends Gear> longlinerGear = new RandomTrawlStringFactory("2:0.001,3:0.001,4:0.001");

    private AlgorithmFactory<? extends Gear> handlinerGear = new RandomTrawlStringFactory("2:0.001,3:0.001,4:0.001");


    //comes as 95th percentile from Steve's data on hold-sizes
    private DoubleParameter longlinerHoldSize = new FixedDoubleParameter(6500); // in kg!

    //comes as 95th percentile from Steve's data on hold-sizes
    private DoubleParameter handlinerHoldSize = new FixedDoubleParameter(2200); // in kg!


    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> handlinerDepartingStrategy =
            new FloridaLogisticDepartingFactory();
    {

        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setIntercept(new FixedDoubleParameter(-2.075184));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setSpring(new FixedDoubleParameter(0.725026));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setSummer(new FixedDoubleParameter(0.624472));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setWinter(new FixedDoubleParameter(0.266862));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setWeekend(new FixedDoubleParameter(-0.097619));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setWindSpeedInKnots(new FixedDoubleParameter(-0.046672));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setRealDieselPrice(new FixedDoubleParameter(-0.515073 / 0.219969157));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setPriceRedGrouper(new FixedDoubleParameter(-0.3604 / 2.20462262));
        ((FloridaLogisticDepartingFactory) handlinerDepartingStrategy).setPriceGagGrouper(new FixedDoubleParameter(0.649616 / 2.20462262));


    }


    private AlgorithmFactory<? extends DiscardingStrategy> handlinerDiscardingStrategy = new DiscardingAllUnsellableFactory();


    /**
     * factory to produce destination strategy
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
        //scaled $/kg
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setIntercept(new FixedDoubleParameter(-3.47701));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setPriceRedGrouper(new FixedDoubleParameter(0.92395 / 2.20462262));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setPriceGagGrouper(new FixedDoubleParameter(-0.65122 / 2.20462262));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setRatioCatchToFishHold(new FixedDoubleParameter(4.37828));
        ((FloridaLogitReturnFactory) handlinerFishingStrategy).setWeekendDummy(new FixedDoubleParameter(-0.24437));


    }

    private AlgorithmFactory<? extends GearStrategy> handlinerGearStrategy =
            new FixedGearStrategyFactory();


    private AlgorithmFactory<? extends WeatherEmergencyStrategy> handlinerWeatherStrategy =
            new IgnoreWeatherFactory();




    //(4)RED SNAPPER:
    //quotas from 96 to 2007 red snapper: 2086524.902kg (4.65 million pounds; SEDAR 31, table 2.5.1)
    //however this was split among all the gulf states; florida west coast only got about 800k pounds
    //so that the four average 2003-2006 is 337 metric tonnes or 337000kg
    //the quota on the gulf dropped a lot in 2007 but there was more or less the same amount of catching in the gulf

    //(3)GAG GROUPER:
    //SWG quota (for gag grouper) was 3991612.856 (8.8 million pounds, but this was the whole SWG; SEDAR 33 table 2.6.1)
    //you see landings dropping from 2.6mp to 1.3mp from 2003 to 2007 but the season length of SEDAR is still 320 days
    //so it's possible that it wasn't quota related and just that this fish is not constraining

    //(2)RED GROUPER:
    // from 2004, red grouper quota was 2408575.485kg (5.31 million pounds, SEDAR 42, table 2.7.1)
    //this is a bit weird though because it was routinely exceeded in the landings (6 mp)



    //commercial landings:
    //RED SNAPPER:
    // 337,100 kg (from 2003 to 2006)
    // 447,800 kg (from 2006 to 2010)

    //GAG GROUPER:
    // 1,119,175 kg (2003 to 2006)
    // 477,925 kg (2007 to 2010)

    //RED GROUPER:
    //2,843,775 kg (2003 to 2006)
    //2,024,700 kg (2007 to 2010)


    private LinkedHashMap<String, String> exogenousCatches = new LinkedHashMap<>();
    {
        //recreational mortality
        //RED SNAPPER:
        //2003 to 2007 average (a+b1) seems to be 935,084.25 kg

        //GAG GROUPER:
        //noaa fisheries harvest (a+b1) 1,607,978.75kg a year on average (2003 to 2006)

        //RED GROUPER:
        //noaa fisheries harvest (a+b1) 887,488kg a year on average (2003 to 2006)



        exogenousCatches.put("RedSnapper",Double.toString(935084.25d));
        exogenousCatches.put("GagGrouper",Double.toString(1607978.75d));
        exogenousCatches.put("RedGrouper",Double.toString(887488d));


    }


    /**
     * this is done separately because mortality is mostly concentrated against juveniles
     */
    //this number comes from SEDAR estimate of 1.5M fish aged 0-1 killed by shrimp bycatch weighted at 83.1g which is the 6 months weight of the fish
   // private DoubleParameter redSnapperMortalityFromShrimpBycatchInKg = new FixedDoubleParameter(124650);
    private DoubleParameter redSnapperMortalityFromShrimpBycatchInKg = new FixedDoubleParameter(124650);



    //
    /**
     * factory to produce Departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> longlinerDepartingStrategy =
            new FloridaLogisticDepartingFactory();

    {


        {

            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setIntercept(new FixedDoubleParameter(-2.959116));
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setSpring(new FixedDoubleParameter(0.770212));
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setSummer(new FixedDoubleParameter(0.933939));
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setWinter(new FixedDoubleParameter(0.706415));
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setWeekend(new NullParameter());
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setWindSpeedInKnots(new FixedDoubleParameter(0.004265));
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setRealDieselPrice(new FixedDoubleParameter(-0.125913/ 0.219969157));
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setPriceRedGrouper(new NullParameter());
            ((FloridaLogisticDepartingFactory) longlinerDepartingStrategy).setPriceGagGrouper(new NullParameter());


        }

    }

    /**
     * factory to produce destination strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> longlinerDestinationStrategy =
            new FloridaLogitDestinationFactory();



    private AlgorithmFactory<? extends DiscardingStrategy> longlinerDiscardingStrategy = new DiscardingAllUnsellableFactory();

    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> longlinerFishingStrategy =
            new FloridaLogitReturnFactory();

    {
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setIntercept(new FixedDoubleParameter(-2.74969d));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setPriceRedGrouper(new FixedDoubleParameter(-0.12476 / 2.20462262));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setPriceGagGrouper(new FixedDoubleParameter(-0.28232 / 2.20462262));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setRatioCatchToFishHold(new FixedDoubleParameter(4.53092));
        ((FloridaLogitReturnFactory) longlinerFishingStrategy).setWeekendDummy(new FixedDoubleParameter(-0.15556));

    }

    private AlgorithmFactory<? extends GearStrategy> longlinerGearStrategy =
            new FixedGearStrategyFactory();


    private AlgorithmFactory<? extends WeatherEmergencyStrategy> longlinerWeatherStrategy =
            new IgnoreWeatherFactory();

    private AlgorithmFactory<? extends Regulation> regulations =  new MultipleRegulationsFactory();
    {
        MultipleRegulationsFactory regs = ((MultipleRegulationsFactory) regulations);
        regs.getTags().clear();
        regs.getFactories().clear();
        regs.getTags().add("all");
        WeakMultiTACStringFactory tripLimits = new WeakMultiTACStringFactory();
        tripLimits.setYearlyQuotaMaps("2:2408575.485,3:3991612.856,4:337000");
        regs.getFactories().add(tripLimits);

    }


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


            Function<SeaTile,MarketMap> marketSupplier = (location) -> {
                //create fixed price market
                MarketMap marketMap = new MarketMap(global);
                        /*
                        market prices for each species
                        */

                for (Species species : global.getSpecies()) {
                    if (species.getName().trim().equals("RedGrouper"))
                        marketMap.addMarket(species,
                                            new FixedPriceMarket(
                                                    redGrouperPrice.apply(model.getRandom()))
                        );
                    else if (species.getName().trim().equals("GagGrouper"))
                        marketMap.addMarket(species,
                                            new FixedPriceMarket(
                                                    gagGrouperPrice.apply(model.getRandom()))
                        );
                    else if (species.getName().trim().equals("RedSnapper"))
                        marketMap.addMarket(species,
                                            new FixedPriceMarket(
                                                    redSnapperPrice.apply(model.getRandom()))
                        );
                    else
                        marketMap.addMarket(species, new FixedPriceMarket(-1d));
                }
                return marketMap;
            };
            //transform $/gallon to $/liter
            CsvColumnToList gasPrices = new CsvColumnToList(
                    Paths.get("temp_wfs", "steve", "Fleet", "PriceWSgas.txt").toString(),
                    true,
                    '$',
                    13,
                    new Function<Double, Double>() {
                        @Override
                        public Double apply(Double dollarsPerGallon) {
                            return  dollarsPerGallon * 0.219969157; //ratio
                        }
                    }
            );
            LinkedList<Double> prices = gasPrices.readColumn();
            TimeSeriesGasPriceMaker gasPriceMaker = new TimeSeriesGasPriceMaker(
                    prices,
                    true,
                    IntervalPolicy.EVERY_DAY
            );
            longlinersPerPort = reader.readFile(
                    mainDirectory.resolve("longline_ports.csv"),
                    map,
                    marketSupplier,
                    gasPriceMaker,
                    model
                    );


            handlinersPerPort = reader.readFile(mainDirectory.resolve("handline_ports.csv"),
                                                map,
                                                marketSupplier,
                                                gasPriceMaker,
                                                model);

            for(Port port : reader.getPorts())
                map.addPort(port);


            //exogenous mortality
            LinkedHashMap<Species,Double>  recast = new LinkedHashMap<>();
            for (Map.Entry<String, String> exogenous : exogenousCatches.entrySet()) {
                recast.put(global.getSpecie(exogenous.getKey()),Double.parseDouble(exogenous.getValue()));
            }
            //start it!
            BiomassDrivenFixedExogenousCatches recreationalMortality = new BiomassDrivenFixedExogenousCatches(recast);
            model.registerStartable(recreationalMortality);

            //shrimp mortality
            LinkedHashMap<Species,Double> mortality = new LinkedHashMap<>();
            mortality.put(global.getSpecie("RedSnapper"),redSnapperMortalityFromShrimpBycatchInKg.apply(model.getRandom()));
            LinkedHashMap<Species,Pair<Integer,Integer>> bounds = new LinkedHashMap<>();
            bounds.put(global.getSpecie("RedSnapper"),new Pair<>(0,1));

            OsmoseBoundedExogenousCatches shrimpMortality =  new OsmoseBoundedExogenousCatches(
                    mortality, bounds, "Shrimp-Bycatch Biomass Lost "
            );
            model.registerStartable(shrimpMortality);


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

        final double mileage = 2.82481; //this is litre per km and it comes from the california estimate of 1.21 gallons a mile
        final double engineWeight = 1;
        final double fuelCapacity = 100000000;
        //longliners
        for(Map.Entry<Port,Integer> entry : longlinersPerPort.entrySet())

            for(int i=0;i<entry.getValue();i++)
            {
                final double speed = longlinerSpeedKph.apply(random);


                Gear fisherGear = longlinerGear.apply(model);


                Fisher newFisher = new Fisher(fisherCounter, entry.getKey(),
                                              model.getRandom(),
                                              regulations.apply(model),
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
                                                      biology),
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

                Gear fisherGear = handlinerGear.apply(model);


                Fisher newFisher = new Fisher(fisherCounter, entry.getKey(),
                                              model.getRandom(),
                                              regulations.apply(model),
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
                        handlinerHoldSize.apply(random), biology),
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


    /**
     * Getter for property 'redSnapperPrice'.
     *
     * @return Value for property 'redSnapperPrice'.
     */
    public DoubleParameter getRedSnapperPrice() {
        return redSnapperPrice;
    }

    /**
     * Setter for property 'redSnapperPrice'.
     *
     * @param redSnapperPrice Value to set for property 'redSnapperPrice'.
     */
    public void setRedSnapperPrice(DoubleParameter redSnapperPrice) {
        this.redSnapperPrice = redSnapperPrice;
    }

    /**
     * Getter for property 'redGrouperPrice'.
     *
     * @return Value for property 'redGrouperPrice'.
     */
    public DoubleParameter getRedGrouperPrice() {
        return redGrouperPrice;
    }

    /**
     * Setter for property 'redGrouperPrice'.
     *
     * @param redGrouperPrice Value to set for property 'redGrouperPrice'.
     */
    public void setRedGrouperPrice(DoubleParameter redGrouperPrice) {
        this.redGrouperPrice = redGrouperPrice;
    }

    /**
     * Getter for property 'gagGrouperPrice'.
     *
     * @return Value for property 'gagGrouperPrice'.
     */
    public DoubleParameter getGagGrouperPrice() {
        return gagGrouperPrice;
    }

    /**
     * Setter for property 'gagGrouperPrice'.
     *
     * @param gagGrouperPrice Value to set for property 'gagGrouperPrice'.
     */
    public void setGagGrouperPrice(DoubleParameter gagGrouperPrice) {
        this.gagGrouperPrice = gagGrouperPrice;
    }

    /**
     * Getter for property 'handlinerDiscardingStrategy'.
     *
     * @return Value for property 'handlinerDiscardingStrategy'.
     */
    public AlgorithmFactory<? extends DiscardingStrategy> getHandlinerDiscardingStrategy() {
        return handlinerDiscardingStrategy;
    }

    /**
     * Setter for property 'handlinerDiscardingStrategy'.
     *
     * @param handlinerDiscardingStrategy Value to set for property 'handlinerDiscardingStrategy'.
     */
    public void setHandlinerDiscardingStrategy(
            AlgorithmFactory<? extends DiscardingStrategy> handlinerDiscardingStrategy) {
        this.handlinerDiscardingStrategy = handlinerDiscardingStrategy;
    }

    /**
     * Getter for property 'longlinerDiscardingStrategy'.
     *
     * @return Value for property 'longlinerDiscardingStrategy'.
     */
    public AlgorithmFactory<? extends DiscardingStrategy> getLonglinerDiscardingStrategy() {
        return longlinerDiscardingStrategy;
    }

    /**
     * Setter for property 'longlinerDiscardingStrategy'.
     *
     * @param longlinerDiscardingStrategy Value to set for property 'longlinerDiscardingStrategy'.
     */
    public void setLonglinerDiscardingStrategy(
            AlgorithmFactory<? extends DiscardingStrategy> longlinerDiscardingStrategy) {
        this.longlinerDiscardingStrategy = longlinerDiscardingStrategy;
    }

    /**
     * Setter for property 'biologyInitializer'.
     *
     * @param biologyInitializer Value to set for property 'biologyInitializer'.
     */
    public void setBiologyInitializer(OsmoseBiologyFactory biologyInitializer) {
        this.biologyInitializer = biologyInitializer;
    }

    /**
     * Getter for property 'exogenousCatches'.
     *
     * @return Value for property 'exogenousCatches'.
     */
    public Map<String, String> getExogenousCatches() {
        return exogenousCatches;
    }

    /**
     * Setter for property 'exogenousCatches'.
     *
     * @param exogenousCatches Value to set for property 'exogenousCatches'.
     */
    public void setExogenousCatches(LinkedHashMap<String, String> exogenousCatches) {
        this.exogenousCatches = exogenousCatches;
    }

    /**
     * Setter for property 'regulations'.
     *
     * @param regulations Value to set for property 'regulations'.
     */
    public void setRegulations(
            AlgorithmFactory<? extends Regulation> regulations) {
        this.regulations = regulations;
    }

    /**
     * Getter for property 'regulations'.
     *
     * @return Value for property 'regulations'.
     */
    public AlgorithmFactory<? extends Regulation> getRegulations() {
        return regulations;
    }

    /**
     * Getter for property 'redSnapperMortalityFromShrimpBycatchInKg'.
     *
     * @return Value for property 'redSnapperMortalityFromShrimpBycatchInKg'.
     */
    public DoubleParameter getRedSnapperMortalityFromShrimpBycatchInKg() {
        return redSnapperMortalityFromShrimpBycatchInKg;
    }

    /**
     * Setter for property 'redSnapperMortalityFromShrimpBycatchInKg'.
     *
     * @param redSnapperMortalityFromShrimpBycatchInKg Value to set for property 'redSnapperMortalityFromShrimpBycatchInKg'.
     */
    public void setRedSnapperMortalityFromShrimpBycatchInKg(
            DoubleParameter redSnapperMortalityFromShrimpBycatchInKg) {
        this.redSnapperMortalityFromShrimpBycatchInKg = redSnapperMortalityFromShrimpBycatchInKg;
    }
}

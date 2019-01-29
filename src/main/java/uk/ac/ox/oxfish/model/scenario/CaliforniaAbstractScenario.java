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
import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.AllocatedBiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.weather.ConstantWeather;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.GarbageGearFactory;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializer;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.BarebonesContinuousDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.FixedGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.CartesianUTMDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.AStarPathfinder;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.sampling.SampledMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.gas.CsvTimeSeriesGasFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasFactory;
import uk.ac.ox.oxfish.model.market.gas.GasPriceMaker;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultiQuotaMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by carrknight on 6/15/17.
 */
public abstract class CaliforniaAbstractScenario implements Scenario {


    /**
     * if this is set to anything but 1 then it scales the <b> number </b> of fish for every tile
     * whose y is above 60 by this factor
     */
    private double californiaScaling = 1.0;

    private Path mainDirectory = Paths.get("inputs", "california");


    public static final double DEFAULT_CATCHABILITY = 0.00156832676d;
    /*
       * fuel efficiency per hour is 57l according to Toft et al
       */
    protected final static Double LITERS_OF_GAS_CONSUMED_PER_HOUR = 57d;

    private boolean resetBiologyAtYear1 = true;

    protected AlgorithmFactory<? extends Regulation> regulationPostReset =


            //2011 numbers:
            new MultiQuotaMapFactory(MultiQuotaMapFactory.QuotaType.ITQ,
                                     new Pair<>("Yelloweye rockfish", 600d),
                                     //        new Pair<>("Canary rockfish",41100d),
                                     new Pair<>("Dover sole", 22234500d),
                                     new Pair<>("Longspine Thornyhead", 1966250d),
                                     new Pair<>("Sablefish", 2724935d), //averaged out till 2014, and with south included
                                     new Pair<>("Shortspine thornyhead", 1481600.056d)
            );
    private String spatialFileName = "spatial.csv";


    {
        HashMap<String, Double> quotaExchangedPerMatch = new HashMap<>();
        quotaExchangedPerMatch.put("Yelloweye rockfish", 6d);
        quotaExchangedPerMatch.put("Dover sole", 500d);
        quotaExchangedPerMatch.put("Longspine Thornyhead", 500d);
        quotaExchangedPerMatch.put("Sablefish", 500d);
        quotaExchangedPerMatch.put("Shortspine thornyhead", 500d);
        ((MultiQuotaMapFactory) regulationPostReset).setQuotaExchangedPerMatch(
                quotaExchangedPerMatch
        );
    }


    /**
     * regulation to use before the biology reset. (if there is no reset, then this regulation stands forever)
     */
    protected AlgorithmFactory<? extends Regulation> regulationPreReset =

            new AnarchyFactory();


    private String californiaBathymetryFile = "california.csv";


    private AlgorithmFactory<? extends LogbookInitializer> logbook =
            new NoLogbookFactory();

    /**
     * gear maker
     */
    protected AlgorithmFactory<? extends Gear> gear =
            new GarbageGearFactory();
    protected LinkedHashMap<String, String> exogenousCatches = new LinkedHashMap<>();

    {
        //these numbers are just the total catches on the noaa website minus DTS catches from catcher vessel report
        //all for the year 2010
        exogenousCatches.put("Dover Sole", Double.toString(676.9 * 1000));
        exogenousCatches.put("Sablefish", Double.toString(4438.2 * 1000));

    }

    /**
     * filename containing all the ports
     */
    private String portFileName = "dts_ports_2011.csv";
    private int gridWidth = 50;
    private NetworkBuilder networkBuilder =
            new EquidegreeBuilder();
    /**
     * boat length in meters
     */
    private DoubleParameter boatLength = new FixedDoubleParameter(
            22.573488); //assuming meters: this is from the data request
    /**
     * boat breadth in meters
     */
    private DoubleParameter boatWidth = new FixedDoubleParameter(7); //assuming meters: this is from the Echo Belle boat
    /**
     * hold size of the boat in kg
     */
    // the Echo Belle has GRT of 54 tonnes, but how much is the net is just a guess
    private DoubleParameter holdSizePerBoat = new FixedDoubleParameter(8000);
    private DoubleParameter fuelTankInLiters = new FixedDoubleParameter(
            45519.577); //this is from data request, transformed in liters from gallons
    private DoubleParameter cruiseSpeedInKph = new FixedDoubleParameter(
            16.0661); //this is 8.675 knots from the data request
    //new thinking:
    //liters per hour 57 (toft)
    //kph 16.0661
    //liter per km : 57/16.0661
    private DoubleParameter literPerKilometer = new FixedDoubleParameter(3.547842974);

    private AlgorithmFactory<? extends GasPriceMaker> gasPriceMaker =
            new FixedGasFactory(0.89991382);

    {
        //read from data!
        gasPriceMaker = new CsvTimeSeriesGasFactory();
        ((CsvTimeSeriesGasFactory) gasPriceMaker).setScaling(0.219969157);
        ((CsvTimeSeriesGasFactory) gasPriceMaker).setLoopThroughTheCSV(false);
        ((CsvTimeSeriesGasFactory) gasPriceMaker).setCsvFile(
                Paths.get("inputs","california","2010_gasprice.csv")
        );

    }

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

    /*{
        ((PerTripImitativeDestinationFactory) destinationStrategy).setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
    }*/

    //pre-ITQ:
    /*
    {
        this.destinationStrategy = new BarebonesContinuousDestinationFactory();
        CentroidMapFileFactory discretizer = new CentroidMapFileFactory();
        discretizer.setFilePath(mainDirectory.resolve("logit").resolve("centroids_utm10N.csv").toString());
        discretizer.setAutomaticallyIgnoreWastelands(true);
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setDiscretizer(
                discretizer
        );
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setDistanceInKm(
                new FixedDoubleParameter(-0.0152236867527225)
        );
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setHabitIntercept(
                new FixedDoubleParameter(0.172763449106076)
        );
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setHabitPeriodInDays(
                new FixedDoubleParameter(365)
        );

    }
    */
    //post-ITQ:
    {
        this.destinationStrategy = new BarebonesContinuousDestinationFactory();
        CentroidMapFileFactory discretizer = new CentroidMapFileFactory();
        discretizer.setFilePath(mainDirectory.resolve("logit").resolve("centroids_utm10N.csv").toString());
        discretizer.setAutomaticallyIgnoreWastelands(true);
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setDiscretizer(
                discretizer
        );
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setDistanceInKm(
                new FixedDoubleParameter(-0.0135515257873626)
        );
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setHabitIntercept(
                new FixedDoubleParameter(0.282719257782064)
        );
        ((BarebonesContinuousDestinationFactory) destinationStrategy).setHabitPeriodInDays(
                new FixedDoubleParameter(365)
        );

    }


    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy =
            new MaximumStepsFactory();
    private AlgorithmFactory<? extends GearStrategy> gearStrategy =
            new FixedGearStrategyFactory();
    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy =
            new IgnoreWeatherFactory();
    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy = new NoDiscardingFactory();
    private boolean usePremadeInput = false;
    /**
     * anything from crew to ice to insurance to maintenance. Paid as a lump-sum cost at the end of each trip
     */
    // https://dataexplorer.northwestscience.fisheries.noaa.gov/fisheye/PerformanceMetrics/
    //median variable cost per day in  in 2011 was  4,684$  to which we remove the fuel costs of 1,049
    //if you do this on average for the years we have data (2011-2015) we get 165$ an hour
    private DoubleParameter hourlyTravellingCosts = new FixedDoubleParameter(165.066666666667);
    private LinkedHashMap<Port,Integer> numberOfFishersPerPort;
    //these prices are averages from the catcher vessel report
    //2010
    private String priceMap = "Dover Sole:0.6698922,Sablefish:4.3235295," +
            "Shortspine Thornyhead:1.0428510," +
            "Longspine Thornyhead:1.0428510," +
            "Yelloweye Rockfish:1.0754502"
            +"," + MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME+":1.7646181"

            ;
    /*
    //these prices come from  http://pacfin.psmfc.org/pacfin_pub/data_rpts_pub/pfmc_rpts_pub/r058Wtwl_p15.txt
    private String priceMap = "Dover Sole:1.208,Sablefish:3.589,Shortspine Thornyhead:3.292,Longspine Thornyhead:0.7187,Yelloweye Rockfish:1.587"
            +"," + MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME+":1.0";
    */
    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {

        GlobalBiology biology;
        NauticalMap map;

        try {
            Path bioDirectory = mainDirectory.resolve("biology");

            DirectoryStream<Path> folders = Files.newDirectoryStream(bioDirectory);
            LinkedHashMap<String,Path> spatialFiles = new LinkedHashMap<>();
            LinkedHashMap<String, Path> folderMap = new LinkedHashMap<>();

            //sort it alphabetically to insure folders are consistently ranked
            List<Path> sortedFolders = new LinkedList<>();
            folders.forEach(sortedFolders::add);
            Collections.sort(sortedFolders, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName().toString(),
                                                                                              o2.getFileName().toString()));
            folders.close();

            //each folder is supposedly a species
            for(Path folder : sortedFolders)
            {


                Path file = folder.resolve(spatialFileName);
                if(file.toFile().exists())
                {
                    String name = folder.getFileName().toString();
                    spatialFiles.put(name, file);

                    /*
                    Preconditions.checkArgument(folder.resolve("count.csv").toFile().exists(),
                                                "The folder "+ name +
                                                        "  doesn't contain the abundance count.csv");

                    Preconditions.checkArgument(folder.resolve("meristics.yaml").toFile().exists(),
                                                "The folder "+ name +
                                                        "  doesn't contain the abundance count.csv");
*/
                    folderMap.put(folder.getFileName().toString(),folder);
                }
                else
                {
                    if(Log.WARN)
                        Log.warn(folder.getFileName() + " does not have a spatial.txt file and so cannot be distributed on the map. " +
                                         "It will be ignored");
                }

            }





            SampledMap sampledMap = null;
            if(usePremadeInput) {
                FileInputStream in = new FileInputStream(mainDirectory.resolve("premade.data").toFile());
                ObjectInputStream stream = new ObjectInputStream(in);
                try {
                    sampledMap = (SampledMap) stream.readObject();
                }
                catch (Exception e){
                    Log.error("Failed to read the premade california scenario.");
                    Log.error(e.toString());
                    System.exit(-1);
                }

                stream.close();
                in.close();

            }
            else
                sampledMap = new SampledMap(mainDirectory.resolve(californiaBathymetryFile),
                                            gridWidth,
                                            spatialFiles);

            //we want a grid of numbers but we have a grid where every cell has many observations
            int gridHeight = sampledMap.getGridHeight();
            ObjectGrid2D altitudeGrid = new ObjectGrid2D(gridWidth, gridHeight);
            Table<Integer,Integer,LinkedList<Double>> sampledAltitudeGrid = sampledMap.getAltitudeGrid();

            //so for altitude we just average them out
            for(int x=0;x<gridWidth;x++)
                for(int y=0;y<gridHeight;y++)
                {
                    OptionalDouble average = sampledAltitudeGrid.get(x, y).
                            stream().mapToDouble(
                            value -> value).filter(
                            aDouble -> aDouble > -9999).average();
                    altitudeGrid.set(x, y,
                                     new SeaTile(x, y, average.orElseGet(() -> 1000d), new TileHabitat(0)));
                }

            biology = buildBiology(model, folderMap);

            GeomGridField unitedMap = new GeomGridField(altitudeGrid);
            unitedMap.setMBR(sampledMap.getMbr());

            //create the map
            CartesianUTMDistance distance = new CartesianUTMDistance();
            map = new NauticalMap(unitedMap, new GeomVectorField(),
                                  distance,
                                  new AStarPathfinder(distance));
            //for all species, find the total observations you get


            //this table contains for each x-y an array telling for each specie what is the average observation at x,y
            final Table<Integer,Integer,double[]> averagesTable = HashBasedTable.create(gridWidth, gridHeight);
            //go through the map
            for(int x=0;x<gridWidth;x++) {
                for (int y = 0; y < gridHeight; y++) {
                    double[] averages = new double[biology.getSize()];
                    averagesTable.put(x, y, averages);
                    SeaTile seaTile = map.getSeaTile(x, y);
                    seaTile.assignLocalWeather(new ConstantWeather(0, 0, 0));
                    seaTile.setBiology(
                            getBiologyInitializer()
                                    .generateLocal(biology, seaTile, model.getRandom(), gridHeight, gridWidth,map ));
                    //if it's sea (don't bother counting otherwise)
                    if (seaTile.getAltitude() < 0)
                    {
                        int i = 0;
                        //each specie grid value is an ObjectGrid2D whose cells are themselves list of observations
                        //for each species
                        for (Map.Entry<String, Table<Integer,Integer,LinkedList<Double>>> specieGrid :
                                sampledMap.getBiologyGrids().entrySet()) {
                            assert biology.getSpecie(i).getName().equals(specieGrid.getKey()); //check we got the correct one
                            //average
                            OptionalDouble average = specieGrid.getValue().get(x,
                                                                               y).stream().mapToDouble(
                                    value -> value).average();
                            averages[i] = average.orElse(0);
                            //scale californian cells a bit. I need to do it here since otherwise the change would be lost
                            //at reset time
                            if(y>=60 && californiaScaling != 1.0)
                                averages[i]*=californiaScaling;
                            i++;
                        }
                    }


                }
            }
            //now that we have the averages, we can compute their sum:
            final double[] sums = new double[biology.getSize()];
            for(double[] average : averagesTable.values())
                for(int i=0; i<sums.length; i++)
                    sums[i] += average[i];

            //and now finally we can turn all that into allocators
            for(Species current : biology.getSpecies())
                getBiologyInitializer().putAllocator(current, input ->
                        (averagesTable.get(input.getGridX(), input.getGridY())[current.getIndex()])
                                /
                                sums[current.getIndex()]);

            getBiologyInitializer().processMap(biology, map, model.getRandom(), model);




            //set yourself up to reset the biology at the given year if needed
            if(resetBiologyAtYear1) {

                //protect all biomass
/*
                for(SeaTile tile : map.getAllSeaTilesExcludingLandAsList())
                    tile.setBiology(
                            new UnfishableLocalBiologyDecorator(1,
                                                                tile.getBiology())
                            ,
                            false);
 */



                model.scheduleOnceInXDays(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        Log.info("Resetting all local biologies");

                        //stop protecting
                        /*
                        for(SeaTile tile : map.getAllSeaTilesExcludingLandAsList())
                            tile.setBiology(
                                    ((UnfishableLocalBiologyDecorator) tile.getBiology()).getDecorated()
                                    ,
                                    false);
                          */
                        for (Species current : biology.getSpecies()) {
                            getBiologyInitializer().resetLocalBiology(current);

                        }
                    }
                }, StepOrder.DAWN, 366);
                model.scheduleOnceAtTheBeginningOfYear(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                Log.info("Resetting all rules");

                                for(Fisher fisher : model.getFishers())
                                    fisher.setRegulation(regulationPostReset.apply(model));
                            }
                        },
                        StepOrder.DAWN,
                        1
                );

            }


            if(Log.TRACE)
                Log.trace("height: " +map.getHeight());



            PortReader reader = new PortReader();
            numberOfFishersPerPort = reader.readFile(
                    mainDirectory.resolve(portFileName),
                    map,
                    (location) -> {
                        MarketMap markets = new MarketMap(biology);
                        //these prices come from  http://pacfin.psmfc.org/pacfin_pub/data_rpts_pub/pfmc_rpts_pub/r058Wtwl_p15.txt

                        Map<String, String> prices = Splitter.on(",").withKeyValueSeparator(":").split(priceMap.trim());
                        for(Map.Entry<String,String> price : prices.entrySet()) {
                            markets.addMarket(biology.getSpecie(price.getKey()), new FixedPriceMarket(Double.valueOf(price.getValue())));
                            if(Log.DEBUG)
                                Log.debug(price.getKey() + " will have price " + price.getValue());
                        }
                        return markets;

                    },
                    gasPriceMaker.apply(model),
                    model);

            for(Port port : numberOfFishersPerPort.keySet())
                map.addPort(port);



            System.out.println("height " + map.distance(0,0,0,1) );
            System.out.println("width " + map.distance(0,0,1,0) );



            //add exogenous catches
            //first turn map of strings into map of species
            LinkedHashMap<Species,Double>  recast = new LinkedHashMap<>();
            for (Map.Entry<String, String> exogenous : exogenousCatches.entrySet()) {
                recast.put(biology.getSpecie(exogenous.getKey()),Double.parseDouble(exogenous.getValue()));
            }
            //start it!

            ExogenousCatches catches = turnIntoExogenousCatchesObject(recast);
            model.registerStartable(catches);


            return new ScenarioEssentials(biology, map);



        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Some files were missing!");
        }



    }

    @NotNull
    abstract protected ExogenousCatches turnIntoExogenousCatchesObject(LinkedHashMap<Species, Double> recast) ;

    /**
     * build the biology part!
     * @param model
     * @param folderMap
     * @return
     */
    protected abstract GlobalBiology buildBiology(FishState model, LinkedHashMap<String, Path> folderMap);

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {

        LinkedList<Fisher> fisherList = new LinkedList<>();

        GlobalBiology biology = model.getBiology();
        NauticalMap map = model.getMap();
        MersenneTwisterFast random = model.getRandom();


        //create logbook initializer
        LogbookInitializer log = logbook.apply(model);
        log.start(model);

        int fisherCounter=0;
        Consumer<Fisher> predictorSetup = FishStateUtilities.predictorSetup(true,
                                                                            biology);
        for(Map.Entry<Port,Integer> entry : numberOfFishersPerPort.entrySet())

            for(int id=0;id<entry.getValue();id++)
            {
                final double speed = cruiseSpeedInKph.apply(random);
                final double capacity = holdSizePerBoat.apply(random);
                final double engineWeight = 10000;
                final double mileage = literPerKilometer.apply(random);
                final double fuelCapacity = fuelTankInLiters.apply(random);

                Gear fisherGear = gear.apply(model);


                Fisher newFisher = new Fisher(fisherCounter, entry.getKey(),
                                              random,
                                              regulationPreReset.apply(model),
                                              departingStrategy.apply(model),
                                              destinationStrategy.apply(model),
                                              fishingStrategy.apply(model),
                                              gearStrategy.apply(model),
                                              discardingStrategy.apply(model),
                                              weatherStrategy.apply(model),
                                              new Boat(10, 10,
                                                       new Engine(engineWeight,
                                                                  mileage,
                                                                  speed),
                                                       new FuelTank(fuelCapacity)),
                                              new Hold(capacity, biology),
                                              fisherGear, model.getSpecies().size());
                fisherCounter++;


                predictorSetup.accept(newFisher);

                fisherList.add(newFisher);

                log.add(newFisher,model);
                //add other trip costs
                newFisher.getAdditionalTripCosts().add(
                        new HourlyCost(hourlyTravellingCosts.apply(random))
                );
            }



        //create the fisher factory object, it will be used by the fishstate object to create and kill fishers
        //while the model is running
        FisherFactory fisherFactory = new FisherFactory(
                () -> numberOfFishersPerPort.keySet().iterator().next(),
                new AlgorithmFactory<Regulation>() {
                    @Override
                    public Regulation apply(FishState state) {
                        if(state.getYear()<1 || !resetBiologyAtYear1)
                            return getRegulationPreReset().apply(state);
                        else
                            return getRegulationPostReset().apply(state);
                    }
                },
                departingStrategy,
                destinationStrategy,
                fishingStrategy,
                discardingStrategy,
                gearStrategy,
                weatherStrategy,
                () -> new Boat(10, 10, new Engine(0,
                                                  literPerKilometer.apply(random),
                                                  cruiseSpeedInKph.apply(random)),
                               new FuelTank(fuelTankInLiters.apply(random))),
                () -> new Hold(holdSizePerBoat.apply(random), biology),
                gear,

                fisherCounter);
        fisherFactory.getAdditionalSetups().add(predictorSetup);
        fisherFactory.getAdditionalSetups().add(new Consumer<Fisher>() {
            @Override
            public void accept(Fisher fisher) {
                log.add(fisher,model);
            }
        });


        if(fisherList.size() <=1)
            networkBuilder = new EmptyNetworkBuilder();



        //allow friendships only within people from the same port!
        networkBuilder.addPredicate((from, to) -> from.getHomePort().equals(to.getHomePort()));

        if(Log.DEBUG)
        {

            List<SeaTile> allSeaTilesAsList = map.getAllSeaTilesAsList();
            for(Species species : biology.getSpecies() )
                Log.debug(species.getName() + ", index " + species.getIndex() + " biomass is : " +
                                  allSeaTilesAsList.stream().mapToDouble(value -> value.getBiomass(species)).sum());
        }

        HashMap<String, FisherFactory> factory = new HashMap<>();
        factory.put(FishState.DEFAULT_POPULATION_NAME,
                    fisherFactory);

        return new ScenarioPopulation(fisherList, new SocialNetwork(networkBuilder), factory);


    }

    /**
     * Getter for property 'gridWidth'.
     *
     * @return Value for property 'gridWidth'.
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Setter for property 'gridWidth'.
     *
     * @param gridWidth Value to set for property 'gridWidth'.
     */
    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    /**
     * Getter for property 'networkBuilder'.
     *
     * @return Value for property 'networkBuilder'.
     */
    public NetworkBuilder getNetworkBuilder() {
        return networkBuilder;
    }

    /**
     * Setter for property 'networkBuilder'.
     *
     * @param networkBuilder Value to set for property 'networkBuilder'.
     */
    public void setNetworkBuilder(NetworkBuilder networkBuilder) {
        this.networkBuilder = networkBuilder;
    }

    /**
     * Getter for property 'boatLength'.
     *
     * @return Value for property 'boatLength'.
     */
    public DoubleParameter getBoatLength() {
        return boatLength;
    }

    /**
     * Setter for property 'boatLength'.
     *
     * @param boatLength Value to set for property 'boatLength'.
     */
    public void setBoatLength(DoubleParameter boatLength) {
        this.boatLength = boatLength;
    }

    /**
     * Getter for property 'boatWidth'.
     *
     * @return Value for property 'boatWidth'.
     */
    public DoubleParameter getBoatWidth() {
        return boatWidth;
    }

    /**
     * Setter for property 'boatWidth'.
     *
     * @param boatWidth Value to set for property 'boatWidth'.
     */
    public void setBoatWidth(DoubleParameter boatWidth) {
        this.boatWidth = boatWidth;
    }

    /**
     * Getter for property 'holdSizePerBoat'.
     *
     * @return Value for property 'holdSizePerBoat'.
     */
    public DoubleParameter getHoldSizePerBoat() {
        return holdSizePerBoat;
    }

    /**
     * Setter for property 'holdSizePerBoat'.
     *
     * @param holdSizePerBoat Value to set for property 'holdSizePerBoat'.
     */
    public void setHoldSizePerBoat(DoubleParameter holdSizePerBoat) {
        this.holdSizePerBoat = holdSizePerBoat;
    }

    /**
     * Getter for property 'fuelTankInLiters'.
     *
     * @return Value for property 'fuelTankInLiters'.
     */
    public DoubleParameter getFuelTankInLiters() {
        return fuelTankInLiters;
    }

    /**
     * Setter for property 'fuelTankInLiters'.
     *
     * @param fuelTankInLiters Value to set for property 'fuelTankInLiters'.
     */
    public void setFuelTankInLiters(DoubleParameter fuelTankInLiters) {
        this.fuelTankInLiters = fuelTankInLiters;
    }

    /**
     * Getter for property 'cruiseSpeedInKph'.
     *
     * @return Value for property 'cruiseSpeedInKph'.
     */
    public DoubleParameter getCruiseSpeedInKph() {
        return cruiseSpeedInKph;
    }

    /**
     * Setter for property 'cruiseSpeedInKph'.
     *
     * @param cruiseSpeedInKph Value to set for property 'cruiseSpeedInKph'.
     */
    public void setCruiseSpeedInKph(DoubleParameter cruiseSpeedInKph) {
        this.cruiseSpeedInKph = cruiseSpeedInKph;
    }

    /**
     * Getter for property 'literPerKilometer'.
     *
     * @return Value for property 'literPerKilometer'.
     */
    public DoubleParameter getLiterPerKilometer() {
        return literPerKilometer;
    }

    /**
     * Setter for property 'literPerKilometer'.
     *
     * @param literPerKilometer Value to set for property 'literPerKilometer'.
     */
    public void setLiterPerKilometer(DoubleParameter literPerKilometer) {
        this.literPerKilometer = literPerKilometer;
    }


    /**
     * Getter for property 'gasPriceMaker'.
     *
     * @return Value for property 'gasPriceMaker'.
     */
    public AlgorithmFactory<? extends GasPriceMaker> getGasPriceMaker() {
        return gasPriceMaker;
    }

    /**
     * Setter for property 'gasPriceMaker'.
     *
     * @param gasPriceMaker Value to set for property 'gasPriceMaker'.
     */
    public void setGasPriceMaker(
            AlgorithmFactory<? extends GasPriceMaker> gasPriceMaker) {
        this.gasPriceMaker = gasPriceMaker;
    }

    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }

    /**
     * Setter for property 'gear'.
     *
     * @param gear Value to set for property 'gear'.
     */
    public void setGear(AlgorithmFactory<? extends Gear> gear) {
        this.gear = gear;
    }

    /**
     * Getter for property 'departingStrategy'.
     *
     * @return Value for property 'departingStrategy'.
     */
    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    /**
     * Setter for property 'departingStrategy'.
     *
     * @param departingStrategy Value to set for property 'departingStrategy'.
     */
    public void setDepartingStrategy(
            AlgorithmFactory<? extends DepartingStrategy> departingStrategy) {
        this.departingStrategy = departingStrategy;
    }

    /**
     * Getter for property 'destinationStrategy'.
     *
     * @return Value for property 'destinationStrategy'.
     */
    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    /**
     * Setter for property 'destinationStrategy'.
     *
     * @param destinationStrategy Value to set for property 'destinationStrategy'.
     */
    public void setDestinationStrategy(
            AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    /**
     * Getter for property 'fishingStrategy'.
     *
     * @return Value for property 'fishingStrategy'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    /**
     * Setter for property 'fishingStrategy'.
     *
     * @param fishingStrategy Value to set for property 'fishingStrategy'.
     */
    public void setFishingStrategy(
            AlgorithmFactory<? extends FishingStrategy> fishingStrategy) {
        this.fishingStrategy = fishingStrategy;
    }

    /**
     * Getter for property 'weatherStrategy'.
     *
     * @return Value for property 'weatherStrategy'.
     */
    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getWeatherStrategy() {
        return weatherStrategy;
    }

    /**
     * Setter for property 'weatherStrategy'.
     *
     * @param weatherStrategy Value to set for property 'weatherStrategy'.
     */
    public void setWeatherStrategy(
            AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy) {
        this.weatherStrategy = weatherStrategy;
    }

    /**
     * Getter for property 'regulation'.
     *
     * @return Value for property 'regulation'.
     */
    public AlgorithmFactory<? extends Regulation> getRegulationPreReset() {
        return regulationPreReset;
    }

    /**
     * Setter for property 'regulation'.
     *
     * @param regulationPreReset Value to set for property 'regulation'.
     */
    public void setRegulationPreReset(
            AlgorithmFactory<? extends Regulation> regulationPreReset) {
        this.regulationPreReset = regulationPreReset;
    }

    public boolean isUsePremadeInput() {
        return usePremadeInput;
    }

    public void setUsePremadeInput(boolean usePremadeInput) {
        this.usePremadeInput = usePremadeInput;
    }

    /**
     * Getter for property 'mainDirectory'.
     *
     * @return Value for property 'mainDirectory'.
     */
    public Path getMainDirectory() {
        return mainDirectory;
    }

    /**
     * Setter for property 'mainDirectory'.
     *
     * @param mainDirectory Value to set for property 'mainDirectory'.
     */
    public void setMainDirectory(Path mainDirectory) {
        this.mainDirectory = mainDirectory;
    }

    /**
     * Getter for property 'hourlyTravellingCosts'.
     *
     * @return Value for property 'hourlyTravellingCosts'.
     */
    public DoubleParameter getHourlyTravellingCosts() {
        return hourlyTravellingCosts;
    }

    /**
     * Setter for property 'hourlyTravellingCosts'.
     *
     * @param hourlyTravellingCosts Value to set for property 'hourlyTravellingCosts'.
     */
    public void setHourlyTravellingCosts(DoubleParameter hourlyTravellingCosts) {
        this.hourlyTravellingCosts = hourlyTravellingCosts;
    }

    public String getPriceMap() {
        return priceMap;
    }

    public void setPriceMap(String priceMap) {
        this.priceMap = priceMap;
    }

    public AlgorithmFactory<? extends GearStrategy> getGearStrategy() {
        return gearStrategy;
    }

    public void setGearStrategy(
            AlgorithmFactory<? extends GearStrategy> gearStrategy) {
        this.gearStrategy = gearStrategy;
    }

    /**
     * Getter for property 'portFileName'.
     *
     * @return Value for property 'portFileName'.
     */
    public String getPortFileName() {
        return portFileName;
    }

    /**
     * Setter for property 'portFileName'.
     *
     * @param portFileName Value to set for property 'portFileName'.
     */
    public void setPortFileName(String portFileName) {
        this.portFileName = portFileName;
    }

    /**
     * Getter for property 'exogenousCatches'.
     *
     * @return Value for property 'exogenousCatches'.
     */
    public LinkedHashMap<String, String> getExogenousCatches() {
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
     * Getter for property 'californiaScaling'.
     *
     * @return Value for property 'californiaScaling'.
     */
    public double getCaliforniaScaling() {
        return californiaScaling;
    }

    /**
     * Setter for property 'californiaScaling'.
     *
     * @param californiaScaling Value to set for property 'californiaScaling'.
     */
    public void setCaliforniaScaling(double californiaScaling) {
        this.californiaScaling = californiaScaling;
    }


    public abstract AllocatedBiologyInitializer getBiologyInitializer();


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
     * Getter for property 'resetBiologyAtYear1'.
     *
     * @return Value for property 'resetBiologyAtYear1'.
     */
    public boolean isResetBiologyAtYear1() {
        return resetBiologyAtYear1;
    }

    /**
     * Setter for property 'resetBiologyAtYear1'.
     *
     * @param resetBiologyAtYear1 Value to set for property 'resetBiologyAtYear1'.
     */
    public void setResetBiologyAtYear1(boolean resetBiologyAtYear1) {
        this.resetBiologyAtYear1 = resetBiologyAtYear1;
    }

    /**
     * Getter for property 'regulationPostReset'.
     *
     * @return Value for property 'regulationPostReset'.
     */
    public AlgorithmFactory<? extends Regulation> getRegulationPostReset() {
        return regulationPostReset;
    }

    /**
     * Setter for property 'regulationPostReset'.
     *
     * @param regulationPostReset Value to set for property 'regulationPostReset'.
     */
    public void setRegulationPostReset(
            AlgorithmFactory<? extends Regulation> regulationPostReset) {
        this.regulationPostReset = regulationPostReset;
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

    /**
     * Getter for property 'spatialFileName'.
     *
     * @return Value for property 'spatialFileName'.
     */
    public String getSpatialFileName() {
        return spatialFileName;
    }

    /**
     * Setter for property 'spatialFileName'.
     *
     * @param spatialFileName Value to set for property 'spatialFileName'.
     */
    public void setSpatialFileName(String spatialFileName) {
        this.spatialFileName = spatialFileName;
    }


    /**
     * Getter for property 'californiaBathymetryFile'.
     *
     * @return Value for property 'californiaBathymetryFile'.
     */
    public String getCaliforniaBathymetryFile() {
        return californiaBathymetryFile;
    }

    /**
     * Setter for property 'californiaBathymetryFile'.
     *
     * @param californiaBathymetryFile Value to set for property 'californiaBathymetryFile'.
     */
    public void setCaliforniaBathymetryFile(String californiaBathymetryFile) {
        this.californiaBathymetryFile = californiaBathymetryFile;
    }
}

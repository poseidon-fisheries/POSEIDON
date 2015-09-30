package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
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
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.FriendshipEdge;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.util.LinkedList;

/**
 * This is the scenario that recreates the NETLOGO prototype model. This means a fake generated sea and coast
 * Created by carrknight on 4/20/15.
 */
public class PrototypeScenario implements Scenario {

    /**
     * higher the more the coast gets jagged
     */
    private int coastalRoughness = 4;
    /**
     * how many rounds of depth smoothing to do
     */
    private int depthSmoothing = 1000000;

    /**
     * number of ports
     */
    private int ports = 1;
    /**
     * map width
     */
    private int width = 50;



    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
            new DiffusingLogisticFactory();

    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
            new ConstantWeatherFactory();

    /**
     * map height
     */
    private int height =50;

    /**
     * the number of fishers
     */
    private int fishers = 100;



    /**
     * Uses Caartesian distance
     */
    private double gridCellSizeInKm = 10;

    /**
     * when this flag is true, agents use their memory to predict future catches and profits
     */
    private boolean usePredictors = false;



    private int[] forcedPortPosition = null;

    /**
     * to use if you really want to port to be somewhere specific
     */
    public void forcePortPosition(int[] forcedPortPosition) {
        this.forcedPortPosition = forcedPortPosition;
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


    private DoubleParameter engineWeight = new NormalDoubleParameter(100,10);

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


    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy =
            new IgnoreWeatherFactory();

    private AlgorithmFactory<? extends Regulation> regulation =  new ProtectedAreasOnlyFactory();


    private AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>> networkBuilder =
            new EquidegreeBuilder();


    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();


    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();

    /**
     * if this is not NaN then it is used as the random seed to feed into the map-making function. This allows for randomness
     * in the biology/fishery
     */
    private Long mapMakerDedicatedRandomSeed =  null;

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





        BiologyInitializer initializer = biologyInitializer.apply(model);
        WeatherInitializer weather = weatherInitializer.apply(model);

        GlobalBiology biology = GlobalBiology.genericListOfSpecies(initializer.getNumberOfSpecies());

        NauticalMap map = NauticalMapFactory.prototypeMapWithRandomSmoothedBiology(coastalRoughness,
                                                                                   mapMakerRandom,
                                                                                   depthSmoothing,
                                                                                   width, height);
        //set habitats
        HabitatInitializer habitat = habitatInitializer.apply(model);
        habitat.applyHabitats(map, mapMakerRandom, model);



        map.setDistance(new CartesianDistance(gridCellSizeInKm));

        NauticalMapFactory.addWeatherAndBiologyToMap(map,random,initializer,
                                                     weather,
                                                     biology, model);





        //create fixed price market
        MarketMap marketMap = new MarketMap(biology);
        /*
      market prices for each species
     */



        for(Specie specie : biology.getSpecies())
            marketMap.addMarket(specie,market.apply(model));

        //create random ports, all sharing the same market
        if(forcedPortPosition == null)
            NauticalMapFactory.addRandomPortsToMap(map, ports, seaTile -> marketMap, mapMakerRandom);
        else
        {
            Port port = new Port(map.getSeaTile(forcedPortPosition[0],forcedPortPosition[1]),
                                 marketMap,0);
            map.addPort(port);
        }





        return new ScenarioEssentials(biology,map, marketMap);
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

        for(int i=0;i<fishers;i++)
        {
            Port port = ports[random.nextInt(ports.length)];
            DepartingStrategy departing = departingStrategy.apply(model);
            final double speed = speedInKmh.apply(random);
            final double capacity = holdSize.apply(random);
            final double engineWeight = this.engineWeight.apply(random);
            final double literPerKilometer = this.literPerKilometer.apply(random);
            final double  fuelCapacity = this.fuelTankSize.apply(random);

            double[] catchabilityMeanPerSpecie = new double[biology.getSize()];
            double[] catchabilitySTD = new double[biology.getSize()];

            Gear fisherGear = gear.apply(model);

            Fisher newFisher = new Fisher(i, port,
                                          random,
                                          regulation.apply(model),
                                          departing,
                                          destinationStrategy.apply(model),
                                          fishingStrategy.apply(model),
                                          weatherStrategy.apply(model),
                                          new Boat(10, 10, new Engine(engineWeight, literPerKilometer, speed),
                                                                                new FuelTank(fuelCapacity)),
                                          new Hold(capacity, biology.getSize()),
                                          fisherGear, model.getSpecies().size());






            //if needed, install better predictors
            if(usePredictors)
            {
                for(Specie specie : model.getSpecies())
                {
                    //create the predictors
                    newFisher.setDailyCatchesPredictor(specie.getIndex(),
                                                       MovingAveragePredictor.dailyMAPredictor(
                                                               "Predicted Daily Catches of " + specie,
                                                               fisher1 -> fisher1.getDailyCounter().getLandingsPerSpecie(
                                                                       specie.getIndex()),
                                                               90));
                    newFisher.setProfitPerUnitPredictor(specie.getIndex(), MovingAveragePredictor.perTripMAPredictor(
                            "Predicted Unit Profit " + specie,
                            fisher1 -> fisher1.getLastFinishedTrip().getUnitProfitPerSpecie(specie.getIndex()),
                            30));
                }
            }




            fisherList.add(newFisher);
        }



        //   GearImitationAnalysis.attachHoldSizeAnalysisToEachFisher(fisherList,model);


        return new ScenarioPopulation(fisherList,new SocialNetwork(networkBuilder));
    }

    public int getCoastalRoughness() {
        return coastalRoughness;
    }

    public void setCoastalRoughness(int coastalRoughness) {
        this.coastalRoughness = coastalRoughness;
    }

    public int getDepthSmoothing() {
        return depthSmoothing;
    }

    public void setDepthSmoothing(int depthSmoothing) {
        this.depthSmoothing = depthSmoothing;
    }



    public int getPorts() {
        return ports;
    }

    public void setPorts(int ports) {
        this.ports = ports;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFishers() {
        return fishers;
    }

    public void setFishers(int fishers) {
        this.fishers = fishers;
    }

    public double getGridCellSizeInKm() {
        return gridCellSizeInKm;
    }

    public void setGridCellSizeInKm(double gridCellSizeInKm) {
        this.gridCellSizeInKm = gridCellSizeInKm;
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


    public AlgorithmFactory<DirectedGraph<Fisher, FriendshipEdge>> getNetworkBuilder() {
        return networkBuilder;
    }

    public void setNetworkBuilder(
            AlgorithmFactory<DirectedGraph<Fisher, FriendshipEdge>> networkBuilder) {
        this.networkBuilder = networkBuilder;
    }

    public DoubleParameter getEngineWeight() {
        return engineWeight;
    }

    public void setEngineWeight(DoubleParameter engineWeight) {
        this.engineWeight = engineWeight;
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
}

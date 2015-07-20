package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.*;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Markets;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.FriendshipEdge;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * This is the scenario that recreates the NETLOGO prototype model. This means a fake generated sea and coast
 * Created by carrknight on 4/20/15.
 */
public class PrototypeScenario implements Scenario {

    /**
     * number of species
     */
    private int numberOfSpecies = 1;

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

    /**
     * map height
     */
    private int height =50;

    /**
     * the number of fishers
     */
    private int fishers = 300;



    /**
     * Uses Caartesian distance
     */
    private double gridSizeInKm = 10;

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
    private DoubleParameter fishingEfficiency = new FixedDoubleParameter(.01);


    private DoubleParameter engineWeight = new NormalDoubleParameter(100,10);

    private DoubleParameter fuelTankSize = new FixedDoubleParameter(10000);


    private DoubleParameter literPerKilometer = new FixedDoubleParameter(1);

    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy =
            new FixedProbabilityDepartingFactory();

    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy =
            new PerTripIterativeDestinationFactory();
    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy =
            new MaximumStepsFactory();


    private AlgorithmFactory<? extends Regulation> regulation =  new ProtectedAreasOnlyFactory();


    private AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>> networkBuilder =
            new EquidegreeBuilder();


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

        GlobalBiology biology = GlobalBiology.genericListOfSpecies(numberOfSpecies);

        NauticalMap map = NauticalMapFactory.prototypeMapWithRandomSmoothedBiology(coastalRoughness,
                                                                                   random,
                                                                                   depthSmoothing,
                                                                                   biologyInitializer.apply(model),
                                                                                   biology,
                                                                                   model, width, height);
        map.setDistance(new CartesianDistance(gridSizeInKm));


        //general biology
        //create fixed price market
        Markets markets = new Markets(biology);
        /*
      market prices for each species
     */
        double[] marketPrices = new double[biology.getSize()];
        Arrays.fill(marketPrices,10.0);

        for(Specie specie : biology.getSpecies())
            markets.addMarket(specie,new FixedPriceMarket(specie, marketPrices[specie.getIndex()]));

        //create random ports, all sharing the same market
        NauticalMapFactory.addRandomPortsToMap(map, ports, seaTile -> markets, random);





        return new ScenarioEssentials(biology,map,markets);
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
        for(int i=0;i<fishers;i++)
        {
            Port port = ports[random.nextInt(ports.length)];
            DepartingStrategy departing = departingStrategy.apply(model);
            final double speed = speedInKmh.apply(random);
            final double capacity = holdSize.apply(random);
            final double efficiency =fishingEfficiency.apply(random);
            final double engineWeight = this.engineWeight.apply(random);
            final double literPerKilometer = this.literPerKilometer.apply(random);
            final double  fuelCapacity = this.fuelTankSize.apply(random);

            fisherList.add(new Fisher(i, port,
                                        random,
                                        regulation.apply(model),
                                        departing,
                                        destinationStrategy.apply(model),
                                        fishingStrategy.apply(model),
                                        new Boat(10,10,new Engine(engineWeight,literPerKilometer,speed),
                                                 new FuelTank(fuelCapacity)),
                                        new Hold(capacity, biology.getSize()),
                                        new FixedProportionGear(efficiency)));
        }

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

    public double getGridSizeInKm() {
        return gridSizeInKm;
    }

    public void setGridSizeInKm(double gridSizeInKm) {
        this.gridSizeInKm = gridSizeInKm;
    }

    public DoubleParameter getSpeedInKmh() {
        return speedInKmh;
    }

    public void setSpeedInKmh(DoubleParameter speedInKmh) {
        this.speedInKmh = speedInKmh;
    }

    public DoubleParameter getFishingEfficiency() {
        return fishingEfficiency;
    }

    public void setFishingEfficiency(DoubleParameter fishingEfficiency) {
        this.fishingEfficiency = fishingEfficiency;
    }


    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
            AlgorithmFactory<? extends Regulation> regulation) {
        this.regulation = regulation;
    }


    public int getNumberOfSpecies() {
        return numberOfSpecies;
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
}

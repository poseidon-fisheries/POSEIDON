package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import fr.ird.osmose.OsmoseSimulation;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.*;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedProbabilityDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.osmose.OsmoseMapMaker;
import uk.ac.ox.oxfish.geography.osmose.OsmoseStepper;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.FriendshipEdge;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * This scenario uses standard osmose configuration and populate with simple agents. It's mostly a way to test how
 * osmose and this ABM would act together
 * Created by carrknight on 6/25/15.
 */
public class OsmosePrototype implements Scenario {

    private int buninLength = 100;


    private String osmoseConfigurationFile = FishStateUtilities.getAbsolutePath(
            Paths.get("inputs","osmose","prototype","osm_all-parameters.csv").toString());



    private boolean preInitializedConfiguration =true;

    private String preInitializedConfigurationDirectory =
            FishStateUtilities.getAbsolutePath(
                    Paths.get("inputs", "osmose", "prototype", "restart").toString()
            );




    private double gridSizeInKm = 5;
    private int ports = 1;

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


    private AlgorithmFactory<? extends Regulation> regulation =  new AnarchyFactory();

    private AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>> networkBuilder =
            new EquidegreeBuilder();

    private int fishers = 50;

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-resulredt object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {
        OsmoseSimulation osmoseSimulation=null;

        try {
            if(!preInitializedConfiguration)
                osmoseSimulation = OsmoseSimulation.startUpOSMOSESimulationWithBurnIn(buninLength,
                                                                                      osmoseConfigurationFile);
            else
            {
                ArrayList<Path> fileList = new ArrayList<>();
                Files.walk(Paths.get(preInitializedConfigurationDirectory), 1).filter(
                        path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".nc")
                ).forEach(fileList::add);

                osmoseSimulation = OsmoseSimulation.startupOSMOSEWithRestartFile(12,osmoseConfigurationFile,
                                                                                 fileList.get(model.getRandom().nextInt(fileList.size())).toString());
            }

        } catch (IOException e) {
            throw  new IllegalArgumentException("Can't instantiate OSMOSE!");
        }
        Specie[] species = new Specie[osmoseSimulation.getNumberOfSpecies()];
        for(int i=0; i<species.length; i++)
            species[i] = new Specie(osmoseSimulation.getSpecies(i).getName());

        GlobalBiology biology = new GlobalBiology(species);

        final OsmoseStepper stepper = new OsmoseStepper(model.getStepsPerDay() * 365, osmoseSimulation, model.random);
        NauticalMap map = OsmoseMapMaker.buildMap(osmoseSimulation, gridSizeInKm,stepper,model.random );



        //general biology
        //create fixed price market
        MarketMap marketMap = new MarketMap(biology);
        /*
      market prices for each species
     */
        double[] marketPrices = new double[biology.getSize()];
        Arrays.fill(marketPrices, 1.0);


        for(Specie specie : biology.getSpecies())
            marketMap.addMarket(specie,new FixedPriceMarket( marketPrices[specie.getIndex()]));

        //create random ports, all sharing the same market
        NauticalMapFactory.addRandomPortsToMap(map, ports, seaTile -> marketMap, model.random);


        model.registerStartable(stepper);

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
        for(int i=0;i< fishers;i++)
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
                                               new FuelTank(fuelCapacity)),                                      new Hold(capacity, biology.getSize()),
                                      new OneSpecieGear(biology.getSpecie(0),efficiency)));
        }

        SocialNetwork network = new SocialNetwork(networkBuilder);

        return new ScenarioPopulation(fisherList,network);


    }


    public int getBuninLength() {
        return buninLength;
    }


    public double getGridSizeInKm() {
        return gridSizeInKm;
    }

    public void setGridSizeInKm(double gridSizeInKm) {
        this.gridSizeInKm = gridSizeInKm;
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

    public DoubleParameter getHoldSize() {
        return holdSize;
    }

    public void setHoldSize(DoubleParameter holdSize) {
        this.holdSize = holdSize;
    }

    public DoubleParameter getFishingEfficiency() {
        return fishingEfficiency;
    }

    public void setFishingEfficiency(DoubleParameter fishingEfficiency) {
        this.fishingEfficiency = fishingEfficiency;
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

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(
            AlgorithmFactory<? extends DestinationStrategy> destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
            AlgorithmFactory<? extends Regulation> regulation) {
        this.regulation = regulation;
    }

    public int getFishers() {
        return fishers;
    }

    public void setFishers(int fishers) {
        this.fishers = fishers;
    }

    public void setBuninLength(int buninLength) {
        this.buninLength = buninLength;
    }


    public String getOsmoseConfigurationFile() {
        return osmoseConfigurationFile;
    }

    public void setOsmoseConfigurationFile(String osmoseConfigurationFile) {
        this.osmoseConfigurationFile = osmoseConfigurationFile;
    }

    public AlgorithmFactory<DirectedGraph<Fisher, FriendshipEdge>> getNetworkBuilder() {
        return networkBuilder;
    }

    public void setNetworkBuilder(
            AlgorithmFactory<DirectedGraph<Fisher, FriendshipEdge>> networkBuilder) {
        this.networkBuilder = networkBuilder;
    }

    public boolean isPreInitializedConfiguration() {
        return preInitializedConfiguration;
    }

    public void setPreInitializedConfiguration(boolean preInitializedConfiguration) {
        this.preInitializedConfiguration = preInitializedConfiguration;
    }

    public String getPreInitializedConfigurationDirectory() {
        return preInitializedConfigurationDirectory;
    }

    public void setPreInitializedConfigurationDirectory(String preInitializedConfigurationDirectory) {
        this.preInitializedConfigurationDirectory = preInitializedConfigurationDirectory;
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

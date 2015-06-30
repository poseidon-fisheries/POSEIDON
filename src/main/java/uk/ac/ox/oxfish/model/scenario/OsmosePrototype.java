package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import fr.ird.osmose.OsmoseSimulation;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.OneSpecieGear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingFactory;
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
import uk.ac.ox.oxfish.model.market.Markets;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.StrategyFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This scenario uses standard osmose configuration and populate with simple agents. It's mostly a way to test how
 * osmose and this ABM would act together
 * Created by carrknight on 6/25/15.
 */
public class OsmosePrototype implements Scenario {

    private int buninLength = 100;


    private String osmoseConfigurationFile = FishStateUtilities.getAbsolutePath(
            Paths.get("inputs","osmose","prototype","osm_all-parameters.csv").toString());

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


    /**
     * factory to produce departing strategy
     */
    private StrategyFactory<? extends DepartingStrategy> departingStrategy =
            new FixedProbabilityDepartingFactory();

    /**
     * factory to produce departing strategy
     */
    private StrategyFactory<? extends DestinationStrategy> destinationStrategy =
            new PerTripIterativeDestinationFactory();
    /**
     * factory to produce fishing strategy
     */
    private StrategyFactory<? extends FishingStrategy> fishingStrategy =
            new MaximumStepsFactory();


    private StrategyFactory<? extends Regulation> regulation =  new AnarchyFactory();

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

        final OsmoseSimulation osmoseSimulation = OsmoseSimulation.startUpOSMOSESimulationWithBurnIn(buninLength,
                                                                                                     osmoseConfigurationFile);

        Specie[] species = new Specie[osmoseSimulation.getNumberOfSpecies()];
        for(int i=0; i<species.length; i++)
            species[i] = new Specie(osmoseSimulation.getSpecies(i).getName());

        GlobalBiology biology = new GlobalBiology(species);

        final OsmoseStepper stepper = new OsmoseStepper(model.getStepsPerDay() * 365, osmoseSimulation, model.random);
        NauticalMap map = OsmoseMapMaker.buildMap(osmoseSimulation, gridSizeInKm,stepper,model.random );



        //general biology
        //create fixed price market
        Markets markets = new Markets(biology);
        /*
      market prices for each species
     */
        double[] marketPrices = new double[biology.getSize()];
        Arrays.fill(marketPrices, 1.0);


        for(Specie specie : biology.getSpecies())
            markets.addMarket(specie,new FixedPriceMarket(specie, marketPrices[specie.getIndex()]));

        //create random ports, all sharing the same market
        NauticalMapFactory.addRandomPortsToMap(map, ports, seaTile -> markets, model.random);


        model.registerStartable(stepper);

        return new ScenarioEssentials(biology,map,markets);


    }




    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public List<Fisher> populateModel(FishState model) {

        LinkedList<Fisher> fisherList = new LinkedList<>();
        final NauticalMap map = model.getMap();
        final GlobalBiology biology = model.getBiology();
        final MersenneTwisterFast random = model.random;

        Port[] ports =map.getPorts().toArray(new Port[map.getPorts().size()]);
        for(int i=0;i< fishers;i++)
        {
            Port port = ports[random.nextInt(ports.length)];
            DepartingStrategy departing = departingStrategy.apply(model);
            double speed = speedInKmh.apply(random);
            double capacity = holdSize.apply(random);
            double efficiency =fishingEfficiency.apply(random);
            fisherList.add(new Fisher(i, port,
                                      random,
                                      regulation.apply(model),
                                      departing,
                                      destinationStrategy.apply(model),
                                      fishingStrategy.apply(model),
                                      new Boat(10,10,speed),
                                      new Hold(capacity, biology.getSize()),
                                      new OneSpecieGear(biology.getSpecie(0),efficiency)));
        }

        return fisherList;


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

    public StrategyFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    public void setDepartingStrategy(
            StrategyFactory<? extends DepartingStrategy> departingStrategy) {
        this.departingStrategy = departingStrategy;
    }

    public StrategyFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(
            StrategyFactory<? extends FishingStrategy> fishingStrategy) {
        this.fishingStrategy = fishingStrategy;
    }

    public StrategyFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(
            StrategyFactory<? extends DestinationStrategy> destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public StrategyFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    public void setRegulation(
            StrategyFactory<? extends Regulation> regulation) {
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
}

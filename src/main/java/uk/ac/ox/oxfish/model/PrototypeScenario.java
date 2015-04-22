package uk.ac.ox.oxfish.model;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.Gear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.strategies.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategy;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;

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
     * how many rounds of biology smoothing to do
     */
    private int biologySmoothing = 1000000;
    /**
     * random minimum biomass pre-smoothing
     */
    private int minBiomass = 10;
    /**
     * random maximum biomass pre-smoothing
     */
    private int maxBiomass = 5000;
    /**
     * number of ports
     */
    private int ports = 1;
    /**
     * map width
     */
    private int width = 50;
    /**
     * map height
     */
    private int height =50;

    /**
     * the number of fishers
     */
    private int fishers = 0;

    /**
     * Uses Caartesian distance
     */
    private double gridSizeInKm = 10;

    private double minDepartingProbability = 0.2;

    private double maxDepartingProbability = 0.8;

    private double minSpeedInKmh = 1;

    private double maxSpeedInKmh = 5;

    private double minHoldSize = 100;
    private double maxHoldSize = 100;

    private double minFishingEfficiency = .02;
    private double maxFishingEfficiency = .02;

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioResult start(FishState model) {

        MersenneTwisterFast random = model.random;
        NauticalMap map = NauticalMapFactory.prototypeMapWithRandomSmoothedBiology(coastalRoughness,
                                                                                   random,
                                                                                   depthSmoothing,
                                                                                   biologySmoothing,
                                                                                   minBiomass,
                                                                                   maxBiomass,
                                                                                   ports,
                                                                                   width,
                                                                                   height);
        map.setDistance(new CartesianDistance(gridSizeInKm));


        GlobalBiology biology = new GlobalBiology(new Specie("TEST SPECIE"));


        //schedule to print repeatedly the day
        model.schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                System.out.println("the time is " + simState.schedule.getTime());
            }
        });

        LinkedList<Fisher> fisherList = new LinkedList<>();
        Port[] ports =map.getPorts().toArray(new Port[map.getPorts().size()]);
        for(int i=0;i<fishers;i++)
        {
            Port port = ports[random.nextInt(ports.length)];
            DepartingStrategy departing = new FixedProbabilityDepartingStrategy(
                    random.nextDouble(true,true)*(maxDepartingProbability-minDepartingProbability)
                            + minDepartingProbability);
            double speed = random.nextDouble(true,true) *
                    (maxSpeedInKmh - minSpeedInKmh) + minSpeedInKmh;
            double capacity = random.nextDouble(true,true) *
                    (maxHoldSize - minHoldSize) + minHoldSize;
            double efficiency = random.nextDouble(true,true) *
                    (maxFishingEfficiency - minFishingEfficiency) + minFishingEfficiency;
            fisherList.add(new Fisher(port,random, departing,
                                      new RandomThenBackToPortDestinationStrategy(),
                                      new Boat(speed),
                                      new Hold(capacity,biology.getSize()),
                                      new FixedProportionGear(efficiency)
            ));
        }

        return new ScenarioResult(biology,map,fisherList);
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

    public int getBiologySmoothing() {
        return biologySmoothing;
    }

    public void setBiologySmoothing(int biologySmoothing) {
        this.biologySmoothing = biologySmoothing;
    }

    public int getMinBiomass() {
        return minBiomass;
    }

    public void setMinBiomass(int minBiomass) {
        this.minBiomass = minBiomass;
    }

    public int getMaxBiomass() {
        return maxBiomass;
    }

    public void setMaxBiomass(int maxBiomass) {
        this.maxBiomass = maxBiomass;
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

    public double getMinDepartingProbability() {
        return minDepartingProbability;
    }

    public void setMinDepartingProbability(double minDepartingProbability) {
        this.minDepartingProbability = minDepartingProbability;
    }

    public double getMaxDepartingProbability() {
        return maxDepartingProbability;
    }

    public void setMaxDepartingProbability(double maxDepartingProbability) {
        this.maxDepartingProbability = maxDepartingProbability;
    }

    public double getMinSpeedInKmh() {
        return minSpeedInKmh;
    }

    public void setMinSpeedInKmh(double minSpeedInKmh) {
        this.minSpeedInKmh = minSpeedInKmh;
    }

    public double getMaxSpeedInKmh() {
        return maxSpeedInKmh;
    }

    public void setMaxSpeedInKmh(double maxSpeedInKmh) {
        this.maxSpeedInKmh = maxSpeedInKmh;
    }

    public double getMinHoldSize() {
        return minHoldSize;
    }

    public void setMinHoldSize(double minHoldSize) {
        this.minHoldSize = minHoldSize;
    }

    public double getMaxHoldSize() {
        return maxHoldSize;
    }

    public void setMaxHoldSize(double maxHoldSize) {
        this.maxHoldSize = maxHoldSize;
    }

    public double getMinFishingEfficiency() {
        return minFishingEfficiency;
    }

    public void setMinFishingEfficiency(double minFishingEfficiency) {
        this.minFishingEfficiency = minFishingEfficiency;
    }

    public double getMaxFishingEfficiency() {
        return maxFishingEfficiency;
    }

    public void setMaxFishingEfficiency(double maxFishingEfficiency) {
        this.maxFishingEfficiency = maxFishingEfficiency;
    }
}

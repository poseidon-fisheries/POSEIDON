package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An object produced by the scenario that allows the model to produce more fishers
 * Created by carrknight on 12/11/15.
 */
public class FisherFactory
{

    private int nextID;

    private Supplier<Port> portSupplier;

    private AlgorithmFactory<? extends Regulation> regulations;

    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy;

    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy;

    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy;

    private AlgorithmFactory<? extends GearStrategy> gearStrategy;

    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy;

    private Supplier<Boat> boatSupplier;

    private Supplier<Hold> holdSupplier;

    private AlgorithmFactory<? extends Gear> gear;

    /**
     * this consumers will be called after the fisher is created but before it is returned.
     * It can be used to add additional characteristics (tags/predictors/etc.) to the boat.
     */
    private final LinkedList<Consumer<Fisher>> additionalSetups = new LinkedList<>();


    private FisherFactory() {
    }

    public FisherFactory(
            Supplier<Port> portSupplier,
            AlgorithmFactory<? extends Regulation> regulations,
            AlgorithmFactory<? extends DepartingStrategy> departingStrategy,
            AlgorithmFactory<? extends DestinationStrategy> destinationStrategy,
            AlgorithmFactory<? extends FishingStrategy> fishingStrategy,
            AlgorithmFactory<? extends GearStrategy> gearStrategy,
            AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy,
            Supplier<Boat> boatSupplier, Supplier<Hold> holdSupplier,
            AlgorithmFactory<? extends Gear> gear, int nextID) {
        this.portSupplier = portSupplier;
        this.regulations = regulations;
        this.departingStrategy = departingStrategy;
        this.destinationStrategy = destinationStrategy;
        this.fishingStrategy = fishingStrategy;
        this.gearStrategy = gearStrategy;
        this.weatherStrategy = weatherStrategy;
        this.boatSupplier = boatSupplier;
        this.holdSupplier = holdSupplier;
        this.gear = gear;
        this.nextID = nextID;
    }



    /**
     * creates a fisher and returns it. Doesn't schedule it or add it to the rest of the model so use this
     * method while the model is still or if the model is running remember to add the fisher to the fisher list
     * and register it as a startable (also add it to the social network if needed)
     * @param fishState model
     * @return
     */
    public Fisher buildFisher(FishState fishState) {
        Fisher fisher = new Fisher(nextID++, portSupplier.get(),
                                   fishState.getRandom(),
                                   regulations.apply(fishState),
                                   departingStrategy.apply(fishState),
                                   destinationStrategy.apply(fishState),
                                   fishingStrategy.apply(fishState),
                                   gearStrategy.apply(fishState),
                                   weatherStrategy.apply(fishState),
                                   boatSupplier.get(),
                                   holdSupplier.get(),
                                   gear.apply(fishState),
                                   fishState.getSpecies().size());
        for(Consumer<Fisher> setup : additionalSetups)
            setup.accept(fisher);
        return fisher;

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
     * Setter for property 'regulations'.
     *
     * @param regulations Value to set for property 'regulations'.
     */
    public void setRegulations(
            AlgorithmFactory<? extends Regulation> regulations) {
        this.regulations = regulations;
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
     * Getter for property 'boatSupplier'.
     *
     * @return Value for property 'boatSupplier'.
     */
    public Supplier<Boat> getBoatSupplier() {
        return boatSupplier;
    }

    /**
     * Setter for property 'boatSupplier'.
     *
     * @param boatSupplier Value to set for property 'boatSupplier'.
     */
    public void setBoatSupplier(Supplier<Boat> boatSupplier) {
        this.boatSupplier = boatSupplier;
    }

    /**
     * Getter for property 'holdSupplier'.
     *
     * @return Value for property 'holdSupplier'.
     */
    public Supplier<Hold> getHoldSupplier() {
        return holdSupplier;
    }

    /**
     * Setter for property 'holdSupplier'.
     *
     * @param holdSupplier Value to set for property 'holdSupplier'.
     */
    public void setHoldSupplier(Supplier<Hold> holdSupplier) {
        this.holdSupplier = holdSupplier;
    }

    /**
     * Getter for property 'gear'.
     *
     * @return Value for property 'gear'.
     */
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
     * Getter for property 'portSupplier'.
     *
     * @return Value for property 'portSupplier'.
     */
    public Supplier<Port> getPortSupplier() {
        return portSupplier;
    }

    /**
     * Setter for property 'portSupplier'.
     *
     * @param portSupplier Value to set for property 'portSupplier'.
     */
    public void setPortSupplier(Supplier<Port> portSupplier) {
        this.portSupplier = portSupplier;
    }

    /**
     * Getter for property 'additionalSetups'.
     *
     * @return Value for property 'additionalSetups'.
     */
    public LinkedList<Consumer<Fisher>> getAdditionalSetups() {
        return additionalSetups;
    }

    /**
     * Getter for property 'nextID'.
     *
     * @return Value for property 'nextID'.
     */
    public int getNextID() {
        return nextID;
    }

    /**
     * Setter for property 'nextID'.
     *
     * @param nextID Value to set for property 'nextID'.
     */
    public void setNextID(int nextID) {
        this.nextID = nextID;
    }
}

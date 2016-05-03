package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.Supplier;

/**
 * An object produced by the scenario that allows the model to produce more fishers
 * Created by carrknight on 12/11/15.
 */
public class FisherFactory implements AlgorithmFactory<Fisher>
{

    private int nextID;

    private Supplier<Port> portSupplier;

    private AlgorithmFactory<? extends Regulation> regulations;

    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy;

    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy;

    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy;

    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy;

    private Supplier<Boat> boatSupplier;

    private Supplier<Hold> holdSupplier;

    private AlgorithmFactory<? extends Gear> gear;


    private FisherFactory() {
    }

    public FisherFactory(
            Supplier<Port> portSupplier,
            AlgorithmFactory<? extends Regulation> regulations,
            AlgorithmFactory<? extends DepartingStrategy> departingStrategy,
            AlgorithmFactory<? extends DestinationStrategy> destinationStrategy,
            AlgorithmFactory<? extends FishingStrategy> fishingStrategy,
            AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy,
            Supplier<Boat> boatSupplier, Supplier<Hold> holdSupplier,
            AlgorithmFactory<? extends Gear> gear, int nextID) {
        this.portSupplier = portSupplier;
        this.regulations = regulations;
        this.departingStrategy = departingStrategy;
        this.destinationStrategy = destinationStrategy;
        this.fishingStrategy = fishingStrategy;
        this.weatherStrategy = weatherStrategy;
        this.boatSupplier = boatSupplier;
        this.holdSupplier = holdSupplier;
        this.gear = gear;
        this.nextID = nextID;
    }

    /**
     * Creates the fisher, add it to the social network and registers it as a startable
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public Fisher apply(FishState fishState) {
        Fisher fisher = new Fisher(nextID++,portSupplier.get(),
                                   fishState.getRandom(),
                                   regulations.apply(fishState),
                                   departingStrategy.apply(fishState),
                                   destinationStrategy.apply(fishState),
                                   fishingStrategy.apply(fishState),
                                   weatherStrategy.apply(fishState),
                                   boatSupplier.get(),
                                   holdSupplier.get(),
                                   gear.apply(fishState),
                                   fishState.getSpecies().size()
                                   );
        nextID++;
        fishState.getFishers().add(fisher);
        fishState.getSocialNetwork().addFisher(fisher,fishState);
        fishState.registerStartable(fisher);
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
}

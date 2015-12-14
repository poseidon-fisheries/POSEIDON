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

    private final Supplier<Port> portSupplier;

    private final AlgorithmFactory<? extends Regulation> regulations;

    private final AlgorithmFactory<? extends DepartingStrategy> departingStrategy;

    private final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy;

    private final AlgorithmFactory<? extends FishingStrategy> fishingStrategy;

    private final AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy;

    private final Supplier<Boat> boatSupplier;

    private final Supplier<Hold> holdSupplier;

    private final AlgorithmFactory<? extends Gear> gear;

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


}

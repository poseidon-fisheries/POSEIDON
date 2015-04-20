package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import com.sun.scenario.effect.impl.prism.PrCropPeer;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.strategies.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.DestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The boat catching all that delicious fish.
 * At its core it is a discrete-state automata: the Action class represents a possible state and the fisher can go through
 * one or more of them in a turn. <br>
 * Strategies are instead the fisher way to deal with decision points (should I go fish or not? Where do I go?)
 * Created by carrknight on 4/2/15.
 */
public class Fisher implements Steppable{

    /**
     * the location of the port!
     */
    private SeaTile location;

    /**
     *
     */
    private DepartingStrategy departingStrategy;


    /**
     * The strategy deciding where to go
     */
    private DestinationStrategy destinationStrategy;

    /**
     * Home is where the port is
     */
    final private Port homePort;

    /**
     * if it is moving somewhere, the destination is stored here.
     */
    private SeaTile destination;
    /**
     * randomizer
     */
    private final MersenneTwisterFast random;

    /**
     * the state of the fisher: the next action they are taking
     */
    private Action action;

    /**
     * boat statistics (also holds information about how much the boat has travelled so far)
     */
    private Boat boat;

    public Fisher(
            Port homePort, MersenneTwisterFast random, DepartingStrategy departingStrategy,
            DestinationStrategy destinationStrategy, Boat boat) {
        this.homePort = homePort; this.random = random;
        this.location = homePort.getLocation();
        this.destination = homePort.getLocation();
        homePort.dock(this);//we dock
        this.departingStrategy = departingStrategy;
        this.destinationStrategy =destinationStrategy;
        this.boat=boat;
        this.action = new AtPort();
    }





    public SeaTile getLocation()
    {
        return location;
    }


    public void start(FishState state)
    {
        state.schedule.scheduleRepeating(this);
    }

    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;

        //tell equipment!
        boat.newStep();

        //run the state machine
        while(true)
        {
            ActionResult result = action.act(model, this);
            action = result.getNextState();
            if(!result.isActAgainThisTurn())
                break;
        }


    }

    public MersenneTwisterFast getRandom() {
        return random;
    }


    public SeaTile getDestination() {
        return destination;
    }


    public Port getHomePort() {
        return homePort;
    }

    public Boat getBoat() {
        return boat;
    }

    /**
     * how much time it takes to travel this many kilometers
     * @param kilometersToTravel how many kilometers to move through
     * @return how many hours it takes to move "kilometersToTravel" (in hours)
     */
    public double hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(double kilometersToTravel) {
        return boat.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(kilometersToTravel);
    }

    /**
     * like hypotheticalTravelTimeToMoveThisMuchAtFullSpeed but adds to it the hours this boat has already travelled
     * @param segmentLengthInKilometers the length of the new step
     * @return current travel time + travel time of the new segment (in hours)
     */
    public double totalTravelTimeAfterAddingThisSegment(double segmentLengthInKilometers) {
        return boat.totalTravelTimeAfterAddingThisSegment(segmentLengthInKilometers);
    }

    /**
     * set new location, consume time and tell the map about our new location
     * @param newPosition the new position
     * @param map the map on which we are moving
     */
    public void move(SeaTile newPosition,NauticalMap map)
    {
        Preconditions.checkArgument(newPosition != location); //i am not already here!
        double distanceTravelled = map.distance(location, newPosition);
        boat.recordTravel(distanceTravelled); //tell the boat
        location = newPosition;
        map.recordFisherLocation(this,newPosition.getGridX(),newPosition.getGridY());

        Preconditions.checkState(boat.getHoursTravelledToday() <= FishState.HOURS_AVAILABLE_TO_TRAVEL_EACH_STEP);
        Preconditions.checkState(newPosition == location);
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     * @return true if the fisherman wants to leave port.
     * @param model the model
     */
    public boolean shouldFisherLeavePort(FishState model) {
        return departingStrategy.shouldFisherLeavePort(this, model);
    }

    /**
     * tell the fisher to check his destination and update it if necessary
     * @param model the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     * */
    public void updateDestination(FishState model, Action currentAction) {
        destination =  destinationStrategy.chooseDestination(this, random, model, currentAction);
        Preconditions.checkNotNull(destination, "Destination can never be null!");
    }

    public void setBoat(Boat boat) {
        this.boat = boat;
    }

    public double getHoursTravelledToday() {
        return boat.getHoursTravelledToday();
    }

    public void setDestinationStrategy(DestinationStrategy destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public void setDepartingStrategy(DepartingStrategy departingStrategy) {
        this.departingStrategy = departingStrategy;
    }
}

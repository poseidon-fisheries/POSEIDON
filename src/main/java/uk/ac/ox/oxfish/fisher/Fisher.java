package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Gear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.strategies.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.FishingStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.YearlyFisherDataGatherer;
import uk.ac.ox.oxfish.model.regs.Regulations;

import java.util.List;
import java.util.Map;

/**
 * The boat catching all that delicious fish.
 * At its core it is a discrete-state automata: the Action class represents a possible state and the fisher can go through
 * one or more of them in a turn. <br>
 * Strategies are instead the fisher way to deal with decision points (should I go fish or not? Where do I go?)
 * Created by carrknight on 4/2/15.
 */
public class Fisher implements Steppable{

    /***
     *     __   __        _      _    _
     *     \ \ / /_ _ _ _(_)__ _| |__| |___ ___
     *      \ V / _` | '_| / _` | '_ \ / -_|_-<
     *       \_/\__,_|_| |_\__,_|_.__/_\___/__/
     *
     */

    /**
     * the location of the port!
     */
    private SeaTile location;

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
     * the regulation object to obey
     */
    private Regulations regulations;

    /**
     * the state of the fisher: the next action they are taking
     */
    private Action action;

    /**
     * time spent at sea
     */
    private int stepsAtSea = 0;

    /**
     * the data gatherer that fires once a year
     */
    private final YearlyFisherDataGatherer yearlyDataGatherer = new YearlyFisherDataGatherer();

    /**
     * the cash owned by the firm
     */
    private double cash = 0;

    /***
     *      ___           _                    _
     *     | __|__ _ _  _(_)_ __ _ __  ___ _ _| |_
     *     | _|/ _` | || | | '_ \ '  \/ -_) ' \  _|
     *     |___\__, |\_,_|_| .__/_|_|_\___|_||_\__|
     *            |_|      |_|
     */

    /**
     * boat statistics (also holds information about how much the boat has travelled so far)
     */
    private Boat boat;

    /**
     * basically the inventory of the ship
     */
    private Hold hold;

    private Gear gear;

    /***
     *      ___ _            _            _
     *     / __| |_ _ _ __ _| |_ ___ __ _(_)___ ___
     *     \__ \  _| '_/ _` |  _/ -_) _` | / -_|_-<
     *     |___/\__|_| \__,_|\__\___\__, |_\___/__/
     *                              |___/
     */

    /**
     * the strategy deciding whether to leave port or not
     */
    private DepartingStrategy departingStrategy;


    /**
     * The strategy deciding where to go
     */
    private DestinationStrategy destinationStrategy;

    /**
     * the decision process of the fisher to choose whether to fish when arrived and for how long to do it
     */
    private FishingStrategy fishingStrategy;



    public Fisher(
            Port homePort, MersenneTwisterFast random,
            Regulations regulations,
            //strategies:
            DepartingStrategy departingStrategy,
            DestinationStrategy destinationStrategy, FishingStrategy fishingStrategy,
            //equipment:
            Boat boat, Hold hold, Gear gear) {
        this.homePort = homePort; this.random = random;
        this.location = homePort.getLocation();
        this.destination = homePort.getLocation();
        homePort.dock(this);//we dock
        this.departingStrategy = departingStrategy;
        this.destinationStrategy =destinationStrategy;
        this.boat=boat;
        this.hold = hold;
        this.action = new AtPort();
        this.gear = gear;
        this.fishingStrategy = fishingStrategy;
        this.regulations = regulations;
    }





    public SeaTile getLocation()
    {
        return location;
    }


    public void start(FishState state)
    {

        state.schedule.scheduleRepeating(this);
        yearlyDataGatherer.start(state,this);
    }

    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;

        //tell equipment!
        boat.newStep();

        //run the state machine
        while(true)
        {
            ActionResult result = action.act(model, this,regulations );
            action = result.getNextState();
            if(!result.isActAgainThisTurn())
                break;
        }

        //if you are not at home
        if(!location.equals(getHomePort().getLocation()))
            stepsAtSea++;

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
     * tell the fisher to check his destination and update it if necessary. If the regulations forbid us to be at sea
     * the destination is always port
     * @param model the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     * */
    public void updateDestination(FishState model, Action currentAction) {

        if(!regulations.allowedAtSea(this, model))
            destination = homePort.getLocation();
        else
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


    public FishingStrategy getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(FishingStrategy fishingStrategy) {
        this.fishingStrategy = fishingStrategy;
    }

    public boolean shouldIFish(FishState state)
    {
        return fishingStrategy.shouldFish(this,random,state);

    }



    /**
     * store the catch
     * @param caught the catch
     */
    public void load(Catch caught) {
        hold.load(caught);
    }

    /**
     * how many pounds of a specific specie are we carrying
     * @param specie the specie
     * @return lbs of specie carried
     */
    public double getPoundsCarried(Specie specie) {
        return hold.getPoundsCarried(specie);
    }

    /**
     * the total pounds of fish carried
     * @return pounds carried
     */
    public double getPoundsCarried() {
        return hold.getPoundsCarried();
    }

    /**
     * how much can this fish hold
     * @return the maximum load
     */
    public double getMaximumLoad() {
        return hold.getMaximumLoad();
    }

    /**
     * unload all the cargo
     * @return the cargo as a catch object
     */
    public Catch unload() {
        return hold.unload();
    }




    /**
     * tell the fisher to use its gear to fish at current location. It stores everything in the hold
     * @param modelBiology the global biology object
     * @return the fish caught and stored (barring overcapacity)
     */
    public Catch fishHere(GlobalBiology modelBiology) {
        Preconditions.checkState(location.getAltitude() < 0, "can't fish on land!");
        Catch catchOfTheDay = gear.fish(this, location, modelBiology);
        regulations.reactToCatch(catchOfTheDay);
        hold.load(catchOfTheDay);
        return catchOfTheDay;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(Gear gear) {
        this.gear = gear;
    }

    /**
     *
     * @return true if destination == location
     */
    public boolean isAtDestination()
    {
        return destination.equals(location);
    }

    public Regulations getRegulations() {
        return regulations;
    }

    public void setRegulations(Regulations regulations) {
        this.regulations = regulations;
    }


    public Map<String, List<Double>> getYearlyData() {
        return yearlyDataGatherer.getDataView();
    }

    public double getCash(){
        return cash;
    };

    public void earn(double moneyEarned)
    {
        cash += moneyEarned;
    }

    public void spend(double moneySpent)
    {
        cash -=moneySpent;
    }
}

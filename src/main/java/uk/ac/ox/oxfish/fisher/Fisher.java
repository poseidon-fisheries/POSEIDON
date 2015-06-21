package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Gear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripLogger;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.DataSet;
import uk.ac.ox.oxfish.model.data.YearlyFisherDataSet;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.List;

/**
 * The boat catching all that delicious fish.
 * At its core it is a discrete-state automata: the Action class represents a possible state and the fisher can go through
 * one or more of them in a turn. <br>
 * Strategies are instead the fisher way to deal with decision points (should I go fish or not? Where do I go?)
 * Created by carrknight on 4/2/15.
 */
public class Fisher implements Steppable, Startable{

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
    private Regulation regulation;

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
    private final YearlyFisherDataSet yearlyDataGatherer = new YearlyFisherDataSet();


    /**
     * a link to the model. Got when start() is called. It's not used or shared except when a new strategy is plugged in
     * at which point this reference is used to call their start
     */
    private FishState state;

    /**
     * stores trip information
     */
    private TripLogger tripLogger = new TripLogger();


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

    /**
     * what is used for fishing
     */
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

    /**
     * the stop switch to call when the fisher is turned off
     */
    private Stoppable receipt;


    public Fisher(
            Port homePort, MersenneTwisterFast random,
            Regulation regulation,
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
        this.regulation = regulation;
    }





    public SeaTile getLocation()
    {
        return location;
    }


    public void start(FishState state)
    {

        this.state = state;
        receipt = state.schedule.scheduleRepeating(this);
        yearlyDataGatherer.start(state,this);
        tripLogger.start(state);

        //start the strategies
        destinationStrategy.start(state);
        fishingStrategy.start(state);
        departingStrategy.start(state);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
        yearlyDataGatherer.stop();
        tripLogger.turnOff();

        //start the strategies
        destinationStrategy.turnOff();
        fishingStrategy.turnOff();
        departingStrategy.turnOff();
    }

    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;

        //tell equipment!
        boat.newStep();

        //run the state machine
        double hoursLeft = model.getHoursPerStep();
        while(true)
        {
            ActionResult result = action.act(model, this, regulation,hoursLeft);
            action = result.getNextState();
            hoursLeft = result.getHoursLeft();
            //should be rounded anyway
            if(hoursLeft <= 0)
            {
                assert  Math.abs(hoursLeft)<.001; //shouldn't be negative!
                break;
            }
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
    public void move(SeaTile newPosition,NauticalMap map, FishState state)
    {
        Preconditions.checkArgument(newPosition != location); //i am not already here!
        double distanceTravelled = map.distance(location, newPosition);
        boat.recordTravel(distanceTravelled); //tell the boat
        location = newPosition;
        map.recordFisherLocation(this,newPosition.getGridX(),newPosition.getGridY());

        //this condition doesn't hold anymore because time travelled is "conserved" between steps
     //   Preconditions.checkState(boat.getHoursTravelledToday() <= state.getHoursPerStep(), boat.getHoursTravelledToday() +  " and ");
        Preconditions.checkState(newPosition == location);
    }

    /**
     * departs
     */
    public void undock() {
        assert this.stepsAtSea == 0;
        assert this.getLocation().equals(homePort.getLocation());
        homePort.depart(this);
        tripLogger.newTrip();
    }

    /**
     * anchors at home-port and sets the trip to "over"
     */
    public void dock(){
        assert this.getLocation().equals(homePort.getLocation());
        homePort.dock(this);
        tripLogger.finishTrip(stepsAtSea);

        stepsAtSea = 0;
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
     * tell the fisher to check his destination and update it if necessary. If the regulation forbid us to be at sea
     * the destination is always port
     * @param model the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     * */
    public void updateDestination(FishState model, Action currentAction) {

        if(!regulation.allowedAtSea(this, model))
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

    public void setDestinationStrategy(DestinationStrategy newStrategy) {
        this.destinationStrategy = newStrategy;
        if(state != null) //if we have started already
        {
            newStrategy.turnOff(); //turn off old strategy
            destinationStrategy.start(state);
        }
    }

    public void setDepartingStrategy(DepartingStrategy newStrategy) {
        this.departingStrategy = newStrategy;
        if(state != null) //if we have started already
        {
            newStrategy.turnOff(); //turn off old strategy
            departingStrategy.start(state);
        }
    }

    public DepartingStrategy getDepartingStrategy() {
        return departingStrategy;
    }

    public DestinationStrategy getDestinationStrategy() {
        return destinationStrategy;
    }

    public FishingStrategy getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(FishingStrategy newStrategy) {

        this.fishingStrategy = newStrategy;
        if(state != null) //if we have started already
        {
            newStrategy.turnOff(); //turn off old strategy
            fishingStrategy.start(state);
        }
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
     * @param hoursSpentFishing
     * @return the fish caught and stored (barring overcapacity)
     */
    public Catch fishHere(GlobalBiology modelBiology, double hoursSpentFishing) {
        Preconditions.checkState(location.getAltitude() < 0, "can't fish on land!");
        Catch catchOfTheDay = gear.fish(this, location,hoursSpentFishing , modelBiology);
        regulation.reactToCatch(catchOfTheDay);
        load(catchOfTheDay);
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

    public Regulation getRegulation() {
        return regulation;
    }

    public void setRegulation(Regulation regulation) {
        this.regulation = regulation;
    }


    public DataSet getYearlyData() {
        return yearlyDataGatherer;

    }

    /**
     * shortcut for getYearlyData().getLatestObservation(columnName)
     */
    public double getLatestYearlyObservation(String columnName) {
        return yearlyDataGatherer.getLatestObservation(columnName);
    }

    public double getCash(){
        return cash;
    };

    public void earn(double moneyEarned)
    {
        cash += moneyEarned;
        tripLogger.recordEarnings(moneyEarned);
    }

    public void spend(double moneySpent)
    {
        cash -=moneySpent;
        tripLogger.recordCosts(moneySpent);

    }



    public int getStepsAtSea() {
        return stepsAtSea;
    }

    public TripRecord getCurrentTrip() {
        return tripLogger.getCurrentTrip();
    }

    public void addTripListener(TripListener listener) {
        tripLogger.addTripListener(listener);
    }

    public void removeTripListener(TripListener listener) {
        tripLogger.removeTripListener(listener);
    }

    public void recordTripCutShort() {
        tripLogger.recordTripCutShort();
    }

    public void recordEarnings(double newEarnings) {
        tripLogger.recordEarnings(newEarnings);
    }

    public void recordCosts(double newCosts) {
        tripLogger.recordCosts(newCosts);
    }

    public List<TripRecord> getAllTrips() {
        return tripLogger.getAllTrips();
    }

    public String getAction() {
        return action.getClass().getSimpleName();
    }
}

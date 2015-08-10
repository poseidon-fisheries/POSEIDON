package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.jfree.chart.resources.JFreeChartResources;
import org.metawidget.inspector.annotation.UiHidden;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.*;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.*;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.maximization.Adaptation;
import uk.ac.ox.oxfish.utility.maximization.AdaptationDailyScheduler;
import uk.ac.ox.oxfish.utility.maximization.AdaptationPerTripScheduler;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * The boat catching all that delicious fish.
 * At its core it is a discrete-state automata: the Action class represents a possible state and the fisher can go through
 * each of them in a turn. <br>
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

    private final int fisherID;

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
    @UiHidden
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
     * hours spent away from port
     */
    private double hoursAtSea = 0;

    /**
     * the data gatherer that fires once a year
     */
    private final YearlyFisherDataSet yearlyDataGatherer = new YearlyFisherDataSet();

    /**
     * the data gatherer that fires every day
     */
    private final DailyFisherDataSet dailyFisherDataSet = new DailyFisherDataSet();

    private final Counter yearlyCounter = new Counter(IntervalPolicy.EVERY_YEAR);

    private final LocationMemories<Catch> catchMemories = new LocationMemories<>(.99,300,2);

    private final LocationMemories<TripRecord> tripMemories = new LocationMemories<>(.99,300,2);

    /**
     * a link to the model. Got when start() is called. It's not used or shared except when a new strategy is plugged in
     * at which point this reference is used to call their start
     */
    private FishState state;

    /**
     * stores trip information
     */
    private final TripLogger tripLogger = new TripLogger();


    /**
     * the cash owned by the firm
     */
    private double bankBalance = 0;

    private SocialNetwork network;


    /**
     * when this flag is on, the agent believes that it MUST return home or it will run out of fuel. All other usual
     * decisions about destination are ignored.
     */
    private boolean fuelEmergencyOverride = false;

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
     * the turnOff switch to call when the fisher is turned off
     */
    private Stoppable receipt;


    /**
     * collection of adaptation algorithms to fire every 2 months
     */
    private final AdaptationDailyScheduler bimonthlyAdaptation = new AdaptationDailyScheduler(60);

    /**
     * collection of adaptation algorithms to fire every 360  days
     */
    private final AdaptationDailyScheduler yearlyAdaptation = new AdaptationDailyScheduler(360);

    /**
     * collection of adaptation algorithms to fire every trip
     */
    private final AdaptationPerTripScheduler tripAdaptation = new AdaptationPerTripScheduler();


    /**
     * Creates a fisher by giving it all its sub-components
     * @param id the id-number of the fisher
     * @param homePort the home port
     * @param random a randomizer
     * @param regulation the rules the fisher follows
     * @param departingStrategy how the fisher decides how to leave the port
     * @param destinationStrategy how the fisher decides where to go
     * @param fishingStrategy how the fisher decides how much to fish
     * @param boat the boat the fisher uses
     * @param hold the space available to load fish
     * @param gear what is used for fishing
\     */
    public Fisher(
            int id,
            Port homePort, MersenneTwisterFast random,
            Regulation regulation,
            //strategies:
            DepartingStrategy departingStrategy,
            DestinationStrategy destinationStrategy, FishingStrategy fishingStrategy,
            //equipment:
            Boat boat, Hold hold, Gear gear) {
        this.fisherID = id;
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
        this.yearlyCounter.addColumn(YearlyFisherDataSet.FUEL_CONSUMPTION);
    }





    public SeaTile getLocation()
    {
        return location;
    }


    public void start(FishState state)
    {

        this.state = state;
        this.network = state.getSocialNetwork();
        receipt = state.scheduleEveryStep(this, StepOrder.FISHER_PHASE);


        //start datas
        yearlyDataGatherer.start(state, this);
        yearlyCounter.start(state);
        dailyFisherDataSet.start(state, this);
        tripLogger.start(state);
        catchMemories.start(state);
        tripMemories.start(state);
        //trip memories need to listen to trip logger
        tripLogger.addTripListener(new TripListener() {
            @Override
            public void reactToFinishedTrip(TripRecord record) {
                SeaTile mostFishedTileInTrip = record.getMostFishedTileInTrip();
                if (mostFishedTileInTrip != null)
                    tripMemories.memorize(record, mostFishedTileInTrip);
            }
        });



        //start the strategies
        destinationStrategy.start(state);
        fishingStrategy.start(state);
        departingStrategy.start(state);

        //start the adaptations
        bimonthlyAdaptation.start(state,this);
        yearlyAdaptation.start(state,this);
        tripAdaptation.start(state,this);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
        yearlyDataGatherer.turnOff();
        dailyFisherDataSet.turnOff();
        tripLogger.turnOff();
        catchMemories.turnOff();
        tripMemories.turnOff();

        //turn off the strategies
        destinationStrategy.turnOff();
        fishingStrategy.turnOff();
        departingStrategy.turnOff();

        //turn off the adaptations
        bimonthlyAdaptation.turnOff();
        yearlyAdaptation.turnOff();
        tripAdaptation.turnOff();
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
            //pre-action accounting
            updateFuelEmergencyFlag(model.getMap());
            double hoursLeftBeforeAction = hoursLeft;

            //take an action
            ActionResult result = action.act(model, this, regulation,hoursLeft);
            hoursLeft = result.getHoursLeft();
            //if you have been moving or you were staying still somewhere away from port
            if(action instanceof Moving || !isAtPort())
                increaseHoursAtSea(hoursLeftBeforeAction-hoursLeft);

            //set up next action
            action = result.getNextState();


            //if you are out of time, continue tomorrow
            if(hoursLeft <= 0)
            {
                assert  Math.abs(hoursLeft)<.001; //shouldn't be negative!
                break;
            }
        }



    }

    /**
     * weird name to avoid beans
     */
    public MersenneTwisterFast grabRandomizer() {
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
        //consume gas!
        consumeFuel(boat.expectedFuelConsumption(distanceTravelled));
        //arrive at new position
        location = newPosition;
        map.recordFisherLocation(this,newPosition.getGridX(),newPosition.getGridY());

        //this condition doesn't hold anymore because time travelled is "conserved" between steps
     //   Preconditions.checkState(boat.getHoursTravelledToday() <= state.getHoursPerStep(), boat.getHoursTravelledToday() +  " and ");
        Preconditions.checkState(newPosition == location);
    }


    public void consumeFuel(double litersConsumed)
    {
        boat.consumeFuel(litersConsumed);
        yearlyCounter.count(YearlyFisherDataSet.FUEL_CONSUMPTION, litersConsumed);
    }

    /**
     * departs
     */
    public void undock() {
        assert this.hoursAtSea == 0;
        assert isAtPort();
        homePort.depart(this);
        tripLogger.newTrip();
    }

    public boolean isAtPort() {
        return this.getLocation().equals(homePort.getLocation());
    }

    /**
     * anchors at home-port and sets the trip to "over"
     */
    public void dock(){
        assert isAtPort();
        homePort.dock(this);
        //when you dock you also refill
        final double litersBought = boat.refill();
        fuelEmergencyOverride = false;
        //now pay for it
        spend(litersBought*homePort.getGasPricePerLiter());

        //finish trip!
        tripLogger.finishTrip(hoursAtSea);

        hoursAtSea = 0;
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


        //if you are not allowed at sea or you are running out of gas, go home
        if(!regulation.allowedAtSea(this, model) || fuelEmergencyOverride)
            destination = homePort.getLocation();
        else
            destination =  destinationStrategy.chooseDestination(this, random, model, currentAction);
        Preconditions.checkNotNull(destination, "Destination can never be null!");
    }

    /**
     * called to check if there is so little fuel we must go back home
     */
    private void updateFuelEmergencyFlag(NauticalMap map)
    {

        if(!fuelEmergencyOverride)
            fuelEmergencyOverride = ! boat.isFuelEnoughForTrip(map.distance(location,getHomePort().getLocation()),1.05);


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
        return !fuelEmergencyOverride && fishingStrategy.shouldFish(this,random,state);

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
        return hold.getTonnesCarried();
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
     * @param hoursSpentFishing hours spent on this activity
     * @param state
     * @return the fish caught and stored (barring overcapacity)
     */
    public Catch fishHere(GlobalBiology modelBiology, double hoursSpentFishing, FishState state)
    {
        Preconditions.checkState(location.getAltitude() < 0, "can't fish on land!");

        //catch fish
        Catch catchOfTheDay = gear.fish(this, location,hoursSpentFishing , modelBiology);

        //record it
        FishingRecord record = new FishingRecord(hoursSpentFishing,gear,location,catchOfTheDay,this,
                                                 state.getStep());
        getCurrentTrip().recordFishing(record);
        catchMemories.memorize(catchOfTheDay,location);

        //now let regulations and hold deal with it
        regulation.reactToCatch(catchOfTheDay);
        load(catchOfTheDay);

        //consume gas
        final double litersBurned = gear.getFuelConsumptionPerHourOfFishing(this, getBoat(), location) * hoursSpentFishing;
        consumeFuel(litersBurned);



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

    public DailyFisherDataSet getDailyData() {
        return dailyFisherDataSet;
    }

    /**
     * shortcut for getYearlyData().getLatestObservation(columnName)
     */
    public double getLatestYearlyObservation(String columnName) {
        return yearlyDataGatherer.getLatestObservation(columnName);
    }

    public double getBankBalance(){
        return bankBalance;
    }

    public void earn(double moneyEarned)
    {
        bankBalance += moneyEarned;
        tripLogger.recordEarnings(moneyEarned);
    }

    public void spend(double moneySpent)
    {
        bankBalance -=moneySpent;
        tripLogger.recordCosts(moneySpent);

    }



    public double balanceXDaysAgo(int daysAgo)
    {
    //    Preconditions.checkArgument(dailyFisherDataSet.numberOfObservations() >daysAgo);
        return getDailyData().getColumn(YearlyFisherDataSet.CASH_COLUMN).getDatumXDaysAgo(daysAgo);
    }


    public void registerYearlyAdaptation(Adaptation a)
    {
        yearlyAdaptation.registerAdaptation(a);
    }

    public void registerBiMonthlyAdaptation(Adaptation a)
    {
        bimonthlyAdaptation.registerAdaptation(a);
    }

    public void registerPerTripAdaptation(Adaptation a)
    {
        tripAdaptation.registerAdaptation(a);
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

    public List<TripRecord> getFinishedTrips() {
        return tripLogger.getFinishedTrips();
    }

    public String getAction() {
        return action.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "Fisher " + fisherID;
    }

    /**
     * return all neighbors of this agent in the social network ignoring the direction of the edges
     */
    public Collection<Fisher> getAllFriends()
    {
        return network.getAllNeighbors(this);
    }

    /**
     * return all neighbors of this agent where there a directed edge from this fisher to his neighbors
     * @return a collection of agents
     */
    public Collection<Fisher> getDirectedFriends()
    {
        return network.getDirectedNeighbors(this);
    }

    public TripRecord getLastFinishedTrip() {
        return tripLogger.getLastFinishedTrip();
    }

    public double getYearlyCounterColumn(String columnName) {
        return yearlyCounter.getColumn(columnName);
    }

    public boolean isFuelEmergencyOverride() {
        return fuelEmergencyOverride;
    }

    public double getFuelLeft() {
        return boat.getLitersOfFuelInTank();
    }

    public int getID() {
        return fisherID;
    }


    /**
     * Ask the fisher what is the best tile with respect to catches made
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForCatchesRemembered(
            Comparator<LocationMemory<Catch>> comparator) {
        return catchMemories.getBestFishingSpotInMemory(comparator);
    }

    /**
     * Ask the fisher what is the best tile with respect to trips made
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForTripsRemembered(
            Comparator<LocationMemory<TripRecord>> comparator) {
        return tripMemories.getBestFishingSpotInMemory(comparator);
    }


    public double getHoursAtSea() {
        return hoursAtSea;
    }

    private void increaseHoursAtSea(double hoursIncrease)
    {
        Preconditions.checkArgument(hoursIncrease >= 0);
        hoursAtSea += hoursIncrease;
    }


    public boolean isGoingToPort()
    {
        return getDestination().equals(getHomePort().getLocation());
    }

}

package uk.ac.ox.oxfish.fisher;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.metawidget.inspector.annotation.UiHidden;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractors;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.LocationMemory;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.FixedPredictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.Predictor;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.DailyFisherTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyCounter;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.TradeInfo;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.AdaptationDailyScheduler;
import uk.ac.ox.oxfish.utility.adaptation.AdaptationPerTripScheduler;

import java.util.*;

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

    /**
     * the id of the fisher. Hopefully unique
     */
    private final int fisherID;



    /**
     * contains most transitory variables related to the fisher
     */
    private final FisherStatus status;

    /**
     * a container of all the long term information that the fisher stores
     */
    private final FisherMemory memory;

    /**
     * a link to the model. Grabbed when start() is called. It's not used or shared except when a new strategy is plugged in
     * at which point this reference is used to call the strategy's start method
     */
    private FishState state;




    /***
     *      ___           _                    _
     *     | __|__ _ _  _(_)_ __ _ __  ___ _ _| |_
     *     | _|/ _` | || | | '_ \ '  \/ -_) ' \  _|
     *     |___\__, |\_,_|_| .__/_|_|_\___|_||_\__|
     *            |_|      |_|
     */

    /**
     * contains all the equipment variables (gear, boat,hold)
     */
    private final FisherEquipment equipment;

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
     * the check the agent makes to decide whether it's time to go back home given the weather
     */
    private WeatherEmergencyStrategy weatherStrategy;


    /**
     * the turnOff switch to call when the fisher is turned off
     */
    private Stoppable receipt;


    /**
     * collection of adaptation algorithms to fire every 2 months
     */
    private final AdaptationDailyScheduler bimonthlyAdaptation = new AdaptationDailyScheduler(60);

    /**
     * collection of adaptation algorithms to fire every 365  days
     */
    private final AdaptationDailyScheduler yearlyAdaptation = new AdaptationDailyScheduler(365);

    /**
     * collection of adaptation algorithms to fire every trip
     */
    private final AdaptationPerTripScheduler tripAdaptation = new AdaptationPerTripScheduler();



    private Predictor[] dailyCatchesPredictor;
            /*MovingAveragePredictor.dailyMAPredictor("Predicted Daily Catches",
                                                                                            fisher -> fisher.getDailyCounter().getLandingsPerSpecie(0),
                                                                                            90);
                                                                                            */

    private Predictor[] profitPerUnitPredictor;
            /*
            MovingAveragePredictor.perTripMAPredictor("Predicted Unit Profit",
                                                                                               fisher -> fisher.getLastFinishedTrip().getUnitProfitPerSpecie(
                                                                                                       0),
                                                                                               30);
                                                                                               */


    private Predictor dailyProfitsPredictor = new FixedPredictor(Double.NaN);
            /*
            MovingAveragePredictor.dailyMAPredictor("Predicted Daily Profits",
                                                                                      fisher ->
                                                                                              fisher.getDailyData().
                                                                                                      getColumn(
                                                                                                              YearlyFisherTimeSeries.CASH_FLOW_COLUMN).getLatest(),
                                                                                      365);
*/


    /**
     * Creates a fisher by giving it all its sub-components
     * @param id the id-number of the fisher
     * @param homePort the home port
     * @param random a randomizer
     * @param regulation the rules the fisher follows
     * @param departingStrategy how the fisher decides how to leave the port
     * @param destinationStrategy how the fisher decides where to go
     * @param fishingStrategy how the fisher decides how much to fish
     * @param weatherStrategy
     * @param boat the boat the fisher uses
     * @param hold the space available to load fish
     * @param gear what is used for fishing
     * @param numberOfSpecies
     */
    public Fisher(
            int id,
            Port homePort, MersenneTwisterFast random,
            Regulation regulation,
            //strategies:
            DepartingStrategy departingStrategy,
            DestinationStrategy destinationStrategy,
            FishingStrategy fishingStrategy,
            WeatherEmergencyStrategy weatherStrategy,
            //equipment:
             Boat boat, Hold hold, Gear gear,
            int numberOfSpecies) {
        this.fisherID = id;

        //set up variables
        this.status = new FisherStatus(random, regulation,homePort);
        this.equipment = new FisherEquipment(boat,hold,gear);
        this.memory = new FisherMemory();

        homePort.dock(this);//we dock
        //strategies
        this.departingStrategy = departingStrategy;
        this.destinationStrategy =destinationStrategy;
        this.fishingStrategy = fishingStrategy;
        this.weatherStrategy = weatherStrategy;


        //predictors
        dailyCatchesPredictor = new Predictor[numberOfSpecies];
        profitPerUnitPredictor = new Predictor[numberOfSpecies];
        for(int i=0; i<dailyCatchesPredictor.length; i++)
        {
            dailyCatchesPredictor[i] = new FixedPredictor(Double.NaN);
            profitPerUnitPredictor[i] = new FixedPredictor(Double.NaN);



        }

    }





    public SeaTile getLocation()
    {
        return status.getLocation();
    }


    public void start(FishState state)
    {

        this.state = state;
        this.status.setNetwork(state.getSocialNetwork());
        receipt = state.scheduleEveryStep(this, StepOrder.FISHER_PHASE);


        //start datas
        memory.start(state, this);



        //start the strategies
        destinationStrategy.start(state,this);
        fishingStrategy.start(state,this);
        departingStrategy.start(state,this);
        weatherStrategy.start(state,this);

        //start the adaptations
        bimonthlyAdaptation.start(state,this);
        yearlyAdaptation.start(state, this);
        tripAdaptation.start(state, this);

        //start the predictors
        for(int i=0; i<dailyCatchesPredictor.length; i++)
        {
            dailyCatchesPredictor[i].start(state, this);
            profitPerUnitPredictor[i].start(state,this);

        }
        dailyProfitsPredictor.start(state,this);


    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
        memory.getYearlyTimeSeries().turnOff();
        memory.getDailyCounter().turnOff();
        memory.getYearlyCounter().turnOff();
        memory.getDailyTimeSeries().turnOff();
        memory.getTripLogger().turnOff();
        memory.getCatchMemories().turnOff();
        memory.getTripMemories().turnOff();

        //turn off the strategies
        destinationStrategy.turnOff();
        fishingStrategy.turnOff();
        departingStrategy.turnOff();
        weatherStrategy.turnOff();

        //turn off the adaptations
        bimonthlyAdaptation.turnOff();
        yearlyAdaptation.turnOff();
        tripAdaptation.turnOff();
    }

    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;

        if(Log.TRACE) {
            Log.trace("Fisher " + fisherID + " is going to start his step");
        }

        //tell equipment!
        equipment.getBoat().newStep();

        //run the state machine
        double hoursLeft = model.getHoursPerStep();
        while(true)
        {
            //pre-action accounting
            updateFuelEmergencyFlag(model.getMap());
            double hoursLeftBeforeAction = hoursLeft;

            //take an action
            ActionResult result = status.getAction().act(model, this, status.getRegulation(), hoursLeft);
            hoursLeft = result.getHoursLeft();
            //if you have been moving or you were staying still somewhere away from port
            if(status.getAction() instanceof Moving || !isAtPort())
                increaseHoursAtSea(hoursLeftBeforeAction-hoursLeft);
            else
                increaseHoursAtPort(hoursLeftBeforeAction-hoursLeft);

            //set up next action
            status.setAction(result.getNextState());


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
        return status.getRandom();
    }


    public SeaTile getDestination() {
        return status.getDestination();
    }


    public Port getHomePort() {
        return status.getHomePort();
    }

    public Boat getBoat() {
        return equipment.getBoat();
    }

    public void setHold(Hold hold) {
        equipment.setHold(hold);
    }

    /**
     * how much time it takes to travel this many kilometers
     * @param kilometersToTravel how many kilometers to move through
     * @return how many hours it takes to move "kilometersToTravel" (in hours)
     */
    public double hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(double kilometersToTravel) {
        return equipment.getBoat().hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(kilometersToTravel);
    }

    /**
     * like hypotheticalTravelTimeToMoveThisMuchAtFullSpeed but adds to it the hours this boat has already travelled
     * @param segmentLengthInKilometers the length of the new step
     * @return current travel time + travel time of the new segment (in hours)
     */
    public double totalTravelTimeAfterAddingThisSegment(double segmentLengthInKilometers) {
        return equipment.getBoat().totalTravelTimeAfterAddingThisSegment(segmentLengthInKilometers);
    }

    /**
     * set new location, consume time and tell the map about our new location
     * @param newPosition the new position
     * @param map the map on which we are moving
     * @param distanceTravelled
     */
    public void move(SeaTile newPosition, NauticalMap map, FishState state, double distanceTravelled)
    {
        Preconditions.checkArgument(newPosition != status.getLocation()); //i am not already here!
        Preconditions.checkArgument(distanceTravelled > 0); //i am not already here!
        equipment.getBoat().recordTravel(distanceTravelled); //tell the boat
        //consume gas!
        consumeFuel(equipment.getBoat().expectedFuelConsumption(distanceTravelled));
        //arrive at new position
        status.setLocation(newPosition);
        map.recordFisherLocation(this,newPosition.getGridX(),newPosition.getGridY());

        //this condition doesn't hold anymore because time travelled is "conserved" between steps
        //   Preconditions.checkState(boat.getHoursTravelledToday() <= state.getHoursPerStep(), boat.getHoursTravelledToday() +  " and ");
        Preconditions.checkState(newPosition == status.getLocation());
    }


    public void consumeFuel(double litersConsumed)
    {
        equipment.getBoat().consumeFuel(litersConsumed);
        Preconditions.checkState(equipment.getBoat().getFuelCapacityInLiters()>=0 || isFuelEmergencyOverride(),
                                 "a boat has run into negative fuel territory");
        memory.getYearlyCounter().count(YearlyFisherTimeSeries.FUEL_CONSUMPTION, litersConsumed);
    }

    /**
     * departs
     */
    public void undock() {
        assert this.status.getHoursAtSea() == 0;
        assert isAtPort();
        status.getHomePort().depart(this);
        memory.getTripLogger().newTrip(getHoursAtPort());
        status.setHoursAtPort(0);
    }

    public boolean isAtPort() {
        return this.status.isAtPort();
    }


    private LinkedList<DockingListener> dockingListeners = new LinkedList<>();
    public void addDockingListener(DockingListener listener){
        dockingListeners.add(listener);
    }
    public void removeDockingListener(DockingListener listener)
    {
        dockingListeners.remove(listener);
    }


    /**
     * anchors at home-port and sets the trip to "over"
     */
    public void dock(){
        assert isAtPort();
        assert status.getHoursAtPort() == 0;
        status.getHomePort().dock(this);
        //when you dock you also refill
        final double litersBought = equipment.getBoat().refill();
        status.setFuelEmergencyOverride(false);
        //now pay for it
        double gasExpenditure = litersBought * status.getHomePort().getGasPricePerLiter();
        spendForTrip(gasExpenditure);
        memory.getYearlyCounter().count(YearlyFisherTimeSeries.FUEL_EXPENDITURE, gasExpenditure);
        if(status.getHoursAtSea()>0) //if you have been somewhere at all
            memory.getYearlyCounter().count(YearlyFisherTimeSeries.TRIPS,1);


        //notify listeners
        for(DockingListener listener : dockingListeners)
        {
            listener.dockingEvent(this,getHomePort());
        }

        //finish trip!
        memory.getTripLogger().finishTrip(status.getHoursAtSea(), getHomePort() );


        status.setHoursAtSea(0);
        assert  isAtPort();

    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     * @return true if the fisherman wants to leave port.
     * @param model the model
     */
    public boolean shouldFisherLeavePort(FishState model) {
        assert isAtPort();
        assert !isFuelEmergencyOverride();
        return !status.isAnyEmergencyFlagOn() && departingStrategy.shouldFisherLeavePort(equipment,status,memory , model);
    }

    /**
     * tell the fisher to check his destination and update it if necessary. If the regulation forbid us to be at sea
     * the destination is always port
     * @param model the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     * */
    public void updateDestination(FishState model, Action currentAction) {


        //if you are not allowed at sea or you are running out of gas, go home
        if(!status.getRegulation().allowedAtSea(this, model) || status.isAnyEmergencyFlagOn())
            status.setDestination(status.getHomePort().getLocation());
        else
            status.setDestination(
                    destinationStrategy.chooseDestination(equipment,status,memory , status.getRandom(), model, currentAction));
        Preconditions.checkNotNull(status.getDestination(), "Destination can never be null!");
    }

    /**
     * called to check if there is so little fuel we must go back home
     */
    private void updateFuelEmergencyFlag(NauticalMap map)
    {

        if(!status.isFuelEmergencyOverride())
            status.setFuelEmergencyOverride(!equipment.getBoat().isFuelEnoughForTrip(
                    map.distance(status.getLocation(), getHomePort().getLocation()), 1.2));

        status.setWeatherEmergencyOverride(weatherStrategy.updateWeatherEmergencyFlag(status.isWeatherEmergencyOverride(),
                                                                                      this,
                                                                                      getLocation()));

    }

    public boolean isAllowedAtSea()
    {
        if(state == null) //you aren't allowed if you haven't started
            return false;
        else
            return getRegulation().allowedAtSea(this,state);
    }

    public void setBoat(Boat boat) {
        equipment.setBoat(boat);
    }

    public double getHoursTravelledToday() {
        return equipment.getBoat().getHoursTravelledToday();
    }

    public void setDestinationStrategy(DestinationStrategy newStrategy) {

        DestinationStrategy old = this.destinationStrategy;
        this.destinationStrategy = newStrategy;

        if(state != null) //if we have started already
        {
            old.turnOff(); //turn off old strategy
            destinationStrategy.start(state,this);
        }
    }

    public void setDepartingStrategy(DepartingStrategy newStrategy) {
        DepartingStrategy old = this.departingStrategy;
        this.departingStrategy = newStrategy;
        if(state != null) //if we have started already
        {
            old.turnOff(); //turn off old strategy
            departingStrategy.start(state,this);
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

        FishingStrategy old = this.fishingStrategy;
        this.fishingStrategy = newStrategy;
        if(state != null) //if we have started already
        {
            old.turnOff(); //turn off old strategy
            fishingStrategy.start(state,this);
        }
    }

    public boolean shouldIFish(FishState state)
    {
        return !status.isAnyEmergencyFlagOn() && fishingStrategy.shouldFish(equipment,
                                                                            status,
                                                                            memory,
                                                                            grabRandomizer(),
                                                                            state);

    }



    /**
     * store the catch
     * @param caught the catch
     */
    public void load(Catch caught) {
        equipment.getHold().load(caught);
    }

    /**
     * how many pounds of a specific species are we carrying
     * @param species the species
     * @return lbs of species carried
     */
    public double getPoundsCarried(Species species) {
        return equipment.getHold().getPoundsCarried(species);
    }

    /**
     * the total pounds of fish carried
     * @return pounds carried
     */
    public double getPoundsCarried() {
        return equipment.getHold().getTotalPoundsCarried();
    }

    /**
     * how much can this fish hold
     * @return the maximum load
     */
    public double getMaximumHold() {
        return equipment.getHold().getMaximumLoad();
    }

    /**
     * unload all the cargo
     * @return the cargo as a catch object
     */
    public Catch unload() {
        return equipment.getHold().unload();
    }




    /**
     * tell the fisher to use its gear to fish at current location. It stores everything in the hold
     * @param modelBiology the global biology object
     * @param hoursSpentFishing hours spent on this activity
     * @param state the model
     * @return the fish caught and stored (barring overcapacity)
     */
    public Catch fishHere(GlobalBiology modelBiology, int hoursSpentFishing, FishState state)
    {
        Preconditions.checkState(status.getLocation().getAltitude() < 0, "can't fish on land!");

        //catch fish
        Catch catchOfTheDay = equipment.getGear().fish(this, status.getLocation(), hoursSpentFishing, modelBiology);

        //record it
        FishingRecord record = new FishingRecord(hoursSpentFishing, equipment.getGear(),
                                                 status.getLocation(),catchOfTheDay,this,
                                                 state.getStep());
        getCurrentTrip().recordFishing(record);
        memory.getCatchMemories().memorize(catchOfTheDay, status.getLocation());

        //now let regulations and the hold deal with it
        status.getRegulation().reactToCatch(catchOfTheDay);
        load(catchOfTheDay);

        //consume gas
        final double litersBurned = equipment.getGear().getFuelConsumptionPerHourOfFishing(this,
                                                                                           equipment.getBoat(),
                                                                                           status.getLocation()) * hoursSpentFishing;
        consumeFuel(litersBurned);

        memory.getYearlyCounter().count(YearlyFisherTimeSeries.EFFORT,hoursSpentFishing);
        memory.getDailyCounter().count(YearlyFisherTimeSeries.EFFORT,hoursSpentFishing);


        return catchOfTheDay;
    }

    public Gear getGear() {
        return equipment.getGear();
    }

    public void setGear(Gear gear) {
        equipment.setGear(gear);
    }

    /**
     *
     * @return true if destination == location
     */
    public boolean isAtDestination()
    {
        return status.getDestination().equals(status.getLocation());
    }

    public Regulation getRegulation() {
        return status.getRegulation();
    }

    public void setRegulation(Regulation regulation) {
        this.status.setRegulation(regulation);
    }


    public TimeSeries<Fisher> getYearlyData() {
        return memory.getYearlyTimeSeries();

    }

    public DailyFisherTimeSeries getDailyData() {
        return memory.getDailyTimeSeries();
    }

    /**
     * shortcut for getYearlyData().getLatestObservation(columnName)
     */
    public double getLatestYearlyObservation(String columnName) {
        return memory.getYearlyTimeSeries().getLatestObservation(columnName);
    }

    public double getBankBalance(){
        return status.getBankBalance();
    }

    public void earn(double moneyEarned)
    {
        status.setBankBalance(status.getBankBalance() + moneyEarned);
        getDailyCounter().count(YearlyFisherTimeSeries.CASH_FLOW_COLUMN,moneyEarned);

    }

    /**
     * consumes money and record the expenditure in the trip record. Useful for things like gas expenditure and other immediate
     * needs of the current trip in progress
     * @param moneySpent
     */
    public void spendForTrip(double moneySpent)
    {
        spendExogenously(moneySpent);
        memory.getTripLogger().recordCosts(moneySpent);
        getDailyCounter().count(YearlyFisherTimeSeries.CASH_FLOW_COLUMN,-moneySpent);

    }


    /**
     * tell the logger you incurred some travel costs without actually touching the bank balance. This is useful for accounting
     * for things like opportunity costs
     * @param implicitCost implicit expenditure
     */
    public void recordOpportunityCosts(double implicitCost)
    {
        memory.getTripLogger().recordOpportunityCosts(implicitCost);
    }


    /**
     * consumes the money but doesn't record the cost in the trip record. This is useful for expenditures like
     * bank interest payments, quota buying and selling and other things that are not due to the immediate needs of the trip
     * being taken
     * @param moneySpent
     */
    public void spendExogenously(double moneySpent){
        status.setBankBalance(status.getBankBalance() - moneySpent);
        getDailyCounter().count(YearlyFisherTimeSeries.CASH_FLOW_COLUMN,-moneySpent);

    }


    /**
     * grabs the data and learns about profits and such
     * @param info information about a trade
     */
    public void processTradeData(TradeInfo info){

        Species species = info.getSpecies();

        memory.getDailyCounter().countLanding(species, info.getBiomassTraded());
        memory.getDailyCounter().countEarnings(species, info.getMoneyExchanged());
        memory.getTripLogger().recordEarnings(species.getIndex(), info.getBiomassTraded(),
                                              info.getMoneyExchanged());

    }

    public double balanceXDaysAgo(int daysAgo)
    {
        //    Preconditions.checkArgument(dailyTimeSeries.numberOfObservations() >daysAgo);
        return getDailyData().getColumn(YearlyFisherTimeSeries.CASH_COLUMN).getDatumXDaysAgo(daysAgo);
    }


    public void addYearlyAdaptation(Adaptation a)
    {
        yearlyAdaptation.registerAdaptation(a);
    }

    public void addBiMonthlyAdaptation(Adaptation a)
    {
        bimonthlyAdaptation.registerAdaptation(a);
    }

    public void addPerTripAdaptation(Adaptation a)
    {
        tripAdaptation.registerAdaptation(a);
    }


    public void removeYearlyAdaptation(Adaptation a)
    {
        yearlyAdaptation.removeAdaptation(a);
    }

    public void removeBiMonthlyAdaptation(Adaptation a)
    {
        bimonthlyAdaptation.removeAdaptation(a);
    }

    public void removePerTripAdaptation(Adaptation a)
    {
        tripAdaptation.removeAdaptation(a);
    }


    public TripRecord getCurrentTrip() {
        return memory.getTripLogger().getCurrentTrip();
    }

    public void addTripListener(TripListener listener) {
        memory.getTripLogger().addTripListener(listener);
    }

    public void removeTripListener(TripListener listener) {
        memory.getTripLogger().removeTripListener(listener);
    }

    public void recordTripCutShort() {
        memory.getTripLogger().recordTripCutShort();
    }

    public void recordEarnings(int specieIndex,double biomass ,double newEarnings) {
        memory.getTripLogger().recordEarnings(specieIndex, biomass, newEarnings);
    }

    public void recordCosts(double newCosts) {
        memory.getTripLogger().recordCosts(newCosts);
    }

    public List<TripRecord> getFinishedTrips() {
        return memory.getTripLogger().getFinishedTrips();
    }

    public String getAction() {
        return status.getAction().getClass().getSimpleName();
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
        return status.getNetwork().getAllNeighbors(this);
    }

    /**
     * return all neighbors of this agent where there a directed edge from this fisher to his neighbors
     * @return a collection of agents
     */
    public Collection<Fisher> getDirectedFriends()
    {
        return status.getNetwork().getDirectedNeighbors(this);
    }

    public TripRecord getLastFinishedTrip() {
        return memory.getTripLogger().getLastFinishedTrip();
    }

    public double getYearlyCounterColumn(String columnName) {
        return memory.getYearlyCounter().getColumn(columnName);
    }

    public boolean isFuelEmergencyOverride() {
        return status.isFuelEmergencyOverride();
    }

    public double getFuelLeft() {
        return equipment.getBoat().getLitersOfFuelInTank();
    }

    public int getID() {
        return fisherID;
    }




    public double getHoursAtSea() {
        return status.getHoursAtSea();
    }

    private void increaseHoursAtSea(double hoursIncrease)
    {
        Preconditions.checkArgument(hoursIncrease >= 0, hoursIncrease);
        Preconditions.checkArgument(status.getHoursAtPort() == 0);
        status.setHoursAtSea(status.getHoursAtSea() + hoursIncrease);
    }


    private void increaseHoursAtPort(double hoursIncrease)
    {
        if(hoursIncrease > 0) { //0 could be an "arriving at port" before docking. Ignore that one
            Preconditions.checkArgument(hoursIncrease >= 0);
            Preconditions.checkArgument(status.getHoursAtSea() == 0);
            status.setHoursAtPort(status.getHoursAtPort() + hoursIncrease);
        }
    }

    public boolean isGoingToPort()
    {
        return status.isGoingToPort();
    }


    /**
     * basically changes the size of the maximum load but takes care of transferring whatever we were holding to the
     * new hold
     * @param newHold
     */
    public void changeHold(Hold newHold)
    {

        //unload old hold
        Catch oldHaul = equipment.getHold().unload();

        equipment.setHold(newHold);
        //load the new hold
        equipment.getHold().load(oldHaul);
    }


    public double getHoursAtPort() {
        return status.getHoursAtPort();
    }

    public FisherDailyCounter getDailyCounter() {
        return memory.getDailyCounter();
    }


    public double predictUnitProfit(int specieIndex)
    {
        return profitPerUnitPredictor[specieIndex].predict();
    }


    public double predictDailyCatches(int specieIndex)
    {
        return dailyCatchesPredictor[specieIndex].predict();
    }

    public double probabilityDailyCatchesBelowLevel(int specieIndex, double level)
    {
        return dailyCatchesPredictor[specieIndex].probabilityBelowThis(level);
    }

    public double probabilitySumDailyCatchesBelow(int specieIndex, double level, int daysToSum)
    {
        return dailyCatchesPredictor[specieIndex].probabilitySumBelowThis(level,daysToSum);
    }


    public double predictDailyProfits()
    {
        return dailyProfitsPredictor.predict();
    }

    public void assignDailyProfitsPredictor(Predictor dailyProfitsPredictor) {

        if(state != null)
        {
            this.dailyProfitsPredictor.turnOff();
            dailyProfitsPredictor.start(state,this);
        }

        this.dailyProfitsPredictor = dailyProfitsPredictor;


    }

    public void setDailyCatchesPredictor(int specieIndex, Predictor newPredictor)
    {
        if(state!=null)
        {
            newPredictor.start(state, this);
            dailyCatchesPredictor[specieIndex].turnOff();
        }
        dailyCatchesPredictor[specieIndex] = newPredictor;

    }

    public void resetDailyCatchesPredictors()
    {
        for(Predictor predictor : dailyCatchesPredictor)
            predictor.reset();
    }

    public void setProfitPerUnitPredictor(int specieIndex, Predictor newPredictor)
    {
        if(state!=null)
        {
            newPredictor.start(state, this);
            profitPerUnitPredictor[specieIndex].turnOff();
        }
        profitPerUnitPredictor[specieIndex] = newPredictor;

    }



    public void setWeatherStrategy(WeatherEmergencyStrategy weatherStrategy) {


        WeatherEmergencyStrategy old = this.weatherStrategy;

        this.weatherStrategy = weatherStrategy;
        if(state!=null)
        {
            old.turnOff();

            weatherStrategy.start(state, this);
        }

    }

    public WeatherEmergencyStrategy getWeatherStrategy() {
        return weatherStrategy;
    }


    public Fisher replaceFriend(Fisher friendToReplace,
                                    boolean ignoreDirection) {
        return status.getNetwork().replaceFriend(this,
                                                 friendToReplace,
                                                 ignoreDirection,
                                                 state.getRandom(),
                                                 state.getFishers());
    }

    /**
     * force the destination to be port, this is used only if your current destination is unreachable (because it's land or landlocked)
     */
    public void setDestinationForPort()
    {
        status.setDestination(getHomePort().getLocation());
    }


    /**
     * Ask the fisher what is the best tile with respect to catches made
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForCatchesRemembered(
            Comparator<LocationMemory<Catch>> comparator) {
        return memory.getBestSpotForCatchesRemembered(comparator);
    }

    /**
     *
     * @param location
     * @return
     */
    public TripRecord rememberLastTripHere(SeaTile location) {
        return memory.rememberLastTripHere(location);
    }

    public Map<SeaTile, LocationMemory<TripRecord>> rememberAllTrips() {
        return memory.rememberAllTrips();
    }

    /**
     * Ask the fisher what is the best tile with respect to trips made
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForTripsRemembered(
            Comparator<LocationMemory<TripRecord>> comparator) {
        return memory.getBestSpotForTripsRemembered(comparator);
    }

    public void addFeatureExtractor(
            String nameOfFeature,
            FeatureExtractor<SeaTile> extractor) {
        memory.addFeatureExtractor(nameOfFeature, extractor);
    }

    public FeatureExtractor<SeaTile> removeFeatureExtractor(String nameOfFeature) {
        return memory.removeFeatureExtractor(nameOfFeature);
    }

    /**
     * Getter for property 'tileRepresentation'.
     *
     * @return Value for property 'tileRepresentation'.
     */
    public FeatureExtractors<SeaTile> getTileRepresentation() {
        return memory.getTileRepresentation();
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    @UiHidden
    public FisherStatus grabStatus() {
        return status;
    }

    /**
     * Getter for property 'memory'.
     *
     * @return Value for property 'memory'.
     */
    @UiHidden
    public FisherMemory grabMemory() {
        return memory;
    }


    /**
     * Getter for property 'equipment'.
     *
     * @return Value for property 'equipment'.
     */
    public FisherEquipment grabEquipment() {
        return equipment;
    }


}

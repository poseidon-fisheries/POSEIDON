/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
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
import uk.ac.ox.oxfish.fisher.log.*;
import uk.ac.ox.oxfish.fisher.selfanalysis.FixedPredictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.Predictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.*;
import uk.ac.ox.oxfish.model.market.TradeInfo;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
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
     * "tags" that can be added or removed at will. They have no explicit meaning or effect.
     */
    private final List<String> tags = new LinkedList<>();

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


    /**
     * when true the agent can ignore the rules (depending on what the strategies tell him to do)
     */
    private boolean cheater = false;



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
     * the strategy to choose what gear to use when going out.
     */
    private GearStrategy gearStrategy;



    private DiscardingStrategy discardingStrategy;

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



    /**
     * Creates a fisher by giving it all its sub-components
     * @param id the id-number of the fisher
     * @param homePort the home port
     * @param random a randomizer
     * @param regulation the rules the fisher follows
     * @param departingStrategy how the fisher decides how to leave the port
     * @param destinationStrategy how the fisher decides where to go
     * @param fishingStrategy how the fisher decides how much to fish
     * @param gearStrategy
     * @param discardingStrategy
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
            GearStrategy gearStrategy,
            DiscardingStrategy discardingStrategy,
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
        this.gearStrategy=gearStrategy;
        this.weatherStrategy = weatherStrategy;
        this.discardingStrategy = discardingStrategy;

        //predictors
        Predictor[] dailyCatchesPredictor = new Predictor[numberOfSpecies];
        status.setDailyCatchesPredictor(dailyCatchesPredictor);
        Predictor[] profitPerUnitPredictor = new Predictor[numberOfSpecies];
        status.setProfitPerUnitPredictor(profitPerUnitPredictor);
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

        //start the regulations
        getRegulation().start(state,this);

        //start the strategies
        destinationStrategy.start(state,this);
        fishingStrategy.start(state,this);
        departingStrategy.start(state,this);
        gearStrategy.start(state,this);
        weatherStrategy.start(state,this);
        discardingStrategy.start(state,this);

        //start the adaptations
        bimonthlyAdaptation.start(state,this);
        yearlyAdaptation.start(state, this);
        tripAdaptation.start(state, this);

        //start the predictors
        for(int i=0; i<status.getDailyCatchesPredictor().length; i++)
        {
            status.getDailyCatchesPredictor()[i].start(state, this);
            status.getProfitPerUnitPredictor()[i].start(state,this);

        }
        status.getDailyProfitsPredictor().start(state,this);


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
        memory.getTripMemories().turnOff();

        //turn off the strategies
        destinationStrategy.turnOff(this);
        fishingStrategy.turnOff(this);
        departingStrategy.turnOff(this);
        weatherStrategy.turnOff(this);
        getRegulation().turnOff(this);
        discardingStrategy.turnOff(this);

        //turn off the adaptations
        bimonthlyAdaptation.turnOff(this);
        yearlyAdaptation.turnOff(this);
        tripAdaptation.turnOff(this);
    }

    @Override
    public void step(SimState simState) {
        FishState model = (FishState) simState;

        if(Log.TRACE) {
            Log.trace("Fisher " + fisherID + " is going to start his step");
        }

        //tell equipment!1003.347
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
        getCurrentTrip().addToDistanceTravelled(distanceTravelled);
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
        getCurrentTrip().recordGasConsumption(litersConsumed);
        assert  equipment.getBoat().getFuelCapacityInLiters()>=0 || isFuelEmergencyOverride() :
                "a boat has lspiRun into negative fuel territory";
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


    public boolean isAtPortAndDocked(){
        return isAtPort() && getHomePort().isDocked(this);
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
        status.setExogenousEmergencyOverride(false);
        memory.getYearlyCounter().count(FisherYearlyTimeSeries.HOURS_OUT, status.getHoursAtSea());
        memory.getYearlyCounter().count(FisherYearlyTimeSeries.FUEL_CONSUMPTION, litersBought);

        //now pay for it
        double gasExpenditure = litersBought * status.getHomePort().getGasPricePerLiter();
        spendForTrip(gasExpenditure);
        memory.getYearlyCounter().count(FisherYearlyTimeSeries.FUEL_EXPENDITURE, gasExpenditure);
        if(status.getHoursAtSea()>0) //if you have been somewhere at all
        {
            memory.getYearlyCounter().count(FisherYearlyTimeSeries.TRIPS, 1);
            //log all areas as just visited!
            for(SeaTile tile : getCurrentTrip().getTilesFished())
                memory.registerVisit(tile, (int) state.getDay());
        }

        //notify listeners
        for(DockingListener listener : dockingListeners)
        {
            listener.dockingEvent(this,getHomePort());
        }

        //spend money on all new costs
        for(Cost realCosts : status.getAdditionalTripCosts()) {
            double cost = realCosts.cost(this, state, getCurrentTrip(), getCurrentTrip().getEarnings(),status.getHoursAtSea() );
            spendForTrip(cost);
        }
        //account for opportunity costs
        for(Cost opportunityCost : status.getOpportunityCosts()) {
            double cost = opportunityCost.cost(this, state, getCurrentTrip(), getCurrentTrip().getEarnings(),status.getHoursAtSea() );
            recordOpportunityCosts(cost);
        }


        //finish trip!
        TripRecord finished = memory.getTripLogger().finishTrip(status.getHoursAtSea(), getHomePort());
        //account for the costs
        memory.getYearlyCounter().count(FisherYearlyTimeSeries.VARIABLE_COSTS,finished.getTotalCosts());
        memory.getYearlyCounter().count(FisherYearlyTimeSeries.EARNINGS,finished.getEarnings());

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
        return !status.isAnyEmergencyFlagOn() && departingStrategy.shouldFisherLeavePort(this, model,model.getRandom());
    }

    /**
     * tell the fisher to check his destination and update it if necessary. If the regulation forbid us to be at sea
     * the destination is always port
     * @param model the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     * */
    public void updateDestination(FishState model, Action currentAction) {


        //if you are not allowed at sea or you are running out of gas, go home
        if(
                (!status.getRegulation().allowedAtSea(this, model) && !cheater )
                        || status.isAnyEmergencyFlagOn())
            status.setDestination(status.getHomePort().getLocation());
        else
            status.setDestination(
                    destinationStrategy.chooseDestination(this, status.getRandom(), model, currentAction));
        Preconditions.checkNotNull(status.getDestination(), "Destination can never be null!");
    }

    /**
     * called to check if there is so little fuel we must go back home
     */
    private void updateFuelEmergencyFlag(NauticalMap map)
    {

        if(getFuelLeft()>=999999999) //if you have boats this large, I am just going to assume you don't care about fuel
            return;

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
            old.turnOff(this); //turn off old strategy
            destinationStrategy.start(state,this);
        }
    }

    public void setDepartingStrategy(DepartingStrategy newStrategy) {
        DepartingStrategy old = this.departingStrategy;
        this.departingStrategy = newStrategy;
        if(state != null) //if we have started already
        {
            old.turnOff(this); //turn off old strategy
            departingStrategy.start(state,this);
        }
    }


    public void setDiscardingStrategy(DiscardingStrategy newStrategy) {

        DiscardingStrategy old = this.discardingStrategy;
        this.discardingStrategy = newStrategy;

        if(state != null) //if we have started already
        {
            old.turnOff(this); //turn off old strategy
            discardingStrategy.start(state,this);
        }
    }

    /**
     * Getter for property 'discardingStrategy'.
     *
     * @return Value for property 'discardingStrategy'.
     */
    public DiscardingStrategy getDiscardingStrategy() {
        return discardingStrategy;
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
            old.turnOff(this); //turn off old strategy
            fishingStrategy.start(state,this);
        }
    }

    public boolean shouldIFish(FishState state)
    {
        return !status.isAnyEmergencyFlagOn() && fishingStrategy.shouldFish(this,
                                                                            state.getRandom(),
                                                                            state,
                                                                            getCurrentTrip());

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
    public double getTotalWeightOfCatchInHold(Species species) {
        return equipment.getHold().getWeightOfCatchInHold(species);
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



    private boolean doubleCheckCounters(Species species){

        double landings = getDailyCounter().getCatchesPerSpecie(species.getIndex());
        double sum = 0;
        for(int i=0; i<species.getNumberOfBins(); i++)
            sum += getDailyCounter().getSpecificLandings(species,i);
//        System.out.println(landings);
//        System.out.println(sum);
        return Math.abs(sum-landings)<.01;

    }


    /**
     * tell the fisher to use its gear to fish at current location. It stores everything in the hold
     * @param modelBiology the global biology object
     * @param hoursSpentFishing hours spent on this activity
     * @param state the model
     * @return the fish caught and stored (barring overcapacity)
     */
    public void fishHere(GlobalBiology modelBiology, int hoursSpentFishing, FishState state)
    {

        //compute the catches (but kill nothing yet)
        fishHere(modelBiology,hoursSpentFishing,state,status.getLocation());


    }

    /**
     * tell the fisher to use its gear to fish at local biology provided by the caller. This is useful if you
     * are not specifically referring to catching stuff straight out of the SeaTile
     * @param modelBiology the global biology object
     * @param hoursSpentFishing hours spent on this activity
     * @param state the model
     * @param localBiology this is what will need to "react" to the amount caught
     * @return the fish caught and stored (barring overcapacity)
     */
    public void fishHere(GlobalBiology modelBiology, int hoursSpentFishing, FishState state, LocalBiology localBiology)
    {
        //preamble
        SeaTile here = status.getLocation();
        Preconditions.checkState(here.getAltitude() < 0, "can't fish on land!");
        //compute the catches (but kill nothing yet)
        Pair<Catch, Catch> catchesAndKept = computeCatchesHere(status.getLocation(),
                                                               localBiology,
                                                               hoursSpentFishing,
                                                               modelBiology,
                                                               state);
        //make local react to catches (involves killing, usually)
        removeFishAfterFishing(modelBiology, catchesAndKept.getFirst(), catchesAndKept.getSecond(), localBiology);
        //pull the fish up, store it, and burn fuel
        recordAndHaulCatch(hoursSpentFishing, here, catchesAndKept.getFirst(), catchesAndKept.getSecond());


    }



    private Pair<Catch,Catch> computeCatchesHere(SeaTile context,
                                                 LocalBiology biology,
                                                int hoursSpentFishing, GlobalBiology modelBiology, FishState state)
    {
        //transfer fish from local to here
        Catch catchOfTheDay = equipment.getGear().fish(this,
                                                       biology,
                                                       context ,
                                                       hoursSpentFishing,
                                                       modelBiology);
        Catch kept = discardingStrategy.chooseWhatToKeep(
                context,
                this,
                catchOfTheDay,
                hoursSpentFishing,
                getRegulation(),
                state,
                grabRandomizer()
        );
        return new Pair<>(catchOfTheDay,kept);
    }

    private void removeFishAfterFishing(
            GlobalBiology modelBiology, Catch catchOfTheDay, Catch notDiscarded, LocalBiology biology) {
        if(catchOfTheDay.totalCatchWeight()> FishStateUtilities.EPSILON) {
            biology.reactToThisAmountOfBiomassBeingFished(catchOfTheDay, notDiscarded, modelBiology);
            //now count catches (which isn't necessarilly landings)
            for(Species species : modelBiology.getSpecies()) {
                getDailyCounter().countCatches(species, catchOfTheDay.getWeightCaught(species));

                if(catchOfTheDay.hasAbundanceInformation() && species.getNumberOfBins() > 0) {
                    getDailyCounter().countLandinngPerBin(species, catchOfTheDay);
                    assert doubleCheckCounters(species);

                }

            }
        }
    }

    private void recordAndHaulCatch(int hoursSpentFishing, SeaTile here, Catch catchOfTheDay, Catch notDiscarded) {
        //learn, record and collect
        //record it
        FishingRecord record = new FishingRecord(hoursSpentFishing,
                                                 here, catchOfTheDay);
        memory.getTripLogger().recordFishing(record);

        //now let regulations and the hold deal with it
        status.getRegulation().reactToFishing(here, this, catchOfTheDay, notDiscarded , hoursSpentFishing);
        load(notDiscarded);

        //consume gas
        final double litersBurned = equipment.getGear().getFuelConsumptionPerHourOfFishing(this,
                                                                                           equipment.getBoat(),
                                                                                           here) * hoursSpentFishing;
        if(litersBurned>0)
            consumeFuel(litersBurned);

        memory.getYearlyCounter().count(FisherYearlyTimeSeries.EFFORT, hoursSpentFishing);
        memory.getDailyCounter().count(FisherYearlyTimeSeries.EFFORT, hoursSpentFishing);
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


        if(state != null)
        {
            getRegulation().turnOff(this);
            regulation.start(state,this);
        }


        this.status.setRegulation(regulation);
    }


    public TimeSeries<Fisher> getYearlyData() {
        return memory.getYearlyTimeSeries();

    }

    public FisherDailyTimeSeries getDailyData() {
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
        getDailyCounter().count(FisherYearlyTimeSeries.CASH_FLOW_COLUMN, moneyEarned);

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
        getDailyCounter().count(FisherYearlyTimeSeries.CASH_FLOW_COLUMN, -moneySpent);

    }


    /**
     * tell the logger you incurred some travel costs without actually touching the bank balance. This is useful for accounting
     * for things like opportunity costs
     * @param implicitCost implicit expenditure
     */
    private void recordOpportunityCosts(double implicitCost)
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
        getDailyCounter().count(FisherYearlyTimeSeries.CASH_FLOW_COLUMN, -moneySpent);

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
        return getDailyData().getColumn(FisherYearlyTimeSeries.CASH_COLUMN).getDatumXDaysAgo(daysAgo);
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


    public List<TripRecord> getFinishedTrips() {
        return memory.getTripLogger().getFinishedTrips();
    }

    public String getAction() {
        return status.getAction().getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "Fisher " + fisherID +"; " + getTags().stream().reduce((s, s2) -> s+" - "+s2);
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

    public Counter getYearlyCounter() {
        return memory.getYearlyCounter();
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
        return status.getProfitPerUnitPredictor()[specieIndex].predict();
    }


    public double predictDailyCatches(int specieIndex)
    {
        return status.getDailyCatchesPredictor()[specieIndex].predict();
    }

    public double probabilityDailyCatchesBelowLevel(int specieIndex, double level)
    {
        return status.getDailyCatchesPredictor()[specieIndex].probabilityBelowThis(level);
    }

    public double probabilitySumDailyCatchesBelow(int specieIndex, double level, int daysToSum)
    {
        return status.getDailyCatchesPredictor()[specieIndex].probabilitySumBelowThis(level,daysToSum);
    }


    public double predictDailyProfits()
    {
        return status.getDailyProfitsPredictor().predict();
    }

    public void assignDailyProfitsPredictor(Predictor dailyProfitsPredictor) {

        if(state != null)
        {
            this.status.getDailyProfitsPredictor().turnOff(this);
            dailyProfitsPredictor.start(state,this);
        }

        status.setDailyProfitsPredictor(dailyProfitsPredictor);


    }

    public void setDailyCatchesPredictor(int specieIndex, Predictor newPredictor)
    {
        if(state!=null)
        {
            newPredictor.start(state, this);
            status.getDailyCatchesPredictor()[specieIndex].turnOff(this);
        }
        status.getDailyCatchesPredictor()[specieIndex] = newPredictor;

    }

    public void resetDailyCatchesPredictors()
    {
        for(Predictor predictor : status.getDailyCatchesPredictor())
            predictor.reset();
    }

    public void setProfitPerUnitPredictor(int specieIndex, Predictor newPredictor)
    {
        if(state!=null)
        {
            newPredictor.start(state, this);
            status.getProfitPerUnitPredictor()[specieIndex].turnOff(this);
        }
        status.getProfitPerUnitPredictor()[specieIndex] = newPredictor;

    }



    public void setWeatherStrategy(WeatherEmergencyStrategy weatherStrategy) {


        WeatherEmergencyStrategy old = this.weatherStrategy;

        this.weatherStrategy = weatherStrategy;
        if(state!=null)
        {
            old.turnOff(this);

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
     * can the agent fish at this location?
     * @param tile the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    public boolean isAllowedToFishHere(SeaTile tile, FishState model) {
        return status.isAllowedToFishHere(this, tile, model);
    }

    /**
     * Getter for property 'gearStrategy'.
     *
     * @return Value for property 'gearStrategy'.
     */
    public GearStrategy getGearStrategy() {
        return gearStrategy;
    }

    /**
     * Setter for property 'gearStrategy'.
     *
     * @param gearStrategy Value to set for property 'gearStrategy'.
     */
    public void setGearStrategy(GearStrategy gearStrategy) {

        if(state != null && this.gearStrategy != gearStrategy)
            this.gearStrategy.turnOff(this);



        this.gearStrategy = gearStrategy;
        if(state!=null)
            this.gearStrategy.start(state,this);

    }

    /**
     * Check if you want to change your gear now!
     * @param random randomizer
     * @param model the model
     * @param currentAction the current action
     */
    public void updateGear( MersenneTwisterFast random,
                            FishState model,
                            Action currentAction)
    {
        Preconditions.checkState(isAtPort(), "Changing Gear out of Port. Not expected!");
        gearStrategy.updateGear(this,random,model,currentAction);
    }


    /**
     * Getter for property 'tags'.
     *
     * @return Value for property 'tags'.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Getter for property 'cheater'.
     *
     * @return Value for property 'cheater'.
     */
    public boolean isCheater() {
        return cheater;
    }

    /**
     * Setter for property 'cheater'.
     *
     * @param cheater Value to set for property 'cheater'.
     */
    public void setCheater(boolean cheater) {
        this.cheater = cheater;
    }

    /**
     * Getter for property 'additionalTripCosts'.
     *
     * @return Value for property 'additionalTripCosts'.
     */
    public LinkedList<Cost> getAdditionalTripCosts() {
        return status.getAdditionalTripCosts();
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public LinkedList<Cost> getOpportunityCosts() {
        return status.getOpportunityCosts();
    }



    public int numberOfDailyObservations() {
        return memory.numberOfDailyObservations();
    }


    /**
     * keep that memory in the database. The key cannot be currently in use!
     * @param key the key for the object
     * @param item the object to store
     */
    public void memorize(String key, Object item) {
        memory.memorize(key, item);
    }

    /**
     * removes the memory associated with that key
     * @param key
     */
    public void forget(String key) {
        memory.forget(key);
    }

    /**
     * returns the object associated with this key
     * @param key
     */
    public Object remember(String key) {
        return memory.remember(key);
    }

    /**
     * registers visit (if the memory exists)
     * @param group
     * @param day
     */
    public void registerVisit(int group, int day) {
        memory.registerVisit(group, day);
    }

    /**
     * registers visit (if the memory exists)
     * @param tile
     * @param day
     */
    public void registerVisit(SeaTile tile, int day) {
        memory.registerVisit(tile, day);
    }

    public DiscretizedLocationMemory getDiscretizedLocationMemory() {
        return memory.getDiscretizedLocationMemory();
    }

    public void setDiscretizedLocationMemory(DiscretizedLocationMemory discretizedLocationMemory) {
        memory.setDiscretizedLocationMemory(discretizedLocationMemory);
    }

    public SocialNetwork getSocialNetwork() {
        return state.getSocialNetwork();
    }


    /**
     * Getter for property 'exogenousEmergencyOverride'.
     *
     * @return Value for property 'exogenousEmergencyOverride'.
     */
    public boolean isExogenousEmergencyOverride() {
        return status.isExogenousEmergencyOverride();
    }

    /**
     * Setter for property 'exogenousEmergencyOverride'.
     *
     * @param exogenousEmergencyOverride Value to set for property 'exogenousEmergencyOverride'.
     */
    public void setExogenousEmergencyOverride(boolean exogenousEmergencyOverride) {
        status.setExogenousEmergencyOverride(exogenousEmergencyOverride);
    }

    public double getTotalWeightOfCatchInHold() {
        return equipment.getTotalWeightOfCatchInHold();
    }

    public double getWeightOfCatchInHold(Species species) {
        return equipment.getWeightOfCatchInHold(species);
    }


    /**
     * Setter for property 'homePort'.
     *
     * @param homePort Value to set for property 'homePort'.
     */
    public void setHomePort(Port homePort) {
        status.setHomePort(homePort);
    }

    public void teleport(SeaTile tile)
    {
        status.setLocation(tile);
    }

    /**
     * grabs the state reference from the fisher
     * @return
     */
    public FishState grabState(){
        return state;
    }

    public boolean canAndWantToFishHere()
    {
        return (getRegulation().canFishHere(this,getLocation(), state) || isCheater())
                &&
                this.shouldIFish(state);
    }

    public Hold getHold() {
        return equipment.getHold();
    }

    public double getCountedLandingsPerBin(Species species, int bin) {
        return getDailyCounter().getSpecificLandings(species, bin);
    }

    public double getHoursAtSeaThisYear() {
        return memory.getHoursAtSeaThisYear();
    }
}

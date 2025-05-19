/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
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
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.GasCost;
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
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.model.restrictions.ReputationalRestrictions;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.AdaptationDailyScheduler;
import uk.ac.ox.oxfish.utility.adaptation.AdaptationPerTripScheduler;
import uk.ac.ox.poseidon.agents.api.Agent;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * The boat catching all that delicious fish.
 * At its core it is a discrete-state automata: the Action class represents a possible state and the fisher can go through
 * each of them in a turn. <br>
 * Strategies are instead the fisher way to deal with decision points (should I go fish or not? Where do I go?)
 * Created by carrknight on 4/2/15.
 */
public class Fisher implements Steppable, Startable, Agent {


    /***
     *     __   __        _      _    _
     *     \ \ / /_ _ _ _(_)__ _| |__| |___ ___
     *      \ V / _` | '_| / _` | '_ \ / -_|_-<
     *       \_/\__,_|_| |_\__,_|_.__/_\___/__/
     *
     */

    //ten thousands liter of fuel means that you don't really need to worry about checking for fuel emergency
    private static final int LARGE_AMOUNT_OF_GAS = 10000;
    private static final long serialVersionUID = -3650930851458635298L;
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
     * contains all the equipment variables (gear, boat,hold)
     */
    private final FisherEquipment equipment;
    /**
     * collection of adaptation algorithms to fire every 2 months
     */
    private final AdaptationDailyScheduler bimonthlyAdaptation = new AdaptationDailyScheduler(60);
    /**
     * collection of adaptation algorithms to fire every 365  days
     */
    private final AdaptationDailyScheduler yearlyAdaptation = new AdaptationDailyScheduler(365);


    /***
     *      ___           _                    _
     *     | __|__ _ _  _(_)_ __ _ __  ___ _ _| |_
     *     | _|/ _` | || | | '_ \ '  \/ -_) ' \  _|
     *     |___\__, |\_,_|_| .__/_|_|_\___|_||_\__|
     *            |_|      |_|
     */
    /**
     * collection of adaptation algorithms to fire every trip
     */
    private final AdaptationPerTripScheduler tripAdaptation = new AdaptationPerTripScheduler();

    /***
     *      ___ _            _            _
     *     / __| |_ _ _ __ _| |_ ___ __ _(_)___ ___
     *     \__ \  _| '_/ _` |  _/ -_) _` | / -_|_-<
     *     |___/\__|_| \__,_|\__\___\__, |_\___/__/
     *                              |___/
     */
    private final LinkedList<DockingListener> dockingListeners = new LinkedList<>();
    /**
     * An immutable copy of the tags list, which needs to be manually refreshed
     * by calling {@link #refreshTagSet()} if the tag list changes. Ideally, we'd monitor the
     * list for changes ourselves, but we can't since we're exposing the mutable list.
     */
    private Set<String> tagSet = ImmutableSet.of();
    /**
     * a link to the model. Grabbed when start() is called. It's not used or shared except when a new strategy is plugged in
     * at which point this reference is used to call the strategy's start method
     */
    private FishState state;
    /**
     * when true the agent can ignore the rules (depending on what the strategies tell him to do)
     */
    private boolean cheater = false;
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


    public Fisher(
        final int id, final Port port,
        final MersenneTwisterFast random,
        final Regulation regulation,
        final ReputationalRestrictions reputationalRestrictions,
        final RegionalRestrictions communityRestrictions,
        //strategies:
        final DepartingStrategy departingStrategy,
        final DestinationStrategy destinationStrategy,
        final FishingStrategy fishingStrategy,
        final GearStrategy gearStrategy,
        final DiscardingStrategy discardingStrategy,
        final WeatherEmergencyStrategy weatherStrategy,
        //equipment:
        final Boat boat, final Hold hold, final Gear gear,
        final int numberOfSpecies
    ) {
        // TODO Auto-generated constructor stub
        this(id, port, random, regulation, departingStrategy, destinationStrategy,
            fishingStrategy, gearStrategy, discardingStrategy, weatherStrategy, boat, hold, gear, numberOfSpecies
        );
        this.status.setCommunalStandards(communityRestrictions);
        this.status.setReputationalRisk(reputationalRestrictions);
    }


    /**
     * Creates a fisher by giving it all its sub-components
     *
     * @param id                  the id-number of the fisher
     * @param homePort            the home port
     * @param random              a randomizer
     * @param regulation          the rules the fisher follows
     * @param departingStrategy   how the fisher decides how to leave the port
     * @param destinationStrategy how the fisher decides where to go
     * @param fishingStrategy     how the fisher decides how much to fish
     * @param gearStrategy
     * @param discardingStrategy
     * @param weatherStrategy
     * @param boat                the boat the fisher uses
     * @param hold                the space available to load fish
     * @param gear                what is used for fishing
     * @param numberOfSpecies
     */
    public Fisher(
        final int id,
        final Port homePort, final MersenneTwisterFast random,
        final Regulation regulation,
        //strategies:
        final DepartingStrategy departingStrategy,
        final DestinationStrategy destinationStrategy,
        final FishingStrategy fishingStrategy,
        final GearStrategy gearStrategy,
        final DiscardingStrategy discardingStrategy,
        final WeatherEmergencyStrategy weatherStrategy,
        //equipment:
        final Boat boat, final Hold hold, final Gear gear,
        final int numberOfSpecies
    ) {
        this.fisherID = id;

        //set up variables
        this.status = new FisherStatus(random, regulation, homePort);
        this.equipment = new FisherEquipment(boat, hold, gear);
        this.memory = new FisherMemory();

        homePort.dock(this);//we dock
        //strategies
        this.departingStrategy = departingStrategy;
        this.destinationStrategy = destinationStrategy;
        this.fishingStrategy = fishingStrategy;
        this.gearStrategy = gearStrategy;
        this.weatherStrategy = weatherStrategy;
        this.discardingStrategy = discardingStrategy;

        //predictors
        final Predictor[] dailyCatchesPredictor = new Predictor[numberOfSpecies];
        status.setDailyCatchesPredictor(dailyCatchesPredictor);
        final Predictor[] profitPerUnitPredictor = new Predictor[numberOfSpecies];
        status.setProfitPerUnitPredictor(profitPerUnitPredictor);
        for (int i = 0; i < dailyCatchesPredictor.length; i++) {
            dailyCatchesPredictor[i] = new FixedPredictor(Double.NaN);
            profitPerUnitPredictor[i] = new FixedPredictor(Double.NaN);
        }

    }

    public void start(final FishState state) {

        this.state = state;
        this.status.setNetwork(state.getSocialNetwork());
        receipt = state.scheduleEveryStep(this, StepOrder.FISHER_PHASE);


        //start datas
        memory.start(state, this);

        //start the regulations
        getRegulation().start(state, this);

        //start the strategies
        destinationStrategy.start(state, this);
        fishingStrategy.start(state, this);
        departingStrategy.start(state, this);
        gearStrategy.start(state, this);
        weatherStrategy.start(state, this);
        discardingStrategy.start(state, this);

        //start the adaptations
        bimonthlyAdaptation.start(state, this);
        yearlyAdaptation.start(state, this);
        tripAdaptation.start(state, this);

        //start the predictors
        for (int i = 0; i < status.getDailyCatchesPredictor().length; i++) {
            status.getDailyCatchesPredictor()[i].start(state, this);
            status.getProfitPerUnitPredictor()[i].start(state, this);

        }
        status.getDailyProfitsPredictor().start(state, this);


    }

    public Regulation getRegulation() {
        return status.getRegulation();
    }

    public void setRegulation(final Regulation regulation) {


        if (state != null) {
            getRegulation().turnOff(this);
            regulation.start(state, this);
        }


        this.status.setRegulation(regulation);
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

        getSocialNetwork().removeFisher(this, state);
    }

    public SocialNetwork getSocialNetwork() {
        return state.getSocialNetwork();
    }

    @Override
    public void step(final SimState simState) {
        final FishState model = (FishState) simState;

        Logger.getGlobal().fine("Fisher " + fisherID + " is going to start his step");

        //tell equipment!
        equipment.getBoat().newStep();

        //run the state machine
        double hoursLeft = model.getHoursPerStep();
        while (true) {
            //pre-action accounting
            updateFuelEmergencyFlag(model.getMap());
            final double hoursLeftBeforeAction = hoursLeft;

            //take an action
            final ActionResult result = status.getAction().act(model, this, status.getRegulation(), hoursLeft);
            hoursLeft = result.getHoursLeft();
            //if you have been moving or you were staying still somewhere away from port
            if (status.getAction() instanceof Moving || !isAtPort())
                increaseHoursAtSea(hoursLeftBeforeAction - hoursLeft);
            else
                increaseHoursAtPort(hoursLeftBeforeAction - hoursLeft);

            //set up next action
            status.setAction(result.getNextState());


            //if you are out of time, continue tomorrow
            if (hoursLeft <= 0) {
                assert Math.abs(hoursLeft) < .001 : hoursLeft; //shouldn't be negative!
                break;
            }
        }


    }

    /**
     * called to check if there is so little fuel we must go back home
     */
    private void updateFuelEmergencyFlag(final NauticalMap map) {

        status.setWeatherEmergencyOverride(weatherStrategy.updateWeatherEmergencyFlag(
            status.isWeatherEmergencyOverride(),
            this,
            getLocation()
        ));


        if (getFuelLeft() >= LARGE_AMOUNT_OF_GAS) //if you have boats this large, I am just going to assume you don't care about fuel
            return;

        if (!status.isFuelEmergencyOverride())
            status.setFuelEmergencyOverride(!equipment.getBoat().isFuelEnoughForTrip(
                map.distance(status.getLocation(), getHomePort().getLocation()), 1.2));


    }

    public boolean isAtPort() {
        return this.status.isAtPort();
    }

    private void increaseHoursAtSea(final double hoursIncrease) {
        Preconditions.checkArgument(hoursIncrease >= 0, hoursIncrease);
        Preconditions.checkArgument(status.getHoursAtPort() == 0);
        status.setHoursAtSea(status.getHoursAtSea() + hoursIncrease);
    }

    private void increaseHoursAtPort(final double hoursIncrease) {
        if (hoursIncrease > 0) { //0 could be an "arriving at port" before docking. Ignore that one
            Preconditions.checkArgument(hoursIncrease >= 0);
            Preconditions.checkArgument(status.getHoursAtSea() == 0);
            status.setHoursAtPort(status.getHoursAtPort() + hoursIncrease);
        }
    }

    public SeaTile getLocation() {
        return status.getLocation();
    }

    public double getFuelLeft() {
        return equipment.getBoat().getLitersOfFuelInTank();
    }

    public Port getHomePort() {
        return status.getHomePort();
    }

    /**
     * Setter for property 'homePort'.
     *
     * @param homePort Value to set for property 'homePort'.
     */
    public void setHomePort(final Port homePort) {
        status.setHomePort(homePort);
    }

    public SeaTile getDestination() {
        return status.getDestination();
    }

    public Boat getBoat() {
        return equipment.getBoat();
    }

    public void setBoat(final Boat boat) {
        equipment.setBoat(boat);
    }

    /**
     * how much time it takes to travel this many kilometers
     *
     * @param kilometersToTravel how many kilometers to move through
     * @return how many hours it takes to move "kilometersToTravel" (in hours)
     */
    public double hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(final double kilometersToTravel) {
        return equipment.getBoat().hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(kilometersToTravel);
    }

    /**
     * like hypotheticalTravelTimeToMoveThisMuchAtFullSpeed but adds to it the hours this boat has already travelled
     *
     * @param segmentLengthInKilometers the length of the new step
     * @return current travel time + travel time of the new segment (in hours)
     */
    public double totalTravelTimeAfterAddingThisSegment(final double segmentLengthInKilometers) {
        return equipment.getBoat().totalTravelTimeAfterAddingThisSegment(segmentLengthInKilometers);
    }

    /**
     * set new location, consume time and tell the map about our new location
     *
     * @param newPosition       the new position
     * @param map               the map on which we are moving
     * @param distanceTravelled
     */
    public void move(
        final SeaTile newPosition,
        final NauticalMap map,
        final FishState state,
        final double distanceTravelled
    ) {
        Preconditions.checkArgument(newPosition != status.getLocation()); //i am not already here!
        Preconditions.checkArgument(distanceTravelled > 0); //i am not already here!
        equipment.getBoat().recordTravel(distanceTravelled); //tell the boat
        //consume gas!
        consumeFuel(equipment.getBoat().expectedFuelConsumption(distanceTravelled));
        getCurrentTrip().addToDistanceTravelled(distanceTravelled);
        //arrive at new position
        status.setLocation(newPosition);
        map.recordFisherLocation(this, newPosition.getGridX(), newPosition.getGridY());

        //this condition doesn't hold anymore because time travelled is "conserved" between steps
        //   Preconditions.checkState(boat.getHoursTravelledToday() <= state.getHoursPerStep(), boat.getHoursTravelledToday() +  " and ");
        Preconditions.checkState(newPosition == status.getLocation());
    }

    public void consumeFuel(final double litersConsumed) {
        equipment.getBoat().consumeFuel(litersConsumed);
        getCurrentTrip().recordGasConsumption(litersConsumed);
        assert equipment.getBoat().getFuelCapacityInLiters() >= 0 || isFuelEmergencyOverride() :
            "a boat has lspiRun into negative fuel territory";
    }

    public TripRecord getCurrentTrip() {
        return memory.getTripLogger().getCurrentTrip();
    }

    public boolean isFuelEmergencyOverride() {
        return status.isFuelEmergencyOverride();
    }

    /**
     * departs
     */
    public void undock() {
        assert this.status.getHoursAtSea() == 0;
        assert isAtPort();
        status.getHomePort().depart(this);
        memory.getTripLogger().newTrip(getHoursAtPort(), state.getDay(), this);
        status.setHoursAtPort(0);
    }

    public double getHoursAtPort() {
        return status.getHoursAtPort();
    }

    public boolean isAtPortAndDocked() {
        return isAtPort() && getHomePort().isDocked(this);
    }

    public void addDockingListener(final DockingListener listener) {
        dockingListeners.add(listener);
    }

    public void removeDockingListener(final DockingListener listener) {
        dockingListeners.remove(listener);
    }

    /**
     * anchors at home-port and sets the trip to "over"
     */
    public void dock() {
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
        final double gasExpenditure = litersBought * status.getHomePort().getGasPricePerLiter();
        spendForTrip(gasExpenditure);
        memory.getYearlyCounter().count(FisherYearlyTimeSeries.FUEL_EXPENDITURE, gasExpenditure);
        if (status.getHoursAtSea() > 0) //if you have been somewhere at all
        {
            memory.getYearlyCounter().count(FisherYearlyTimeSeries.TRIPS, 1);
            //log all areas as just visited!
            for (final SeaTile tile : getCurrentTrip().getTilesFished())
                memory.registerVisit(tile, state.getDay());
        }

        //notify listeners
        for (final DockingListener listener : dockingListeners) {
            listener.dockingEvent(this, getHomePort());
        }

        //spend money on all new costs
        for (final Cost realCosts : status.getAdditionalTripCosts()) {
            final double cost = realCosts.cost(
                this,
                state,
                getCurrentTrip(),
                getCurrentTrip().getEarnings(),
                status.getHoursAtSea()
            );
            spendForTrip(cost);
        }
        //account for opportunity costs
        for (final Cost opportunityCost : status.getOpportunityCosts()) {
            final double cost = opportunityCost.cost(
                this,
                state,
                getCurrentTrip(),
                getCurrentTrip().getEarnings(),
                status.getHoursAtSea()
            );
            recordOpportunityCosts(cost);
        }


        //finish trip!
        if (status.getHoursAtSea() > 0) {
            final TripRecord finished = memory.getTripLogger().finishTrip(status.getHoursAtSea(), getHomePort(), this);
            //account for the costs
            memory.getYearlyCounter().count(FisherYearlyTimeSeries.VARIABLE_COSTS, finished.getTotalCosts());
            memory.getYearlyCounter().count(FisherYearlyTimeSeries.EARNINGS, finished.getEarnings());

        } else {
            memory.getTripLogger().resetTrip();
        }

        status.setHoursAtSea(0);
        assert isAtPort();

    }

    /**
     * consumes money and record the expenditure in the trip record. Useful for things like gas expenditure and other immediate
     * needs of the current trip in progress
     *
     * @param moneySpent
     */
    public void spendForTrip(final double moneySpent) {
        spendExogenously(moneySpent);
        memory.getTripLogger().recordCosts(moneySpent);
        getDailyCounter().count(FisherYearlyTimeSeries.CASH_FLOW_COLUMN, -moneySpent);

    }

    /**
     * tell the logger you incurred some travel costs without actually touching the bank balance. This is useful for accounting
     * for things like opportunity costs
     *
     * @param implicitCost implicit expenditure
     */
    private void recordOpportunityCosts(final double implicitCost) {
        memory.getTripLogger().recordOpportunityCosts(implicitCost);
    }

    /**
     * consumes the money but doesn't record the cost in the trip record. This is useful for expenditures like
     * bank interest payments, quota buying and selling and other things that are not due to the immediate needs of the trip
     * being taken
     *
     * @param moneySpent
     */
    public void spendExogenously(final double moneySpent) {
        status.setBankBalance(status.getBankBalance() - moneySpent);
        getDailyCounter().count(FisherYearlyTimeSeries.CASH_FLOW_COLUMN, -moneySpent);

    }

    public FisherDailyCounter getDailyCounter() {
        return memory.getDailyCounter();
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param model the model
     * @return true if the fisherman wants to leave port.
     */
    public boolean shouldFisherLeavePort(final FishState model) {
        assert isAtPort();
        assert !isFuelEmergencyOverride();
        return !status.isAnyEmergencyFlagOn() && departingStrategy.shouldFisherLeavePort(
            this,
            model,
            model.getRandom()
        );
    }

    /**
     * tell the fisher to check his destination and update it if necessary. If the regulation forbid us to be at sea
     * the destination is always port
     *
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    public void updateDestination(final FishState model, final Action currentAction) {


        //if you are not allowed at sea or you are running out of gas, go home
        if (
            (!status.getRegulation().allowedAtSea(this, model) && !cheater)
                || status.isAnyEmergencyFlagOn())
            status.setDestination(status.getHomePort().getLocation());
        else
            status.setDestination(
                destinationStrategy.chooseDestination(this, status.getRandom(), model, currentAction));
        Preconditions.checkNotNull(status.getDestination(), "Destination can never be null!");
    }

    public boolean isAllowedAtSea() {
        if (state == null) //you aren't allowed if you haven't started
            return false;
        else
            return getRegulation().allowedAtSea(this, state);
    }

    public double getHoursTravelledToday() {
        return equipment.getBoat().getHoursTravelledToday();
    }

    /**
     * Getter for property 'discardingStrategy'.
     *
     * @return Value for property 'discardingStrategy'.
     */
    public DiscardingStrategy getDiscardingStrategy() {
        return discardingStrategy;
    }

    public void setDiscardingStrategy(final DiscardingStrategy newStrategy) {

        final DiscardingStrategy old = this.discardingStrategy;
        this.discardingStrategy = newStrategy;

        if (state != null) //if we have started already
        {
            old.turnOff(this); //turn off old strategy
            discardingStrategy.start(state, this);
        }
    }

    public DepartingStrategy getDepartingStrategy() {
        return departingStrategy;
    }

    public void setDepartingStrategy(final DepartingStrategy newStrategy) {
        final DepartingStrategy old = this.departingStrategy;
        this.departingStrategy = newStrategy;
        if (state != null) //if we have started already
        {
            old.turnOff(this); //turn off old strategy
            departingStrategy.start(state, this);
        }
    }

    public DestinationStrategy getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(final DestinationStrategy newStrategy) {

        final DestinationStrategy old = this.destinationStrategy;
        this.destinationStrategy = newStrategy;

        if (state != null) //if we have started already
        {
            old.turnOff(this); //turn off old strategy
            destinationStrategy.start(state, this);
        }
    }

    public FishingStrategy getFishingStrategy() {
        return fishingStrategy;
    }

    public void setFishingStrategy(final FishingStrategy newStrategy) {

        final FishingStrategy old = this.fishingStrategy;
        this.fishingStrategy = newStrategy;
        if (state != null) //if we have started already
        {
            old.turnOff(this); //turn off old strategy
            fishingStrategy.start(state, this);
        }
    }

    /**
     * how many pounds of a specific species are we carrying
     *
     * @param species the species
     * @return lbs of species carried
     */
    public double getTotalWeightOfCatchInHold(final Species species) {
        return equipment.getHold().getWeightOfCatchInHold(species);
    }

    /**
     * how much can this fish hold
     *
     * @return the maximum load
     */
    public double getMaximumHold() {
        return equipment.getHold().getMaximumLoad();
    }

    /**
     * unload all the cargo
     *
     * @return the cargo as a catch object
     */
    public Catch unload() {
        return equipment.getHold().unload();
    }

    /**
     * tell the fisher to use its gear to fish at current location. It stores everything in the hold
     *
     * @param modelBiology      the global biology object
     * @param hoursSpentFishing hours spent on this activity
     * @param state             the model
     * @return the fish caught and stored (barring overcapacity)
     */
    public void fishHere(final GlobalBiology modelBiology, final int hoursSpentFishing, final FishState state) {

        //compute the catches (but kill nothing yet)
        fishHere(modelBiology, hoursSpentFishing, state, status.getLocation());


    }

    /**
     * tell the fisher to use its gear to fish at local biology provided by the caller. This is useful if you
     * are not specifically referring to catching stuff straight out of the SeaTile
     *
     * @param modelBiology      the global biology object
     * @param hoursSpentFishing hours spent on this activity
     * @param state             the model
     * @param localBiology      this is what will need to "react" to the amount caught
     * @return the fish caught and stored (barring overcapacity)
     */
    public Entry<Catch, Catch> fishHere(
        final GlobalBiology modelBiology,
        final int hoursSpentFishing,
        final FishState state,
        final LocalBiology localBiology
    ) {
        //preamble
        final SeaTile here = status.getLocation();
        assert here.isWater() : "can't fish on land!";
        //Preconditions.checkState(here.isWater(), );
        //compute the catches (but kill nothing yet)
        final Entry<Catch, Catch> catchesAndKept = computeCatchesHere(
            status.getLocation(),
            localBiology,
            hoursSpentFishing,
            modelBiology,
            state
        );
        //make local react to catches (involves killing, usually)
        removeFishAfterFishing(modelBiology, catchesAndKept.getKey(), catchesAndKept.getValue(), localBiology);
        //pull the fish up, store it, and burn fuel
        recordAndHaulCatch(hoursSpentFishing, here, catchesAndKept.getKey(), catchesAndKept.getValue(), state);
        return catchesAndKept;
    }

    private Entry<Catch, Catch> computeCatchesHere(
        final SeaTile context,
        final LocalBiology biology,
        final int hoursSpentFishing, final GlobalBiology modelBiology, final FishState state
    ) {
        //transfer fish from local to here
        final Catch catchOfTheDay = equipment.getGear().fish(
            this,
            biology,
            context,
            hoursSpentFishing,
            modelBiology
        );
        final Catch kept = discardingStrategy.chooseWhatToKeep(
            context,
            this,
            catchOfTheDay,
            hoursSpentFishing,
            getRegulation(),
            state,
            grabRandomizer()
        );
        return entry(catchOfTheDay, kept);
    }

    private void removeFishAfterFishing(
        final GlobalBiology modelBiology,
        final Catch catchOfTheDay,
        final Catch notDiscarded,
        final LocalBiology biology
    ) {
        if (catchOfTheDay.totalCatchWeight() > FishStateUtilities.EPSILON) {
            biology.reactToThisAmountOfBiomassBeingFished(catchOfTheDay, notDiscarded, modelBiology);
            //now count catches (which isn't necessarilly landings)
            for (final Species species : modelBiology.getSpecies()) {
                getDailyCounter().countCatches(species, catchOfTheDay.getWeightCaught(species));

                if (catchOfTheDay.hasAbundanceInformation() && species.getNumberOfBins() > 0) {
                    getDailyCounter().countLandinngPerBin(species, catchOfTheDay);
                    assert doubleCheckCounters(species);

                }

            }
        }
    }

    private void recordAndHaulCatch(
        final int hoursSpentFishing,
        final SeaTile here,
        final Catch catchOfTheDay,
        final Catch notDiscarded,
        final FishState model
    ) {
        //learn, record and collect
        //record it
        final FishingRecord record = new FishingRecord(hoursSpentFishing,
            here, catchOfTheDay
        );
        memory.getTripLogger().recordFishing(record);

        //now let regulations and the hold deal with it
        status.getRegulation().reactToFishing(here, this, catchOfTheDay, notDiscarded, hoursSpentFishing, model);
        load(notDiscarded);

        //consume gas
        final double litersBurned = equipment.getGear().getFuelConsumptionPerHourOfFishing(
            this,
            equipment.getBoat(),
            here
        ) * hoursSpentFishing;
        if (litersBurned > 0)
            consumeFuel(litersBurned);

        memory.getYearlyCounter().count(FisherYearlyTimeSeries.EFFORT, hoursSpentFishing);
        memory.getDailyCounter().count(FisherYearlyTimeSeries.EFFORT, hoursSpentFishing);
    }

    /**
     * weird name to avoid beans
     */
    public MersenneTwisterFast grabRandomizer() {
        return status.getRandom();
    }

    private boolean doubleCheckCounters(final Species species) {

        final double landings = getDailyCounter().getCatchesPerSpecie(species.getIndex());
        double sum = 0;
        for (int i = 0; i < species.getNumberOfBins(); i++)
            sum += getDailyCounter().getSpecificLandings(species, i);
//        System.out.println(landings);
//        System.out.println(sum);
        return Math.abs(sum - landings) < .01;

    }

    /**
     * store the catch
     *
     * @param caught the catch
     */
    public void load(final Catch caught) {
        equipment.getHold().load(caught);
    }

    public Gear getGear() {
        return equipment.getGear();
    }

    public void setGear(final Gear gear) {
        equipment.setGear(gear);
    }

    /**
     * @return true if destination == location
     */
    public boolean isAtDestination() {
        return status.getDestination().equals(status.getLocation());
    }

    public TimeSeries<Fisher> getYearlyData() {
        return memory.getYearlyTimeSeries();

    }

    /**
     * shortcut for getYearlyData().getLatestObservation(columnName)
     */
    public double getLatestYearlyObservation(final String columnName) {
        return memory.getYearlyTimeSeries().getLatestObservation(columnName);
    }

    public double getBankBalance() {
        return status.getBankBalance();
    }

    public void earn(final double moneyEarned) {
        status.setBankBalance(status.getBankBalance() + moneyEarned);
        getDailyCounter().count(FisherYearlyTimeSeries.CASH_FLOW_COLUMN, moneyEarned);

    }

    /**
     * grabs the data and learns about profits and such
     *
     * @param info information about a trade
     */
    public void processTradeData(final TradeInfo info) {

        final Species species = info.getSpecies();

        memory.getDailyCounter().countLanding(species, info.getBiomassTraded());
        memory.getDailyCounter().countEarnings(species, info.getMoneyExchanged());
        memory.getTripLogger().recordEarnings(species.getIndex(), info.getBiomassTraded(),
            info.getMoneyExchanged()
        );

    }

    public double balanceXDaysAgo(final int daysAgo) {
        //    Preconditions.checkArgument(dailyTimeSeries.numberOfObservations() >daysAgo);
        return getDailyData().getColumn(FisherYearlyTimeSeries.CASH_COLUMN).getDatumXStepsAgo(daysAgo);
    }

    public FisherDailyTimeSeries getDailyData() {
        return memory.getDailyTimeSeries();
    }

    public void addYearlyAdaptation(final Adaptation a) {
        yearlyAdaptation.registerAdaptation(a);
    }

    public void addBiMonthlyAdaptation(final Adaptation a) {
        bimonthlyAdaptation.registerAdaptation(a);
    }

    public void addPerTripAdaptation(final Adaptation a) {
        tripAdaptation.registerAdaptation(a);
    }

    public void removeYearlyAdaptation(final Adaptation a) {
        yearlyAdaptation.removeAdaptation(a);
    }

    public void removeBiMonthlyAdaptation(final Adaptation a) {
        bimonthlyAdaptation.removeAdaptation(a);
    }

    public void removePerTripAdaptation(final Adaptation a) {
        tripAdaptation.removeAdaptation(a);
    }

    public void addTripListener(final TripListener listener) {
        memory.getTripLogger().addTripListener(listener);
    }

    public void removeTripListener(final TripListener listener) {
        memory.getTripLogger().removeTripListener(listener);
    }

    public List<TripRecord> getFinishedTrips() {
        return memory.getTripLogger().getFinishedTrips();
    }

    public List<SharedTripRecord> getSharedTrips() {
        return memory.getSharedTrips();
    }

    public List<SharedTripRecord> getTripsSharedWith(final Fisher friend) {
        final Collection<Fisher> friends = this.getDirectedFriends();
        if (friends.contains(friend))
            return memory.getTripsSharedWith(friend);
        else
            return new ArrayList<>();
    }

    /**
     * return all neighbors of this agent where there a directed edge from this fisher to his neighbors
     *
     * @return a collection of agents
     */
    public Collection<Fisher> getDirectedFriends() {
        return status.getNetwork().getDirectedNeighbors(this);
    }

    public void shareTrip(final TripRecord trip, final boolean allFriends, final Collection<Fisher> sharedFriends) {
        memory.shareTrip(trip, allFriends, sharedFriends);
    }

    public String getAction() {
        return status.getAction().getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "Fisher " + fisherID + "; " + String.join(" - ", getTagsList());
    }

    /**
     * Getter for property 'tags'.
     *
     * @return Value for property 'tags'.
     */
    public List<String> getTagsList() {
        return tags;
    }

    @Override
    public Set<String> getTags() {
        return tagSet;
    }

    public void refreshTagSet() {
        tagSet = ImmutableSet.copyOf(getTagsList());
    }

    /**
     * return all neighbors of this agent in the social network ignoring the direction of the edges
     */
    public Collection<Fisher> getAllFriends() {
        return status.getNetwork().getAllNeighbors(this);
    }

    public double getYearlyCounterColumn(final String columnName) {
        return memory.getYearlyCounter().getColumn(columnName);
    }

    public Counter getYearlyCounter() {
        return memory.getYearlyCounter();
    }

    public boolean isGoingToPort() {
        return status.isGoingToPort();
    }

    /**
     * basically changes the size of the maximum load but takes care of transferring whatever we were holding to the
     * new hold
     *
     * @param newHold
     */
    public void changeHold(final Hold newHold) {

        //unload old hold
        final Catch oldHaul = equipment.getHold().unload();

        equipment.setHold(newHold);
        //load the new hold
        equipment.getHold().load(oldHaul);
    }

    public double predictUnitProfit(final int specieIndex) {
        return status.getProfitPerUnitPredictor()[specieIndex].predict();
    }

    public double predictDailyCatches(final int specieIndex) {
        return status.getDailyCatchesPredictor()[specieIndex].predict();
    }

    public double probabilityDailyCatchesBelowLevel(final int specieIndex, final double level) {
        return status.getDailyCatchesPredictor()[specieIndex].probabilityBelowThis(level);
    }

    public double probabilitySumDailyCatchesBelow(final int specieIndex, final double level, final int daysToSum) {
        return status.getDailyCatchesPredictor()[specieIndex].probabilitySumBelowThis(level, daysToSum);
    }

    public double predictDailyProfits() {
        return status.getDailyProfitsPredictor().predict();
    }

    public void assignDailyProfitsPredictor(final Predictor dailyProfitsPredictor) {

        if (state != null) {
            this.status.getDailyProfitsPredictor().turnOff(this);
            dailyProfitsPredictor.start(state, this);
        }

        status.setDailyProfitsPredictor(dailyProfitsPredictor);


    }

    public void setDailyCatchesPredictor(final int specieIndex, final Predictor newPredictor) {
        if (state != null) {
            newPredictor.start(state, this);
            status.getDailyCatchesPredictor()[specieIndex].turnOff(this);
        }
        status.getDailyCatchesPredictor()[specieIndex] = newPredictor;

    }

    public void resetDailyCatchesPredictors() {
        for (final Predictor predictor : status.getDailyCatchesPredictor())
            predictor.reset();
    }

    public void setProfitPerUnitPredictor(final int specieIndex, final Predictor newPredictor) {
        if (state != null) {
            newPredictor.start(state, this);
            status.getProfitPerUnitPredictor()[specieIndex].turnOff(this);
        }
        status.getProfitPerUnitPredictor()[specieIndex] = newPredictor;

    }

    public WeatherEmergencyStrategy getWeatherStrategy() {
        return weatherStrategy;
    }

    public void setWeatherStrategy(final WeatherEmergencyStrategy weatherStrategy) {


        final WeatherEmergencyStrategy old = this.weatherStrategy;

        this.weatherStrategy = weatherStrategy;
        if (state != null) {
            old.turnOff(this);

            weatherStrategy.start(state, this);
        }

    }

    public Fisher replaceFriend(
        final Fisher friendToReplace,
        final boolean ignoreDirection
    ) {
        return status.getNetwork().replaceFriend(
            this,
            friendToReplace,
            ignoreDirection,
            state.getRandom(),
            state.getFishers()
        );
    }

    /**
     * force the destination to be port, this is used only if your current destination is unreachable (because it's land or landlocked)
     */
    public void setDestinationForPort() {
        status.setDestination(getHomePort().getLocation());
    }

    /**
     * @param location
     * @return
     */
    public TripRecord rememberLastTripHere(final SeaTile location) {
        return memory.rememberLastTripHere(location);
    }

    public Map<SeaTile, LocationMemory<TripRecord>> rememberAllTrips() {
        return memory.rememberAllTrips();
    }

    /**
     * Ask the fisher what is the best tile with respect to trips made
     *
     * @param comparator how should the fisher compare each tile remembered
     */
    public SeaTile getBestSpotForTripsRemembered(
        final Comparator<LocationMemory<TripRecord>> comparator
    ) {
        return memory.getBestSpotForTripsRemembered(comparator);
    }

    public void addFeatureExtractor(
        final String nameOfFeature,
        final FeatureExtractor<SeaTile> extractor
    ) {
        memory.addFeatureExtractor(nameOfFeature, extractor);
    }

    public FeatureExtractor<SeaTile> removeFeatureExtractor(final String nameOfFeature) {
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
     *
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    public boolean isAllowedToFishHere(final SeaTile tile, final FishState model) {
        return status.isAllowedToFishHere(this, tile, model);
    }

    public boolean isAllowedReputationToFishHere(final SeaTile tile, final FishState model) {
        return status.isAllowedReputationToFishHere(this, tile, model);
    }

    public int countTerritories() {
        return status.countTerritories();
    }

    public boolean isAllowedByCommunityStandardsToFishHere(final SeaTile tile, final FishState model) {
        return status.isAllowedByCommunityStandardsToFishHere(this, tile, model);
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
    public void setGearStrategy(final GearStrategy gearStrategy) {

        if (state != null && this.gearStrategy != gearStrategy)
            this.gearStrategy.turnOff(this);


        this.gearStrategy = gearStrategy;
        if (state != null)
            this.gearStrategy.start(state, this);

    }

    /**
     * Check if you want to change your gear now!
     *
     * @param random        randomizer
     * @param model         the model
     * @param currentAction the current action
     */
    public void updateGear(
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {
        Preconditions.checkState(isAtPort(), "Changing Gear out of Port. Not expected!");
        gearStrategy.updateGear(this, random, model, currentAction);
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public LinkedList<Cost> getOpportunityCosts() {
        return status.getOpportunityCosts();
    }

    public double getExpectedFuelConsumption(final double distanceKM) {
        return equipment.getBoat().expectedFuelConsumption(distanceKM);
    }

    public int numberOfDailyObservations() {
        return memory.numberOfDailyObservations();
    }

    /**
     * keep that memory in the database. The key cannot be currently in use!
     *
     * @param key  the key for the object
     * @param item the object to store
     */
    public void memorize(final String key, final Object item) {
        memory.memorize(key, item);
    }

    /**
     * removes the memory associated with that key
     *
     * @param key
     */
    public void forget(final String key) {
        memory.forget(key);
    }

    /**
     * returns the object associated with this key
     *
     * @param key
     */
    public Object remember(final String key) {
        return memory.remember(key);
    }

    /**
     * registers visit (if the memory exists)
     *
     * @param group
     * @param day
     */
    public void registerVisit(final int group, final int day) {
        memory.registerVisit(group, day);
    }

    /**
     * registers visit (if the memory exists)
     *
     * @param tile
     * @param day
     */
    public void registerVisit(final SeaTile tile, final int day) {
        memory.registerVisit(tile, day);
    }

    public DiscretizedLocationMemory getDiscretizedLocationMemory() {
        return memory.getDiscretizedLocationMemory();
    }

    public void setDiscretizedLocationMemory(final DiscretizedLocationMemory discretizedLocationMemory) {
        memory.setDiscretizedLocationMemory(discretizedLocationMemory);
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
    public void setExogenousEmergencyOverride(final boolean exogenousEmergencyOverride) {
        status.setExogenousEmergencyOverride(exogenousEmergencyOverride);
    }

    public double getTotalWeightOfCatchInHold() {
        return equipment.getTotalWeightOfCatchInHold();
    }

    public double getWeightOfCatchInHold(final Species species) {
        return equipment.getWeightOfCatchInHold(species);
    }

    public void teleport(final SeaTile tile) {
        status.setLocation(tile);
    }

    /**
     * grabs the state reference from the fisher
     *
     * @return
     */
    public FishState grabState() {
        return state;
    }

    public boolean canAndWantToFishHere() {
        return (getRegulation().canFishHere(this, getLocation(), state) || isCheater())
            &&
            this.shouldIFish(state);
    }

    /**
     * Getter for property 'cheater'.
     *
     * @return Value for property 'cheater'.
     */
    public boolean isCheater() {
        return cheater;
    }

    public boolean shouldIFish(final FishState state) {
        return !status.isAnyEmergencyFlagOn() && fishingStrategy.shouldFish(
            this,
            state.getRandom(),
            state,
            getCurrentTrip()
        );

    }

    /**
     * Setter for property 'cheater'.
     *
     * @param cheater Value to set for property 'cheater'.
     */
    public void setCheater(final boolean cheater) {
        this.cheater = cheater;
    }

    public Hold getHold() {
        return equipment.getHold();
    }

    public void setHold(final Hold hold) {
        equipment.setHold(hold);
    }

    public double getCountedLandingsPerBin(final Species species, final int bin) {
        return getDailyCounter().getSpecificLandings(species, bin);
    }

    public double getHoursAtSeaThisYear() {
        return memory.getHoursAtSeaThisYear() + getHoursAtSea();
    }

    public double getHoursAtSea() {
        return status.getHoursAtSea();
    }

    public boolean isTerritory(final SeaTile tile) {
        if (status.getReputationalRisk() == null)
            return false;
        else
            return status.getReputationalRisk().isTerritory(tile);
    }

    /**
     * Getter for property 'additionalVariables'.
     *
     * @return Value for property 'additionalVariables'.
     */
    public HashMap<String, Object> getAdditionalVariables() {
        return status.getAdditionalVariables();
    }

    /**
     * you've been active if you are currently out at sea or you have finished at least a trip
     * in the past 365 days!
     *
     * @return
     */
    public boolean hasBeenActiveThisYear() {

        if (getLastFinishedTrip() == null)
            return false;

        return getLastFinishedTrip().getTripDay() > state.getDay() - 364 ||
            (!getCurrentTrip().isCompleted() && getHoursAtSea() > 0);
    }

    public TripRecord getLastFinishedTrip() {
        return memory.getTripLogger().getLastFinishedTrip();
    }

    public double getExpectedAdditionalCosts(
        final double additionalTripHours,
        final double additionalEffortHours,
        final double additionalKmTravelled
    ) {


        double totalCost = GasCost.expectedAdditionalGasCosts(this, additionalKmTravelled);
        for (final Cost otherCosts : getAdditionalTripCosts()) {
            totalCost += otherCosts.expectedAdditionalCosts(
                this,
                additionalTripHours,
                additionalEffortHours,
                additionalKmTravelled
            );
        }
        return totalCost;
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
     * Currently returns the Fisher's first tag, which currently contains the ID in
     * EPO scenarios, and falls back on the numeric ID if no tags are defined.
     * <p>
     * TODO: this should be fixed by having a proper String id field in the Fisher class
     * sooner rather than later.
     *
     * @return the Fisher's id.
     */
    @Override
    public String getId() {
        return getTagsList().stream()
            .findFirst()
            .orElse(String.valueOf(getID()));
    }

    public int getID() {
        return fisherID;
    }
}

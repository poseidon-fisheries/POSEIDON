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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.selfanalysis.FixedPredictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.Predictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.model.restrictions.ReputationalRestrictions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * contains all the transitory variables of  a fisher including:
 * <ul>
 *     <li> Location and Destination</li>
 *     <li> Action being taken </li>
 *     <li> Regulations followed </li>
 *     <li> Finances </li>
 * </ul>
 */
public class FisherStatus implements Serializable {


    private static final long serialVersionUID = 5108217722345643799L;
    /**
     * randomizer
     */
    private final MersenneTwisterFast random;
    /**
     * anything that doesn't fit a counter or a time series can be stored in this map which is nothing else than a
     * white board to scribble fisher parameters in.
     * Anything we don't necessarilly want to stay within a strategy object or that it affects fisher stuff across
     * objects
     */
    private final HashMap<String, Object> additionalVariables = new HashMap<>();
    /**
     * list of costs to pay at the end of the trip. Any amount computed is spent. Wages would be a good example.
     */
    private final LinkedList<Cost> additionalTripCosts = new LinkedList<>();
    /**
     * list of additional costs to account for on trip record but not to actually burn money on.
     */
    private final LinkedList<Cost> opportunityCosts = new LinkedList<>();
    /**
     * the location of the port!
     */
    private SeaTile location;
    /**
     * set by events (right now by enforcement).
     * It forces agents back to port!
     */
    private boolean exogenousEmergencyOverride = false;
    /**
     * Home is where the port is
     */
    private Port homePort;
    /**
     * if it is moving somewhere, the destination is stored here.
     */
    private SeaTile destination;
    /**
     * the regulation object to obey
     */
    private Regulation regulation;
    /**
     * The community Standards to obey
     */

    private RegionalRestrictions communalStandards;
    /**
     * The reputational risks to observe
     */

    private ReputationalRestrictions reputationalRisk;
    /**
     * the state of the fisher: the next action they are taking
     */
    private Action action;
    /**
     * hours spent away from port current trip
     */
    private double hoursAtSea;
    private double hoursAtPort;
    /**
     * the cash owned by the firm
     */
    private double bankBalance;
    private SocialNetwork network;
    private Predictor[] dailyCatchesPredictor;
    private Predictor[] profitPerUnitPredictor;
    private Predictor dailyProfitsPredictor = new FixedPredictor(Double.NaN);
    /**
     * when this flag is on, the agent believes that it MUST return home or it will lspiRun out of fuel. All other usual
     * decisions about destination are ignored.
     */
    private boolean fuelEmergencyOverride = false;
    /**
     * when this flag is on, the agent believes it MUST return home to avoid the worst part of a storm. All other decisions
     * are otherwise ignored
     */
    private boolean weatherEmergencyOverride = false;

    public FisherStatus(
        final MersenneTwisterFast random, final Regulation regulation, final Action action, final Port homePort, final SeaTile location,
        final SeaTile destination,
        final double hoursAtSea, final double hoursAtPort,
        final double bankBalance, final boolean fuelEmergencyOverride, final SocialNetwork network
    ) {
        this.location = location;
        this.homePort = homePort;
        this.destination = destination;
        this.random = random;
        this.regulation = regulation;
        this.action = action;
        this.hoursAtSea = hoursAtSea;
        this.hoursAtPort = hoursAtPort;
        this.network = network;
        this.bankBalance = bankBalance;
        this.fuelEmergencyOverride = fuelEmergencyOverride;
    }

    /**
     * default initializer used by the fisher constructor. Initializes most stuff automagically except
     * for the social network which is set to null (this is because we need to build fishers before we can
     * create a netowkr for them)
     *
     * @param random     randomizer
     * @param regulation regulation object
     * @param homePort   home port
     */
    public FisherStatus(
        final MersenneTwisterFast random, final Regulation regulation, final Port homePort
    ) {
        this.homePort = homePort;
        this.location = homePort.getLocation();
        this.destination = homePort.getLocation();
        this.random = random;
        this.regulation = regulation;
        this.action = new AtPort();
        this.hoursAtSea = 0;
        this.hoursAtPort = 0;
        this.network = network;
        this.bankBalance = 0;
        this.fuelEmergencyOverride = false;
        this.exogenousEmergencyOverride = false;
        network = null;
    }

    public SeaTile getLocation() {
        return location;
    }

    public void setLocation(final SeaTile location) {
        this.location = location;
    }

    public Port getHomePort() {
        return homePort;
    }

    /**
     * Setter for property 'homePort'.
     *
     * @param homePort Value to set for property 'homePort'.
     */
    public void setHomePort(final Port homePort) {
        this.homePort = homePort;
    }

    public SeaTile getDestination() {
        return destination;
    }

    public void setDestination(final SeaTile destination) {
        this.destination = destination;
    }

    public MersenneTwisterFast getRandom() {
        return random;
    }

    public Regulation getRegulation() {
        return regulation;
    }

    public void setRegulation(final Regulation regulation) {
        this.regulation = regulation;
    }

    public RegionalRestrictions getCommunalStandards() {
        return communalStandards;
    }

    public void setCommunalStandards(final RegionalRestrictions communalStandards) {
        this.communalStandards = communalStandards;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    public double getHoursAtSea() {
        return hoursAtSea;
    }

    public void setHoursAtSea(final double hoursAtSea) {
        this.hoursAtSea = hoursAtSea;
    }

    public double getHoursAtPort() {
        return hoursAtPort;
    }
            /*MovingAveragePredictor.dailyMAPredictor("Predicted Daily Catches",
                                                                                            fisher -> fisher.getDailyCounter().getLandingsPerSpecie(0),
                                                                                            90);
                                                                                            */

    public void setHoursAtPort(final double hoursAtPort) {
        this.hoursAtPort = hoursAtPort;
    }
            /*
            MovingAveragePredictor.perTripMAPredictor("Predicted Unit Profit",
                                                                                               fisher -> fisher.getLastFinishedTrip().getUnitProfitPerSpecie(
                                                                                                       0),
                                                                                               30);
                                                                                               */

    public double getBankBalance() {
        return bankBalance;
    }
            /*
            MovingAveragePredictor.dailyMAPredictor("Predicted Daily Profits",
                                                                                      fisher ->
                                                                                              fisher.getDailyData().
                                                                                                      getColumn(
                                                                                                              FisherYearlyTimeSeries.CASH_FLOW_COLUMN).getLatest(),
                                                                                      365);
*/

    public void setBankBalance(final double bankBalance) {
        this.bankBalance = bankBalance;
    }

    public SocialNetwork getNetwork() {
        return network;
    }

    public void setNetwork(final SocialNetwork network) {
        this.network = network;
    }

    public boolean isFuelEmergencyOverride() {
        return fuelEmergencyOverride;
    }

    public void setFuelEmergencyOverride(final boolean fuelEmergencyOverride) {
        this.fuelEmergencyOverride = fuelEmergencyOverride;
    }

    public boolean isWeatherEmergencyOverride() {
        return weatherEmergencyOverride;
    }

    public void setWeatherEmergencyOverride(final boolean weatherEmergencyOverride) {
        this.weatherEmergencyOverride = weatherEmergencyOverride;
    }

    /**
     * whenever any of these flag is set to true, the agent just goes back home
     *
     * @return
     */
    public boolean isAnyEmergencyFlagOn() {
        return fuelEmergencyOverride || weatherEmergencyOverride || exogenousEmergencyOverride;
    }

    /**
     * @return true if destination == location
     */
    public boolean isAtDestination() {
        return destination.equals(location);
    }

    public boolean isGoingToPort() {
        return destination.equals(homePort.getLocation());
    }

    public boolean isAtPort() {
        return homePort.getLocation().equals(location);
    }

    public FisherStatus makeCopy() {
        return new FisherStatus(
            random,
            regulation.makeCopy(),
            action,
            homePort,
            location,
            destination,
            hoursAtSea,
            hoursAtPort,
            bankBalance,
            fuelEmergencyOverride,
            network
        );
    }

    /**
     * Can this fisher be at sea?
     *
     * @param fisher the  fisher
     * @param model  the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    public boolean isAllowedAtSea(final Fisher fisher, final FishState model) {
        return regulation.allowedAtSea(fisher, model);
    }

    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    public boolean isAllowedToFishHere(final Fisher agent, final SeaTile tile, final FishState model) {
        return regulation.canFishHere(agent, tile, model);
    }

    public boolean isAllowedByCommunityStandardsToFishHere(final Fisher agent, final SeaTile tile, final FishState model) {
        if (communalStandards == null) System.out.println("My communal standards are not set!");
        return communalStandards.canFishHere(agent, tile, model);
    }

    public boolean isAllowedReputationToFishHere(final Fisher agent, final SeaTile tile, final FishState model) {
        return reputationalRisk.canFishHere(agent, tile, model);
    }

    /**
     * Getter for property 'exogenousEmergencyOverride'.
     *
     * @return Value for property 'exogenousEmergencyOverride'.
     */
    public boolean isExogenousEmergencyOverride() {
        return exogenousEmergencyOverride;
    }

    /**
     * Setter for property 'exogenousEmergencyOverride'.
     *
     * @param exogenousEmergencyOverride Value to set for property 'exogenousEmergencyOverride'.
     */
    public void setExogenousEmergencyOverride(final boolean exogenousEmergencyOverride) {
        this.exogenousEmergencyOverride = exogenousEmergencyOverride;
    }

    /**
     * Getter for property 'additionalTripCosts'.
     *
     * @return Value for property 'additionalTripCosts'.
     */
    public LinkedList<Cost> getAdditionalTripCosts() {
        return additionalTripCosts;
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public LinkedList<Cost> getOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * Getter for property 'dailyCatchesPredictor'.
     *
     * @return Value for property 'dailyCatchesPredictor'.
     */
    public Predictor[] getDailyCatchesPredictor() {
        return dailyCatchesPredictor;
    }

    /**
     * Setter for property 'dailyCatchesPredictor'.
     *
     * @param dailyCatchesPredictor Value to set for property 'dailyCatchesPredictor'.
     */
    public void setDailyCatchesPredictor(final Predictor[] dailyCatchesPredictor) {
        this.dailyCatchesPredictor = dailyCatchesPredictor;
    }

    /**
     * Getter for property 'profitPerUnitPredictor'.
     *
     * @return Value for property 'profitPerUnitPredictor'.
     */
    public Predictor[] getProfitPerUnitPredictor() {
        return profitPerUnitPredictor;
    }

    /**
     * Setter for property 'profitPerUnitPredictor'.
     *
     * @param profitPerUnitPredictor Value to set for property 'profitPerUnitPredictor'.
     */
    public void setProfitPerUnitPredictor(final Predictor[] profitPerUnitPredictor) {
        this.profitPerUnitPredictor = profitPerUnitPredictor;
    }

    /**
     * Getter for property 'dailyProfitsPredictor'.
     *
     * @return Value for property 'dailyProfitsPredictor'.
     */
    public Predictor getDailyProfitsPredictor() {
        return dailyProfitsPredictor;
    }

    /**
     * Setter for property 'dailyProfitsPredictor'.
     *
     * @param dailyProfitsPredictor Value to set for property 'dailyProfitsPredictor'.
     */
    public void setDailyProfitsPredictor(final Predictor dailyProfitsPredictor) {
        this.dailyProfitsPredictor = dailyProfitsPredictor;
    }

    /**
     * Getter for property 'additionalVariables'.
     *
     * @return Value for property 'additionalVariables'.
     */
    public HashMap<String, Object> getAdditionalVariables() {
        return additionalVariables;
    }

    public int countTerritories() {
        return getReputationalRisk().countTerritory();
    }

    public ReputationalRestrictions getReputationalRisk() {
        return reputationalRisk;
    }

    public void setReputationalRisk(final ReputationalRestrictions reputationalRisk) {
        this.reputationalRisk = reputationalRisk;
    }
}

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

import ec.util.MersenneTwisterFast;
import org.metawidget.inspector.annotation.UiHidden;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.selfanalysis.FixedPredictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.Predictor;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.io.Serializable;
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





    /**
     * the location of the port!
     */
    private SeaTile location;

    /**
     * set by events (right now by enforcement).
     * It forces agents back to port!
     */
    private boolean exogenousEmergencyOverride = false;

    public SeaTile getLocation() {
        return location;
    }

    public void setLocation(SeaTile location) {
        this.location = location;
    }

    /**
     * Home is where the port is
     */
    private Port homePort;

    public Port getHomePort() {
        return homePort;
    }


    /**
     * if it is moving somewhere, the destination is stored here.
     */
    private SeaTile destination;

    public SeaTile getDestination() {
        return destination;
    }

    public void setDestination(SeaTile destination) {
        this.destination = destination;
    }

    /**
     * randomizer
     */
    @UiHidden
    private final MersenneTwisterFast random;

    public MersenneTwisterFast getRandom() {
        return random;
    }


    /**
     * the regulation object to obey
     */
    private Regulation regulation;

    public Regulation getRegulation() {
        return regulation;
    }

    public void setRegulation(Regulation regulation) {
        this.regulation = regulation;
    }

    /**
     * the state of the fisher: the next action they are taking
     */
    private Action action;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * hours spent away from port current trip
     */
    private double hoursAtSea;

    public double getHoursAtSea() {
        return hoursAtSea;
    }


    public void setHoursAtSea(double hoursAtSea) {
        this.hoursAtSea = hoursAtSea;
    }

    private double hoursAtPort;

    public double getHoursAtPort() {
        return hoursAtPort;
    }

    public void setHoursAtPort(double hoursAtPort) {
        this.hoursAtPort = hoursAtPort;
    }

    /**
     * the cash owned by the firm
     */
    private double bankBalance;

    public double getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    private SocialNetwork network;

    public SocialNetwork getNetwork() {
        return network;
    }

    public void setNetwork(SocialNetwork network) {
        this.network = network;
    }




    /**
     * list of costs to pay at the end of the trip. Any amount computed is spent. Wages would be a good example.
     */
    private final LinkedList<Cost> additionalTripCosts = new LinkedList<>();

    /**
     * list of additional costs to account for on trip record but not to actually burn money on.
     */
    private final LinkedList<Cost> opportunityCosts = new LinkedList<>();



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
                                                                                                              FisherYearlyTimeSeries.CASH_FLOW_COLUMN).getLatest(),
                                                                                      365);
*/


    /**
     * when this flag is on, the agent believes that it MUST return home or it will lspiRun out of fuel. All other usual
     * decisions about destination are ignored.
     */
    private boolean fuelEmergencyOverride = false;

    public boolean isFuelEmergencyOverride() {
        return fuelEmergencyOverride;
    }

    public void setFuelEmergencyOverride(boolean fuelEmergencyOverride) {
        this.fuelEmergencyOverride = fuelEmergencyOverride;
    }


    /**
     * when this flag is on, the agent believes it MUST return home to avoid the worst part of a storm. All other decisions
     * are otherwise ignored
     */
    private boolean weatherEmergencyOverride = false;


    public boolean isWeatherEmergencyOverride() {
        return weatherEmergencyOverride;
    }

    public void setWeatherEmergencyOverride(boolean weatherEmergencyOverride) {
        this.weatherEmergencyOverride = weatherEmergencyOverride;
    }

    /**
     * whenever any of these flag is set to true, the agent just goes back home
     * @return
     */
    public boolean isAnyEmergencyFlagOn()
    {
        return fuelEmergencyOverride || weatherEmergencyOverride || exogenousEmergencyOverride;
    }

    public FisherStatus(
            MersenneTwisterFast random, Regulation regulation, Action action, Port homePort, SeaTile location,
            SeaTile destination,
            double hoursAtSea, double hoursAtPort,
            double bankBalance, boolean fuelEmergencyOverride, SocialNetwork network) {
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
     * @param random randomizer
     * @param regulation regulation object
     * @param homePort home port
     */
    public FisherStatus(
            MersenneTwisterFast random, Regulation regulation, Port homePort) {
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
        this.exogenousEmergencyOverride=false;
        network = null;
    }


    /**
     *
     * @return true if destination == location
     */
    public boolean isAtDestination()
    {
        return destination.equals(location);
    }

    public boolean isGoingToPort()
    {
        return destination.equals(homePort.getLocation());
    }

    public boolean isAtPort() {
        return homePort.getLocation().equals(location);
    }


    public FisherStatus makeCopy()
    {
        return new FisherStatus(random,
                                                 regulation.makeCopy(),
                                                 action,
                                                 homePort,
                                                 location,
                                                 destination,
                                                 hoursAtSea,
                                                 hoursAtPort,
                                                 bankBalance,
                                                 fuelEmergencyOverride,
                                                 network);
    }

    /**
     * Can this fisher be at sea?
     * @param fisher the  fisher
     * @param model the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    public boolean isAllowedAtSea(Fisher fisher, FishState model) {
        return regulation.allowedAtSea(fisher, model);
    }

    /**
     * can the agent fish at this location?
     * @param agent the agent that wants to fish
     * @param tile the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    public boolean isAllowedToFishHere(Fisher agent, SeaTile tile, FishState model) {
        return regulation.canFishHere(agent, tile, model);
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
    public void setExogenousEmergencyOverride(boolean exogenousEmergencyOverride) {
        this.exogenousEmergencyOverride = exogenousEmergencyOverride;
    }

    /**
     * Setter for property 'homePort'.
     *
     * @param homePort Value to set for property 'homePort'.
     */
    public void setHomePort(Port homePort) {
        this.homePort = homePort;
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
    public void setDailyCatchesPredictor(Predictor[] dailyCatchesPredictor) {
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
    public void setProfitPerUnitPredictor(Predictor[] profitPerUnitPredictor) {
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
    public void setDailyProfitsPredictor(Predictor dailyProfitsPredictor) {
        this.dailyProfitsPredictor = dailyProfitsPredictor;
    }
}
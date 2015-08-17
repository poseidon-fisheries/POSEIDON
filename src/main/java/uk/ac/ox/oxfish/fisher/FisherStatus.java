package uk.ac.ox.oxfish.fisher;

import ec.util.MersenneTwisterFast;
import org.metawidget.inspector.annotation.UiHidden;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.io.Serializable;

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

    public SeaTile getLocation() {
        return location;
    }

    public void setLocation(SeaTile location) {
        this.location = location;
    }

    /**
     * Home is where the port is
     */
    final private Port homePort;

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
     * hours spent away from port
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
     * when this flag is on, the agent believes that it MUST return home or it will run out of fuel. All other usual
     * decisions about destination are ignored.
     */
    private boolean fuelEmergencyOverride = false;

    public boolean isFuelEmergencyOverride() {
        return fuelEmergencyOverride;
    }

    public void setFuelEmergencyOverride(boolean fuelEmergencyOverride) {
        this.fuelEmergencyOverride = fuelEmergencyOverride;
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
}
package uk.ac.ox.oxfish.fisher;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.strategies.DepartingStrategy;
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

    public Fisher(Port homePort, MersenneTwisterFast random) {
        this.homePort = homePort; this.random = random;
        this.location = homePort.getLocation();
        this.destination = homePort.getLocation();
        homePort.dock(this);//we dock
    }

    public SeaTile getLocation()
    {
        return location;
    }


    @Override
    public void step(SimState simState) {
        FishState fish = (FishState) simState;

        //tell equipment!
        boat.newStep();

    }

    public MersenneTwisterFast getRandom() {
        return random;
    }


    public SeaTile getDestination() {
        return destination;
    }

    public void setDestination(SeaTile destination) {
        this.destination = destination;
    }

    public Port getHomePort() {
        return homePort;
    }

    public Boat getBoat() {
        return boat;
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     * @return true if the fisherman wants to leave port.
     * @param model the model
     */
    public boolean shouldFisherLeavePort(FishState model) {
        return departingStrategy.shouldFisherLeavePort(this, model);
    }
}

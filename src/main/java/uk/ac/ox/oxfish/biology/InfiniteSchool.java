package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.ArrayList;

/**
 * "An infinite school" of fish. It exists in a radius around a point. Moves towards waypoints.
 *
 * Created by carrknight on 11/17/16.
 */
public class InfiniteSchool implements Startable, Steppable {


    private int positionX;

    private int positionY;

    /**
     * the positions this school moves toward
     */
    private ArrayList<Pair<Integer,Integer>> waypoints;

    /**
     * which waypoint we are currently going to
     */
    private int currentWaypoint = 0;

    /**
     * after how many days we move
     */
    private final int speedInDays;

    /**
     * how many cells does it occupy?
     */
    private final double diameterSquared;

    /**
     * biomass of the school per cell. This never gets fished out.
     */
    private final double biomassPerCell;

    /**
     * how many days have we been still at this location?
     */
    private int daysWaiting = 0;

    /**
     * the species simulated by this school
     */
    private final Species species;


    public InfiniteSchool(
            int positionX, int positionY, int speedInDays, double diameter, double biomassPerCell,
            Species species, Pair<Integer, Integer>... waypoints) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.speedInDays = speedInDays;
        this.diameterSquared = diameter*diameter;
        this.biomassPerCell = biomassPerCell;
        this.species = species;

        Preconditions.checkArgument(waypoints.length >= 2);
        this.waypoints = new ArrayList<>(waypoints.length);
        for(Pair<Integer,Integer> waypoint : waypoints)
            this.waypoints.add(waypoint);

        updateWaypoint();
    }

    private Stoppable stoppable;

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Preconditions.checkState(stoppable==null);
        this.stoppable = model.scheduleEveryDay(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(stoppable!=null)
            stoppable.stop();
    }

    @Override
    public void step(SimState simState) {

        daysWaiting++;
        if(daysWaiting >= speedInDays)
        {
            Pair<Integer,Integer> waypoint = waypoints.get(currentWaypoint);

            positionX+= Math.signum(waypoint.getFirst()-positionX);
            positionY+= Math.signum(waypoint.getSecond()-positionY);

            daysWaiting = 0;
            updateWaypoint();
        }

    }


    public void updateWaypoint(){
        Pair<Integer,Integer> waypoint = waypoints.get(currentWaypoint);
        if(positionX==waypoint.getFirst() && positionY==waypoint.getSecond())
        {
            currentWaypoint++;
            if(currentWaypoint>=waypoints.size())
                currentWaypoint=0;
        }


    }


    public boolean contains(SeaTile tile)
    {

        return Math.pow(tile.getGridX() - positionX,2) + Math.pow(tile.getGridY() - positionY,2)<= diameterSquared;
    }

    /**
     * Getter for property 'biomassPerCell'.
     *
     * @return Value for property 'biomassPerCell'.
     */
    public double getBiomassPerCell() {
        return biomassPerCell;
    }

    /**
     * Getter for property 'positionX'.
     *
     * @return Value for property 'positionX'.
     */
    public int getPositionX() {
        return positionX;
    }

    /**
     * Getter for property 'positionY'.
     *
     * @return Value for property 'positionY'.
     */
    public int getPositionY() {
        return positionY;
    }

    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }
}

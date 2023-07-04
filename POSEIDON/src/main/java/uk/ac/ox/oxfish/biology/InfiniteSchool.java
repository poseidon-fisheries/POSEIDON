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

package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * "An infinite school" of fish. It exists in a radius around a point. Moves towards waypoints.
 * <p>
 * Created by carrknight on 11/17/16.
 */
public class InfiniteSchool implements Startable, Steppable {


    private static final long serialVersionUID = 6982466809540285530L;
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
     * the species simulated by this school
     */
    private final Species species;
    /**
     * the positions this school moves toward
     */
    private final ArrayList<Entry<Integer, Integer>> waypoints;
    private int positionX;
    private int positionY;
    /**
     * which waypoint we are currently going to
     */
    private int currentWaypoint = 0;
    /**
     * how many days have we been still at this location?
     */
    private int daysWaiting = 0;
    private Stoppable stoppable;

    @SuppressWarnings("unchecked")
    public InfiniteSchool(
        final int positionX,
        final int positionY,
        final int speedInDays,
        final double diameter,
        final double biomassPerCell,
        final Species species,
        final Entry<Integer, Integer>... waypoints
    ) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.speedInDays = speedInDays;
        this.diameterSquared = diameter * diameter;
        this.biomassPerCell = biomassPerCell;
        this.species = species;

        Preconditions.checkArgument(waypoints.length >= 2);
        this.waypoints = new ArrayList<>(waypoints.length);
        Collections.addAll(this.waypoints, waypoints);

        updateWaypoint();
    }

    public void updateWaypoint() {
        final Entry<Integer, Integer> waypoint = waypoints.get(currentWaypoint);
        if (positionX == waypoint.getKey() && positionY == waypoint.getValue()) {
            currentWaypoint++;
            if (currentWaypoint >= waypoints.size())
                currentWaypoint = 0;
        }


    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        Preconditions.checkState(stoppable == null);
        this.stoppable = model.scheduleEveryDay(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (stoppable != null)
            stoppable.stop();
    }

    @Override
    public void step(final SimState simState) {

        daysWaiting++;
        if (daysWaiting >= speedInDays) {
            final Entry<Integer, Integer> waypoint = waypoints.get(currentWaypoint);

            positionX += Math.signum(waypoint.getKey() - positionX);
            positionY += Math.signum(waypoint.getValue() - positionY);

            daysWaiting = 0;
            updateWaypoint();
        }

    }

    public boolean contains(final SeaTile tile) {

        return Math.pow(tile.getGridX() - positionX, 2) + Math.pow(tile.getGridY() - positionY, 2) <= diameterSquared;
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

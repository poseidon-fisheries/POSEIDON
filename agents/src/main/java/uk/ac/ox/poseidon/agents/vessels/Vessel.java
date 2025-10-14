/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Agent;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Destination;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.*;

@Getter
@Setter
public class Vessel implements Agent, Oriented2D {

    private static final int VESSEL_BEHAVIOUR_ORDERING = 1;
    private final String id;
    private final EventManager eventManager;
    private final Account account;
    private final VesselField vesselField;
    private final PortGrid portGrid;
    private final Map<String, Object> tags = new HashMap<>();

    // Modifiable characteristics
    @NonNull private String name;
    @NonNull private Port homePort;
    @NonNull private Hold<?> hold;
    @NonNull private Gear<?> gear;
    @NonNull private Engine engine;

    // Current state variables
    @Getter(AccessLevel.NONE)
    private final Deque<Behaviour> behaviourStack = new ArrayDeque<>();
    private double heading;
    private Destination destination;
    private boolean active;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    Vessel(
        final String id,
        final @NonNull String name,
        final EventManager eventManager,
        final Account account,
        final VesselField vesselField,
        final PortGrid portGrid,
        final @NonNull Port homePort
    ) {
        this.id = id;
        this.name = name;
        this.eventManager = eventManager;
        this.account = account;
        this.vesselField = vesselField;
        this.portGrid = portGrid;
        this.homePort = homePort;
        setCurrentCell(portGrid.getLocation(homePort));
    }

    @Override
    public double orientation2D() {
        return heading;
    }

    public void setHeadingTowards(
        final Int2D destinationCell
    ) {
        setHeadingTowards(getVesselField().getModelGrid().toPoint(destinationCell));
    }

    private void setHeadingTowards(
        final Double2D destinationPoint
    ) {
        final Double2D location = getPoint();
        final double dx = destinationPoint.x - location.x;
        final double dy = destinationPoint.y - location.y;
        setHeading(Math.atan2(dy, dx));
    }

    public Double2D getPoint() {
        return vesselField.getPoint(this);
    }

    public Coordinate getCoordinate() {
        return vesselField.getModelGrid().toCoordinate(getPoint());
    }

    public Int2D getCell() {
        return vesselField.getCell(this);
    }

    public void setCurrentCell(
        final Int2D cell
    ) {
        vesselField.setCell(this, cell);
    }

    public boolean isAtDestination() {
        return destination != null && getCell().equals(destination.getCell());
    }

    public boolean isAtPort() {
        return portGrid.anyObjectsAt(getCell());
    }

    public void popBehaviour() {
        behaviourStack.pop();
    }

    public void pushBehaviour(final Behaviour behaviour) {
        behaviourStack.push(behaviour);
    }

    public Behaviour currentBehaviour() {
        return behaviourStack.peek();
    }

    public void scheduleNextAction(final TemporalSchedule schedule) {
        while (currentBehaviour() != null) {
            final var action = currentBehaviour().nextAction(this, schedule.getDateTime());
            if (action != null) {
                action.init();
                schedule.scheduleOnceIn(action.getDuration(), action, VESSEL_BEHAVIOUR_ORDERING);
                break;
            } else {
                popBehaviour();
            }
        }
    }

    public void setDestination(final Destination destination) {
        this.destination = destination;
    }

    public void setDestination(final Int2D cell) {
        setDestination(new Destination(cell, cell));
    }

    @Override
    public String toString() {
        return name == null ? id : name + " (" + id + ")";
    }

    public boolean isAtHomePort() {
        return getCell().equals(getHomePort().getCell());
    }

    public void putTag(
        final String key,
        final Object value
    ) {
        tags.put(key, value);
    }

    public Optional<Object> getTag(final String key) {
        return Optional.ofNullable(tags.get(key));
    }
}

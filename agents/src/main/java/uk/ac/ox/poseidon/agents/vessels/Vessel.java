/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.vessels;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import javax.measure.Quantity;
import javax.measure.quantity.Speed;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

@Getter
@Setter
public class Vessel implements Oriented2D {

    private final String id;
    private final VesselField vesselField;
    private final PortGrid portGrid;
    private final EventManager eventManager;
    @Getter(AccessLevel.NONE)
    private final Deque<Behaviour> behaviourStack = new ArrayDeque<>();
    private Port homePort;
    private Quantity<Speed> cruisingSpeed;
    private double heading;
    private Int2D currentDestination;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    Vessel(
        final String id,
        final PortGrid portGrid,
        final Port homePort,
        final Quantity<Speed> cruisingSpeed,
        final VesselField vesselField,
        final EventManager eventManager
    ) {
        this.id = id;
        this.portGrid = portGrid;
        this.homePort = homePort;
        this.cruisingSpeed = cruisingSpeed;
        this.vesselField = vesselField;
        this.eventManager = eventManager;
    }

    @Override
    public double orientation2D() {
        return heading;
    }

    public void setHeadingTowards(
        final Int2D destinationCell
    ) {
        setHeadingTowards(getVesselField().getGridExtent().toPoint(destinationCell));
    }

    private void setHeadingTowards(
        final Double2D destinationPoint
    ) {
        final Double2D location = getCurrentPoint();
        final double dx = destinationPoint.x - location.x;
        final double dy = destinationPoint.y - location.y;
        setHeading(Math.atan2(dy, dx));
    }

    public Double2D getCurrentPoint() {
        return vesselField.getPoint(this);
    }

    public Int2D getCurrentCell() {
        return vesselField.getCell(this);
    }

    public void setCurrentCell(
        final Int2D cell
    ) {
        vesselField.setCell(this, cell);
    }

    public boolean isAtCurrentDestination() {
        final Int2D currentDestination = getCurrentDestination();
        return currentDestination != null && getCurrentCell().equals(currentDestination);
    }

    public boolean isAtPort() {
        return portGrid.anyPortsAt(getCurrentCell());
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
        Optional
            .ofNullable(currentBehaviour())
            .map(behaviour -> behaviour.nextAction(this, schedule.getDateTime()))
            .ifPresent(action -> {
                action.init();
                schedule.scheduleOnceIn(action.getDuration(), action);
            });
    }

    @Override
    public String toString() {
        return id;
    }
}

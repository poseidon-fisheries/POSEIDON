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

import lombok.*;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Agent;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Destination;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class Vessel extends Agent<Vessel> implements Oriented2D {

    private final @NonNull String id;
    private final @NonNull EventManager eventManager;
    private final @NonNull Account account;
    private final @NonNull VesselField vesselField;
    private final @NonNull PortGrid portGrid;

    @Getter(AccessLevel.NONE)
    private final @NonNull Map<String, Object> tags = new HashMap<>();

    // Modifiable characteristics
    @Setter private String name;
    private Port homePort;
    private Hold hold;
    private Gear gear;
    private Engine engine;

    private boolean activeInRegister;

    // Current state variables
    private double heading;
    private Destination destination;

    @Override
    public boolean isActive() {
        return super.isActive() &&
            activeInRegister &&
            homePort != null &&
            hold != null &&
            gear != null &&
            engine != null;
    }

    public void setHomePort(final Port homePort) {
        mutate(() -> {
            this.homePort = homePort;
            if (this.homePort != null && getCell() == null)
                setCurrentCell(portGrid.getLocation(homePort));
        });
    }

    public void setHold(final Hold hold) {
        mutate(() -> this.hold = hold);
    }

    public void setGear(final Gear gear) {
        mutate(() -> this.gear = gear);
    }

    public void setEngine(final Engine engine) {
        mutate(() -> this.engine = engine);
    }

    public void setActiveInRegister(final boolean activeInRegister) {
        mutate(() -> this.activeInRegister = activeInRegister);
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
        this.heading = Math.atan2(dy, dx);
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
        mutate(() -> tags.put(key, value));
    }

    public Optional<Object> getTag(final String key) {
        return Optional.ofNullable(tags.get(key));
    }
}

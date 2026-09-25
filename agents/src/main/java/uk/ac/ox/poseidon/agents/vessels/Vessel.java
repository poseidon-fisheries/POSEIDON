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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.Agent;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A fishing vessel: an {@link Agent} with a hold, gear, engine, home port, and behaviour tree
 * driving its trips. Active only while {@link #isActive()} holds — the base agent is active, it's
 * registered as active, its gear is active, and it has a home port.
 */
@Getter
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
public class Vessel extends Agent implements Oriented2D {

    /** This vessel's identifying id. */
    private final @NonNull String id;
    /** The field this vessel moves within. */
    private final @NonNull VesselField vesselField;
    /** The grid of ports this vessel can dock at. */
    private final @NonNull PortGrid portGrid;

    // TODO: consider if the vessel really needs a reference to the market grid
    /** The grid of markets this vessel can sell into. */
    private final @NonNull MarketGrid marketGrid;

    /**
     * @param schedule      the simulation's temporal schedule
     * @param eventManager  the event manager this vessel broadcasts on
     * @param nextBehaviour the vessel's initial behaviour
     * @param id            the vessel's identifying id
     * @param vesselField   the field the vessel moves within
     * @param portGrid      the grid of ports the vessel can dock at
     * @param marketGrid    the grid of markets the vessel can sell into
     */
    public Vessel(
        @NonNull final TemporalSchedule schedule,
        @NonNull final EventManager eventManager,
        @NonNull final Behaviour nextBehaviour,
        @NonNull final String id,
        @NonNull final VesselField vesselField,
        @NonNull final PortGrid portGrid,
        @NonNull final MarketGrid marketGrid
    ) {
        super(schedule, eventManager, nextBehaviour);
        this.id = id;
        this.vesselField = vesselField;
        this.portGrid = portGrid;
        this.marketGrid = marketGrid;
    }

    /** @return this vessel's tags, unmodifiable */
    @SuppressWarnings("unused")
    public @NonNull Map<String, Object> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    private final @NonNull Map<String, Object> tags = new HashMap<>();

    // Modifiable characteristics
    /** This vessel's display name. */
    @Setter private String name;
    /** This vessel's running balance. */
    @Setter private Account account;
    /** This vessel's home port. */
    private Port homePort;
    /** This vessel's hold. */
    private Hold hold;
    /** This vessel's gear. */
    private Gear gear;
    /** This vessel's engine. */
    private Engine engine;

    /** Whether this vessel is currently registered as active (see {@link #setRegisteredAsActive}). */
    private boolean registeredAsActive;

    // Current state variables
    /** This vessel's current heading, in radians. */
    private double heading;
    /** The vessel's current trip, or {@code null} if it isn't on one. */
    private Trip currentTrip;

    /** @return whether the base agent is active, {@link #registeredAsActive}, the gear is active, and there's a home port */
    @Override
    public boolean isActive() {
        return super.isActive() &&
            registeredAsActive &&
            gear.isActive() &&
            homePort != null;
    }

    /** Sets the vessel's home port, moving it there if it has no current cell yet. */
    public void setHomePort(final Port homePort) {
        mutate(() -> {
            this.homePort = homePort;
            // TODO: if location were to be stored in port we wouldn't need the port grid here
            if (this.homePort != null && getCell() == null)
                setCurrentCell(portGrid.getLocation(homePort));
        });
    }

    /** Sets the vessel's hold. */
    public void setHold(final @NonNull Hold hold) {
        mutate(() -> this.hold = hold);
    }

    /** Sets the vessel's gear. */
    public void setGear(final @NonNull Gear gear) {
        mutate(() -> this.gear = gear);
    }

    /** Sets the vessel's engine. */
    public void setEngine(final @NonNull Engine engine) {
        mutate(() -> this.engine = engine);
    }

    /** Sets whether the vessel is registered as active. */
    public void setRegisteredAsActive(final boolean registeredAsActive) {
        mutate(() -> this.registeredAsActive = registeredAsActive);
    }

    /** @return {@link #heading} */
    @Override
    public double orientation2D() {
        return heading;
    }

    /** Points the vessel's heading towards {@code destinationCell}'s centre. */
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

    /** @return the vessel's continuous-space position */
    public Double2D getPoint() {
        return vesselField.getPoint(this);
    }

    /** @return the vessel's position, converted to a geographic coordinate */
    public Coordinate getCoordinate() {
        return vesselField.getModelGrid().toCoordinate(getPoint());
    }

    /** @return the grid cell the vessel currently occupies */
    public Int2D getCell() {
        return vesselField.getCell(this);
    }

    /** Moves the vessel to {@code cell}. */
    public void setCurrentCell(
        final Int2D cell
    ) {
        vesselField.setCell(this, cell);
    }

    /** @return {@code true} if the vessel's current cell has a port on it */
    public boolean isAtPort() {
        return portGrid.anyObjectsAt(getCell());
    }

    @Override
    public String toString() {
        return name == null ? id : name + " (" + id + ")";
    }

    /** @return {@code true} if the vessel's current cell is its home port's cell */
    public boolean isAtHomePort() {
        return getCell().equals(getHomePortLocation());
    }

    /** @return the home port's grid cell */
    public Int2D getHomePortLocation() {
        return portGrid.getLocation(homePort);
    }

    /** Sets (or replaces) a tag by name. */
    public void putTag(
        final String tagName,
        final Object tagValue
    ) {
        mutate(() -> tags.put(tagName, tagValue));
    }

    /** @return the named tag's value, if set */
    public Optional<Object> getTag(final String tagName) {
        return Optional.ofNullable(tags.get(tagName));
    }

    /** Starts a new {@link Trip} to {@code destination}. */
    public void startTrip(final Int2D destination) {
        this.currentTrip = new Trip(this, destination);
    }

    /** Ends the current trip, folding its account into the vessel's own. */
    public void endTrip() {
        currentTrip.endTrip();
        this.account.add(currentTrip.getAccount());
        this.currentTrip = null;
    }
}

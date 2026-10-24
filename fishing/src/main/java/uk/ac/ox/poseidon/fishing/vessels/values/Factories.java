/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.vessels.values;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import org.joda.money.Money;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.ports.Port;

/**
 * Factories for values of a vessel: functions given the vessel to read. They
 * read its state (its cell, trips, event managers), test it (whether it is
 * active, where it is) or derive a value from it (a property, a trip cost).
 * {@link #other} reads such a value off another vessel, in a test relating the
 * two.
 */
@SuppressWarnings("overloads")
public final class Factories {

  private Factories() {}

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link CurrentCell}
   * @see CurrentCell
   */
  public static CurrentCellFactory currentCell() {
    return new CurrentCellFactory();
  }

  /**
   * @param pathFinder finds which cells are reachable, and by what path
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
   *         {@link AccessibleWaterCells}
   * @see AccessibleWaterCells
   */
  public static <S extends Scope> AccessibleWaterCellsFactory<S> accessibleWaterCells(
    final Factory<? super S, ? extends GridPathFinder> pathFinder
  ) {
    return new AccessibleWaterCellsFactory<>(pathFinder);
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link HomePort}
   * @see HomePort
   */
  public static HomePortFactory homePort() {
    return new HomePortFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link CurrentTripDestination}
   * @see CurrentTripDestination
   */
  public static CurrentTripDestinationFactory currentTripDestination() {
    return new CurrentTripDestinationFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link CurrentTimeAtSea}
   * @see CurrentTimeAtSea
   */
  public static CurrentTimeAtSeaFactory currentTimeAtSea() {
    return new CurrentTimeAtSeaFactory(currentDateTime());
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link CurrentTripStartDateTime}
   * @see CurrentTripStartDateTime
   */
  public static CurrentTripStartDateTimeFactory currentTripStartDateTime() {
    return new CurrentTripStartDateTimeFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link VesselEventManager}
   * @see VesselEventManager
   */
  public static VesselEventManagerFactory vesselEventManager() {
    return new VesselEventManagerFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link CurrentTripEventManager}
   * @see CurrentTripEventManager
   */
  public static CurrentTripEventManagerFactory currentTripEventManager() {
    return new CurrentTripEventManagerFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link VesselIsActive}
   * @see VesselIsActive
   */
  public static VesselIsActiveFactory vesselIsActive() {
    return new VesselIsActiveFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         a {@link HasStartedFishingTripToday}, one per vessel, registered
   *         on the vessel's event manager as it is built; share one between
   *         the components that read it, so that a component built later
   *         reads the same record
   * @see HasStartedFishingTripToday
   */
  public static HasStartedFishingTripTodayFactory hasStartedFishingTripToday() {
    return new HasStartedFishingTripTodayFactory(currentDateTime());
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory} for
   *         a {@link CurrentPortStayStartDateTime}, one per vessel, registered
   *         on the vessel's event manager as it is built; the current
   *         date-time when it is built is taken as the start of the vessel's
   *         first stay at port, so it must first be built for a vessel at the
   *         vessel's creation, as it is when a component the vessel
   *         specification lists uses it. A component chosen only after a
   *         change of attributes is built later, and would date that first
   *         stay from the change
   * @see CurrentPortStayStartDateTime
   */
  public static CurrentPortStayStartDateTimeFactory currentPortStayStartDateTime() {
    return new CurrentPortStayStartDateTimeFactory(currentDateTime());
  }

  /**
   * Reads a value of a vessel off the other vessel, in a test relating the
   * vessel a call is for to another vessel, such as a friendship condition:
   * {@code condition(other(vesselProperty("homePort")),
   * equal(vesselProperty("homePort")))} holds when the other vessel's home port
   * is the vessel's. A value of a vessel used there without it reads the vessel
   * the call is for.
   *
   * @param value the value to read off the other vessel
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
   *         {@link Other}
   * @see Other
   */
  public static <S extends Scope, R> OtherFactory<S, R> other(
    final ValueFactory<? super S, ? super Vessel, ? extends R> value
  ) {
    return new OtherFactory<>(value);
  }

  /**
   * @param port the port, read for the vessel tested, to test its docking
   *             against
   * @return a {@link VesselScope}-relative factory for a
   *         {@link VesselIsDockedAt}
   * @see VesselIsDockedAt
   */
  public static <S extends Scope> VesselIsDockedAtFactory<S> vesselIsDockedAt(
    final ValueFactory<? super S, ? super Vessel, Port> port
  ) {
    return new VesselIsDockedAtFactory<>(port);
  }

  /**
   * @param hourlyCosts gives the hourly cost of a vessel at sea
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link TimeAtSeaCost}, the cost of a finished trip's time at sea
   * @see TimeAtSeaCostFactory
   */
  public static <S extends Scope> TimeAtSeaCostFactory<S> timeAtSeaCost(
    final ValueFactory<? super S, ? super Vessel, ? extends Money> hourlyCosts
  ) {
    return new TimeAtSeaCostFactory<>(hourlyCosts);
  }

  /**
   * @param hourlyCosts the cost of an hour at sea, the same for every vessel
   * @return the same as {@link #timeAtSeaCost(ValueFactory)}, over a constant
   *         {@code hourlyCosts}
   */
  public static <S extends Scope> TimeAtSeaCostFactory<S> timeAtSeaCost(
    final Factory<? super S, ? extends Money> hourlyCosts
  ) {
    return timeAtSeaCost(constant(hourlyCosts));
  }

  /**
   * @param propertyPath the dotted path of properties to follow from the
   *                     vessel, e.g. {@code "homePort.code"} for the code of
   *                     the vessel's home port
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an
   *         {@link uk.ac.ox.poseidon.core.functions.ObjectProperty} of a vessel
   * @see VesselPropertyFactory
   */
  public static <R> VesselPropertyFactory<R> vesselProperty(
    final String propertyPath
  ) {
    return new VesselPropertyFactory<>(propertyPath);
  }

  /**
   * @param component    the component to read
   * @param propertyPath the dotted path of properties to follow from the
   *                     component, e.g. {@code "availableCapacityInKg"} of a
   *                     hold
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link ComponentProperty} of a vessel
   * @see ComponentPropertyFactory
   */
  public static <R> ComponentPropertyFactory<R> componentProperty(
    final VesselComponent<?> component,
    final String propertyPath
  ) {
    return new ComponentPropertyFactory<>(component, propertyPath);
  }
}

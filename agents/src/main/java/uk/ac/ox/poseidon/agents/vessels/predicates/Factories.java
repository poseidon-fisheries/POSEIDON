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

package uk.ac.ox.poseidon.agents.vessels.predicates;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for {@code Predicate<Vessel>}s testing a vessel's activity, trips,
 * home port or location.
 */
public class Factories {

  private Factories() {}

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link VesselIsActive}
   * @see VesselIsActive
   */
  public static VesselIsActiveFactory vesselIsActive() {
    return new VesselIsActiveFactory();
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
   *         {@link VesselHasStartedTripToday}
   * @see VesselHasStartedTripToday
   */
  public static VesselHasStartedTripTodayFactory vesselHasStartedTripToday() {
    return new VesselHasStartedTripTodayFactory();
  }

  /**
   * @return a {@link VesselScope}-relative factory for a
   *         {@link VesselHasSameHomePort} matching the scoped vessel's home
   *         port
   * @see VesselHasSameHomePort
   */
  public static VesselHasSameHomePortFactory vesselHasSameHomePort() {
    return new VesselHasSameHomePortFactory();
  }

  /**
   * @param cell the cell, read for the vessel tested, to test its
   *                     location against
   * @return a {@link VesselScope}-relative factory for a {@link VesselIsAt}
   * @see VesselIsAt
   */
  public static <S extends Scope> VesselIsAtFactory<S> vesselIsAt(
    final ValueFactory<? super S, ? super Vessel, Int2D> cell
  ) {
    return new VesselIsAtFactory<>(cell);
  }
}

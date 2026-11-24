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

package uk.ac.ox.poseidon.fishing.travel;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.time.Duration;
import org.joda.money.Money;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.engines.Engine;
import uk.ac.ox.poseidon.geography.paths.PathLengthCalculator;

/**
 * Factories for how long vessels would travel, and what the time costs.
 */
@SuppressWarnings("overloads")
public final class Factories {

  private Factories() {}

  /**
   * @param engine               the engine whose speed gives the travel time
   * @param pathLengthCalculator measures the path to the cell
   * @return a {@link MappingFactory} for a {@link TravelTimeToCell}
   * @see TravelTimeToCell
   */
  public static <S extends Scope> TravelTimeToCellFactory<S> travelTimeToCell(
    final VesselComponent<? extends Engine> engine,
    final Factory<
      ? super S,
      ? extends PathLengthCalculator
    > pathLengthCalculator
  ) {
    return new TravelTimeToCellFactory<>(engine, pathLengthCalculator);
  }

  /**
   * @param engine               the engine whose speed gives the travel time
   * @param pathLengthCalculator measures the path home
   * @return a {@link MappingFactory} for a {@link TravelTimeHomeFromCell}
   * @see TravelTimeHomeFromCell
   */
  public static <S extends Scope> TravelTimeHomeFromCellFactory<S> travelTimeHomeFromCell(
    final VesselComponent<? extends Engine> engine,
    final Factory<
      ? super S,
      ? extends PathLengthCalculator
    > pathLengthCalculator
  ) {
    return new TravelTimeHomeFromCellFactory<>(engine, pathLengthCalculator);
  }

  /**
   * @param travelTime how long a vessel would travel to take an option
   * @param hourlyCost gives the vessel's cost per hour at sea
   * @return a {@link uk.ac.ox.poseidon.fishing.choices.Cost} factory for a
   *         {@link TimeCost}
   * @see TimeCost
   */
  public static <S extends Scope, T> TimeCostFactory<S, T> timeCost(
    final MappingFactory<
      ? super S,
      ? super Vessel,
      ? super T,
      ? extends Duration
    > travelTime,
    final ValueFactory<? super S, ? super Vessel, ? extends Money> hourlyCost
  ) {
    return new TimeCostFactory<>(travelTime, hourlyCost);
  }

  /**
   * @param travelTime how long a vessel would travel to take an option
   * @param hourlyCost the cost of an hour, the same for every vessel
   * @return the same as {@link #timeCost(MappingFactory, ValueFactory)}, over a
   *         constant {@code hourlyCost}
   */
  public static <S extends Scope, T> TimeCostFactory<S, T> timeCost(
    final MappingFactory<
      ? super S,
      ? super Vessel,
      ? super T,
      ? extends Duration
    > travelTime,
    final Factory<? super S, ? extends Money> hourlyCost
  ) {
    return timeCost(travelTime, constant(hourlyCost));
  }
}

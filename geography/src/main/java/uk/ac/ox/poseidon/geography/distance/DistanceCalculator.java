/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography.distance;

import static com.google.common.base.Preconditions.checkArgument;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOMETRE_PER_HOUR;
import static tech.units.indriya.unit.Units.METRE;

import java.time.Duration;
import java.util.List;
import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * Measures distance and derived travel duration between two coordinates or
 * between two cells, from the centre of one cell to the centre of the other.
 * Built via {@link uk.ac.ox.poseidon.geography.distance.Factories}.
 */
public interface DistanceCalculator {
  /**
   * @param a one coordinate
   * @param b the other coordinate
   * @return the distance between {@code a} and {@code b}, in kilometres
   */
  double distanceInKm(Coordinate a, Coordinate b);

  /**
   * @param start one cell
   * @param end   the other cell
   * @return the distance between the centres of {@code start} and
   *         {@code end}, in kilometres
   */
  double distanceInKm(Cell start, Cell end);

  /**
   * @param path the cells to travel through in order
   * @return the length of {@code path}, summing the distance of each leg, in
   *         kilometres; zero for a path of a single cell
   */
  default double distanceInKm(final List<Cell> path) {
    double distanceInKm = 0.0;
    for (int i = 0; i < path.size() - 1; i++) {
      distanceInKm += distanceInKm(path.get(i), path.get(i + 1));
    }
    return distanceInKm;
  }

  /**
   * @return {@link #distanceInKm(Cell, Cell)} as a length
   *         {@link Quantity}
   */
  default Quantity<Length> distance(final Cell start, final Cell end) {
    return getQuantity(distanceInKm(start, end), KILO(METRE));
  }

  /**
   * @return {@link #distanceInKm(Coordinate, Coordinate)} as a length
   *         {@link Quantity}
   */
  default Quantity<Length> distance(final Coordinate a, final Coordinate b) {
    return getQuantity(distanceInKm(a, b), KILO(METRE));
  }

  /**
   * @return the time to travel from {@code start} to {@code end} at
   *         {@code cruisingSpeed}
   */
  default Duration travelDuration(
    final Cell start,
    final Cell end,
    final Quantity<Speed> cruisingSpeed
  ) {
    return travelDuration(List.of(start, end), cruisingSpeed);
  }

  /**
   * @return the time to travel from {@code start} to {@code end} at
   *         {@code cruisingSpeedInKph}
   */
  default Duration travelDuration(
    final Cell start,
    final Cell end,
    final double cruisingSpeedInKph
  ) {
    return travelDuration(List.of(start, end), cruisingSpeedInKph);
  }

  /**
   * @param path               the cells to travel through in order, at least
   *                           one
   * @param cruisingSpeedInKph the constant travel speed, in km/h
   * @return the time to travel the full path at {@code cruisingSpeedInKph},
   *         summing the distance of each leg; zero for a path of a single
   *         cell
   * @throws IllegalArgumentException if {@code path} is empty
   */
  default Duration travelDuration(
    final List<Cell> path,
    final double cruisingSpeedInKph
  ) {
    checkArgument(
      !path.isEmpty(),
      "Path must contain at least one cell but was empty"
    );
    return travelDuration(distanceInKm(path), cruisingSpeedInKph);
  }

  /**
   * Takes the distance first and the speed last, like the other
   * {@code travelDuration} methods: both are {@code double}s, so a swap would
   * not be caught by the compiler.
   *
   * @param totalDistanceInKm  the total distance to travel, in kilometres
   * @param cruisingSpeedInKph the constant travel speed, in km/h
   * @return the time to travel {@code totalDistanceInKm} at
   *         {@code cruisingSpeedInKph}
   */
  static Duration travelDuration(
    final double totalDistanceInKm,
    final double cruisingSpeedInKph
  ) {
    final long SECONDS_PER_HOUR = 3600;
    return Duration.ofSeconds(
      (long) ((totalDistanceInKm / cruisingSpeedInKph) * SECONDS_PER_HOUR)
    );
  }

  /** @return the time to travel the full path at {@code cruisingSpeed} */
  default Duration travelDuration(
    final List<Cell> path,
    final Quantity<Speed> cruisingSpeed
  ) {
    return travelDuration(
      path,
      cruisingSpeed.to(KILOMETRE_PER_HOUR).getValue().doubleValue()
    );
  }
}

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

package uk.ac.ox.poseidon.fishing.vessels.holds;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.fishing.catches.CatchCategoriser;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;

/** Factories for a vessel's {@link Hold}, with unlimited or fixed capacity. */
public class Factories {

  private Factories() {}

  /**
   * @param catchCategoriser sorts uncategorised content added to the hold
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         for an {@link InfiniteBiomassHold}
   * @see InfiniteBiomassHold
   */
  public static InfiniteBiomassHoldFactory infiniteBiomassHold(
    final Factory<
      ? super VesselComponentScope,
      ? extends CatchCategoriser
    > catchCategoriser
  ) {
    return new InfiniteBiomassHoldFactory(catchCategoriser);
  }

  /**
   * @param capacity         the hold's total capacity
   * @param tolerance        how far over capacity the hold may go before
   *                         rejecting content
   * @param catchCategoriser sorts uncategorised content added to the hold
   * @return a {@link uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory}
   *         for a {@link StandardBiomassHold}
   * @see StandardBiomassHold
   */
  public static StandardBiomassHoldFactory standardBiomassHold(
    final Factory<
      ? super VesselComponentScope,
      ? extends Quantity<Mass>
    > capacity,
    final Factory<
      ? super VesselComponentScope,
      ? extends Quantity<Mass>
    > tolerance,
    final Factory<
      ? super VesselComponentScope,
      ? extends CatchCategoriser
    > catchCategoriser
  ) {
    return new StandardBiomassHoldFactory(
      capacity,
      tolerance,
      catchCategoriser
    );
  }
}

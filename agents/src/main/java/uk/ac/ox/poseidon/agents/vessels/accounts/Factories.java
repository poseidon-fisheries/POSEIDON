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

package uk.ac.ox.poseidon.agents.vessels.accounts;

import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.Fleet;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * Factories for {@link sim.engine.Steppable}s that draw on vessels'
 * {@link Account}s.
 */
public class Factories {

  private Factories() {}

  /**
   * @param fleet         the fleet whose active vessels get charged
   * @param cost computes the amount to subtract from each vessel's
   *                      account
   * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
   *         {@link FixedCostCollector}
   * @see FixedCostCollector
   */
  public static <S extends Scope> FixedCostCollectorFactory<S> fixedCostCollector(
    final Factory<? super S, ? extends Fleet> fleet,
    final ValueFactory<? super S, ? super Vessel, ? extends Money> cost
  ) {
    return new FixedCostCollectorFactory<>(fleet, cost);
  }
}

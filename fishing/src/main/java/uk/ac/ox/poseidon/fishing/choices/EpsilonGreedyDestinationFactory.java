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

package uk.ac.ox.poseidon.fishing.choices;

import ec.util.MersenneTwisterFast;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScopeFactory;
import uk.ac.ox.poseidon.geography.grids.Cell;

/**
 * A {@link VesselComponentScopeFactory} counterpart of {@link EpsilonGreedyChooser},
 * built via {@link Factories#epsilonGreedyDestination}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class EpsilonGreedyDestinationFactory
  extends VesselComponentScopeFactory<Function<Vessel, Optional<Cell>>>
  implements ValueFactory<VesselComponentScope, Vessel, Optional<Cell>>
{

  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    Double
  > epsilon;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    ? extends Cell
  > explorer;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    ? extends Cell
  > exploiter;
  private final Factory<
    ? super VesselComponentScope,
    ? extends MersenneTwisterFast
  > randomNumberGenerator;

  /**
   * @return an {@link EpsilonGreedyChooser} of destinations over the resolved
   *         explorer and exploiter
   */
  @Override
  protected Function<Vessel, Optional<Cell>> newInstance(
    final VesselComponentScope scope
  ) {
    return new EpsilonGreedyChooser<>(
      epsilon.get(scope),
      explorer.get(scope),
      exploiter.get(scope),
      randomNumberGenerator.get(scope)
    );
  }
}

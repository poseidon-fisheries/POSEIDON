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

package uk.ac.ox.poseidon.agents.vessels;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.tasks.TaskFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link TaskFactory} for a {@link TripBlock}. Built via
 * {@link Factories#trip}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class TripBlockFactory extends TaskFactory<TripBlock> {

  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    Optional<Int2D>
  > initialDestination;

  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Singular
  private final ImmutableList<
    ? extends Factory<? super VesselComponentScope, ? extends Task<Vessel>>
  > children;

  @Override
  protected TripBlock newInstance(final VesselComponentScope scope) {
    return new TripBlock(
      initialDestination.get(scope),
      currentDateTime.get(scope),
      children
        .stream()
        .<Task<Vessel>>map(child -> child.get(scope))
        .collect(toImmutableList())
    );
  }
}

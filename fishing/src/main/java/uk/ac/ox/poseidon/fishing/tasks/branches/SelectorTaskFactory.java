/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.tasks.branches;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;

/**
 * A {@link BranchTaskFactory} for a gdx-ai {@link Selector}: runs its children
 * in order, succeeding (and stopping) as soon as one succeeds, failing once all
 * of them have. Built via {@link Factories#selectorTask}.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class SelectorTaskFactory extends BranchTaskFactory<Selector<Vessel>> {

  /**
   * @param children factories for the selector's child tasks, resolved and
   *                 attached in order
   */
  public SelectorTaskFactory(
    final List<
      ? extends Factory<? super VesselComponentScope, ? extends Task<Vessel>>
    > children
  ) {
    super(ImmutableList.copyOf(children));
  }

  @Override
  protected Selector<Vessel> newTask() {
    return new Selector<>();
  }
}

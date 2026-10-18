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

package uk.ac.ox.poseidon.fishing.tasks.general;

import static com.badlogic.gdx.ai.btree.Task.Status.FAILED;
import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.tasks.VesselTask;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * A leaf {@link VesselTask} that succeeds or fails in one step, depending on
 * whether {@code predicate} holds for the vessel.
 */
@RequiredArgsConstructor
public class VesselPredicateTask extends VesselTask {

  private final Function<? super Vessel, Boolean> predicate;

  /**
   * @return {@code SUCCEEDED} if {@code predicate} holds for the vessel,
   *         {@code FAILED} otherwise
   */
  @Override
  public Status execute() {
    return predicate.apply(getVessel()) ? SUCCEEDED : FAILED;
  }
}

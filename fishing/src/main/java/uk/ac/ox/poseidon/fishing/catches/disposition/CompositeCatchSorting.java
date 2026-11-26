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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import java.util.Collection;
import uk.ac.ox.poseidon.biology.buckets.Bucket;

/**
 * Chains sorting steps, feeding each step only what its predecessor kept.
 * Rejects a step that keeps more of any content than it received, naming it.
 * A step returning its own input needs no subtraction check.
 */
public final class CompositeCatchSorting implements CatchSorting {

  private final CatchSorting[] steps;

  CompositeCatchSorting(final Collection<CatchSorting> steps) {
    this.steps = steps.toArray(CatchSorting[]::new);
  }

  @Override
  public Bucket keep(final Bucket catches, final double freeCapacityInKg) {
    Bucket keptCatch = catches;
    for (final CatchSorting step : steps) {
      final Bucket catchesForStep = keptCatch;
      keptCatch = step.keep(catchesForStep, freeCapacityInKg);
      if (keptCatch != catchesForStep) {
        try {
          catchesForStep.subtract(keptCatch);
        } catch (final IllegalArgumentException exception) {
          throw new IllegalArgumentException(
            "Catch sorting step %s kept content it was not given".formatted(
              step
            ),
            exception
          );
        }
      }
    }
    return keptCatch;
  }
}

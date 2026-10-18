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

package uk.ac.ox.poseidon.fishing.vessels;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;

/**
 * The values of a row of a choice, waiting for what they give: built via
 * {@link Factories#when}, and made a row by {@link #then}.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ChoiceCondition {

  private final ImmutableList<Object> values;

  /**
   * @param factory builds the component for a vessel with these values
   * @return the row giving what {@code factory} builds
   */
  public <T> ChoiceRow<T> then(
    final Factory<? super VesselComponentScope, ? extends T> factory
  ) {
    return new ChoiceRow<>(values, new BuildOutcome<>(factory));
  }

  /**
   * @param noComponent {@link Factories#none}
   * @return the row giving a vessel with these values no component
   */
  public <T> ChoiceRow<T> then(final NoComponent<T> noComponent) {
    return new ChoiceRow<>(values, noComponent);
  }
}

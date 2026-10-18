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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import uk.ac.ox.poseidon.core.ScenarioNode;

/**
 * A row of a {@link ChosenComponent}: a combination of exact values of the
 * attributes the choice reads, in their order, and what it gives. Built via
 * {@link Factories#when}.
 *
 * @param <T> the type of the component
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public final class ChoiceRow<T> implements ScenarioNode, ChoiceEntry<T> {

  /** The values of the attributes the choice reads, in their order. */
  @NonNull
  @Singular
  private final ImmutableList<Object> values;

  /** What a vessel with these values is given. */
  @NonNull
  private final ChoiceOutcome<T> outcome;
}

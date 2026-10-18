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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.Factory;

/**
 * The outcome of a choice that builds the component with a factory, under a
 * fresh store each time the vessel is given it. Built via
 * {@link ChoiceCondition#then(Factory)} or {@link Factories#otherwise(Factory)}.
 *
 * @param <T> the type of the component
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public final class BuildOutcome<T> implements ChoiceOutcome<T> {

  /** Builds the component; its own level decides where its product lives. */
  @NonNull
  private final Factory<? super VesselComponentScope, ? extends T> factory;
}

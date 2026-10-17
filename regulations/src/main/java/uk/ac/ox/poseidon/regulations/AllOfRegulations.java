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

package uk.ac.ox.poseidon.regulations;

import java.util.Collection;
import lombok.NonNull;

/**
 * The conjunction of several rules: permits an action only when every one of
 * them permits it (every action when there are none), asking them in order and
 * stopping at the first that forbids it. Built via
 * {@link Factories#allOf(uk.ac.ox.poseidon.core.Factory...)}.
 *
 * @param <A> the type of action judged
 */
public class AllOfRegulations<A extends Action<?>> implements Regulations<A> {

  // Using an array for performance reasons
  @NonNull
  private final Regulations<? super A>[] regulations;

  /** @param regulations the rules, all of which must permit an action */
  @SuppressWarnings("unchecked")
  public AllOfRegulations(
    @NonNull final Collection<? extends Regulations<? super A>> regulations
  ) {
    this.regulations = regulations.toArray(Regulations[]::new);
  }

  @Override
  public boolean isPermitted(final A action) {
    for (final Regulations<? super A> regulation : regulations) {
      if (!regulation.isPermitted(action)) {
        return false;
      }
    }
    return true;
  }
}

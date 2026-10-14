/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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
import java.util.List;
import java.util.function.Predicate;
import lombok.NonNull;
import uk.ac.ox.poseidon.core.predicates.logical.AllOf;

/**
 * The conjunction of several rules: permits an action only when every one of them permits it
 * (every action when there are none), asking them in order and stopping at the first that
 * forbids it. An {@link AllOf} that is itself a rule. Built via
 * {@link Factories#allOf(uk.ac.ox.poseidon.core.Factory...)}.
 *
 * @param <A> the type of action judged
 */
public class AllOfRegulations<A extends Action<?>>
  extends AllOf<A>
  implements Regulations<A>
{

  /** @param regulations the rules, all of which must permit an action */
  public AllOfRegulations(
    @NonNull final Collection<? extends Regulations<? super A>> regulations
  ) {
    super(List.<Predicate<? super A>>copyOf(regulations));
  }

  @Override
  public boolean isPermitted(final A action) {
    return test(action);
  }
}

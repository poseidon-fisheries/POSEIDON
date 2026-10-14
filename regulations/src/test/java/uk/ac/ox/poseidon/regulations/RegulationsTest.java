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

package uk.ac.ox.poseidon.regulations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class RegulationsTest {

  @SuppressWarnings("unchecked")
  private final Action<Object> action = mock(Action.class);

  @Test
  void asAPredicateHoldsForPermittedActions() {
    final Predicate<Action<Object>> permitting = (Regulations<
      Action<Object>
    >) a -> true;
    final Predicate<Action<Object>> forbidding = (Regulations<
      Action<Object>
    >) a -> false;

    assertThat(permitting.test(action)).isTrue();
    assertThat(forbidding.test(action)).isFalse();
  }

  @Test
  void isForbiddenIsTheOppositeOfIsPermitted() {
    final Regulations<Action<Object>> permitting = a -> true;
    final Regulations<Action<Object>> forbidding = a -> false;

    assertThat(permitting.isForbidden(action)).isFalse();
    assertThat(forbidding.isForbidden(action)).isTrue();
  }
}

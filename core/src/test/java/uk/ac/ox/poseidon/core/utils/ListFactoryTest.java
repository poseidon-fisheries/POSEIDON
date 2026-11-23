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

package uk.ac.ox.poseidon.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.utils.Factories.listOf;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;

class ListFactoryTest {

  @Test
  void resolvesEachComponentInOrder() {
    final ListFactory<Scope, Object> factory = listOf(
      List.of(object("a"), object(1), object(2.5))
    );

    assertThat(factory.get(GLOBAL_SCOPE)).containsExactly("a", 1, 2.5);
  }

  @Test
  void anEmptyListOfFactoriesResolvesToAnEmptyList() {
    final ListFactory<Scope, Object> factory = listOf(List.of());

    assertThat(factory.get(GLOBAL_SCOPE)).isEmpty();
  }
}

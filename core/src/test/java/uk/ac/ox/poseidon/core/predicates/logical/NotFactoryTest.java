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

package uk.ac.ox.poseidon.core.predicates.logical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.predicates.logical.Factories.not;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.alwaysTrue;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;

class NotFactoryTest {

  @Test
  void negatingAMappingGivesAMapping() {
    final BiFunction<Object, Object, Boolean> mapping = not(alwaysTrue()).get(
      Scope.GLOBAL_SCOPE
    );
    assertThat(mapping.apply(null, "t")).isFalse();
  }

  @Test
  void negatingAValueGivesAValue() {
    final Function<Object, Boolean> value = not(constant(true)).get(
      Scope.GLOBAL_SCOPE
    );
    assertThat(value.apply(null)).isFalse();
  }

  @Test
  void aNullComponentFailsWhenBuilt() {
    final MappingFactory<Scope, Object, Object, Boolean> nullFactory = scope ->
      null;
    assertThatThrownBy(() ->
      not(nullFactory).get(Scope.GLOBAL_SCOPE)
    ).isInstanceOf(NullPointerException.class);
  }
}

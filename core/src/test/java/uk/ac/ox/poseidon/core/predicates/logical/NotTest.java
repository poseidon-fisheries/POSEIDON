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

import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.functions.MappingAsValue;
import uk.ac.ox.poseidon.core.functions.ValueAsMapping;

class NotTest {

  @Test
  void mappingNegatesItsComponentUnderTheSameContext() {
    final Not<String, String> notSame = new Not<>(String::equals);
    assertThat(notSame.apply("a", "a")).isFalse();
    assertThat(notSame.apply("a", "b")).isTrue();
  }

  @Test
  void valueNegatesItsComponentUnderTheSameContext() {
    final Function<String, Boolean> isA = "a"::equals;
    final Function<String, Boolean> notA = new MappingAsValue<>(
      new Not<String, Void>(new ValueAsMapping<>(isA))
    );
    assertThat(notA.apply("a")).isFalse();
    assertThat(notA.apply("b")).isTrue();
  }
}

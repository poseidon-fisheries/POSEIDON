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

package uk.ac.ox.poseidon.core.functions;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toFunction;
import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toPredicate;
import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class VoidContextConversionsTest {

  private final List<Object> contexts = new ArrayList<>();

  @Test
  void aSupplierAsksTheValueWithNoContext() {
    final Function<Object, String> value = context -> {
      contexts.add(context);
      return "value";
    };
    assertThat(toSupplier(value).get()).isEqualTo("value");
    assertThat(contexts).containsExactly((Object) null);
  }

  @Test
  void aPredicateAsksTheMappingWithNoContext() {
    final BiFunction<Object, Integer, Boolean> positive = (context, i) -> {
      contexts.add(context);
      return i > 0;
    };
    assertThat(toPredicate(positive).test(1)).isTrue();
    assertThat(toPredicate(positive).test(-1)).isFalse();
    assertThat(contexts).containsExactly(null, null);
  }

  @Test
  void aFunctionAsksTheMappingWithNoContext() {
    final BiFunction<Object, Integer, Integer> doubled = (context, i) -> {
      contexts.add(context);
      return 2 * i;
    };
    assertThat(toFunction(doubled).apply(3)).isEqualTo(6);
    assertThat(contexts).containsExactly((Object) null);
  }
}

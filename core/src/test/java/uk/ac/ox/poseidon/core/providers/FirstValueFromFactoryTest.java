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

package uk.ac.ox.poseidon.core.providers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.providers.constant.Constant;

class FirstValueFromFactoryTest {

  private final AtomicInteger counter = new AtomicInteger(0);

  private final FirstValueFromFactory<Scope, Integer> factory =
    new FirstValueFromFactory<>(new CountingFactory(counter));

  @Test
  void returnedValueIsStable() {
    final Function<Object, Integer> value = factory.get(Scope.GLOBAL_SCOPE);
    assertThat(value.apply(null)).isEqualTo(1);
    assertThat(value.apply(null)).isEqualTo(1);
    assertThat(value.apply(null)).isEqualTo(1);
  }

  @Test
  void delegateCalledOnlyOnce() {
    factory.get(Scope.GLOBAL_SCOPE);
    assertThat(counter).hasValue(1);
  }

  @Test
  void returnsConstant() {
    assertThat(factory.get(Scope.GLOBAL_SCOPE))
      .isInstanceOf(Constant.class)
      .extracting(value -> value.apply(null))
      .isEqualTo(1);
  }

  /** A value counting how many times it is asked. */
  private static final class CountingFactory
    extends GlobalScopeFactory<Function<Object, Integer>>
    implements ValueFactory<Scope, Object, Integer>
  {

    private final transient AtomicInteger counter;

    CountingFactory(final AtomicInteger counter) {
      this.counter = counter;
    }

    @Override
    protected Function<Object, Integer> newInstance(final Scope scope) {
      return context -> counter.incrementAndGet();
    }
  }
}

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

package uk.ac.ox.poseidon.core.providers.constant;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.time.Factories.hours;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/**
 * A fixed value is a constant where a value is expected and always the same
 * where a mapping is: both ignore what they are asked under.
 */
class FactoriesTest {

  @Test
  void aConstantIsTheSameWhateverTheContext() {
    final Function<Object, String> value = constant("x").get(GLOBAL_SCOPE);
    assertThat(value.apply("a")).isEqualTo("x");
    assertThat(value.apply(null)).isEqualTo("x");
  }

  @Test
  void alwaysIsTheSameWhateverTheContextAndWhateverItMaps() {
    final BiFunction<Object, Object, String> mapping = always("x").get(
      GLOBAL_SCOPE
    );
    assertThat(mapping.apply("a", 1)).isEqualTo("x");
    assertThat(mapping.apply(null, null)).isEqualTo("x");
  }

  @Test
  void alwaysResolvesAFactoryRatherThanWrappingIt() {
    final BiFunction<Object, Object, Duration> mapping = always(hours(1)).get(
      GLOBAL_SCOPE
    );
    assertThat(mapping.apply(null, "route")).isEqualTo(Duration.ofHours(1));
  }
}

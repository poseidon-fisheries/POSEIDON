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

package uk.ac.ox.poseidon.core.aggregators;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.aggregators.Factories.maxAggregator;
import static uk.ac.ox.poseidon.core.aggregators.Factories.meanAggregator;
import static uk.ac.ox.poseidon.core.aggregators.Factories.medianAggregator;
import static uk.ac.ox.poseidon.core.aggregators.Factories.minAggregator;
import static uk.ac.ox.poseidon.core.aggregators.Factories.sumAggregator;

import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

class FactoriesTest {

  private final List<Double> numbers = List.of(1.0, 2.0, 3.0, 4.0, 5.0);

  @Test
  void meanAggregatorResolvesToTheMean() {
    assertThat(meanAggregator().get(GLOBAL_SCOPE).apply(numbers)).hasValue(3.0);
  }

  @Test
  void minAggregatorResolvesToTheMinimum() {
    assertThat(minAggregator().get(GLOBAL_SCOPE).apply(numbers)).hasValue(1.0);
  }

  @Test
  void maxAggregatorResolvesToTheMaximum() {
    assertThat(maxAggregator().get(GLOBAL_SCOPE).apply(numbers)).hasValue(5.0);
  }

  @Test
  void medianAggregatorResolvesToTheMedian() {
    assertThat(medianAggregator().get(GLOBAL_SCOPE).apply(numbers)).hasValue(
      3.0
    );
  }

  @Test
  void sumAggregatorResolvesToTheSum() {
    assertThat(sumAggregator().get(GLOBAL_SCOPE).apply(numbers)).isEqualTo(
      OptionalDouble.of(15.0)
    );
  }
}

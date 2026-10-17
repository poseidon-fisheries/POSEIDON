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
import static uk.ac.ox.poseidon.core.functions.Factories.intervalLabel;
import static uk.ac.ox.poseidon.core.functions.IntervalLabelFactory.interval;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.IntervalLabelFactory;

/**
 * The label of the interval a number falls in, the number being read from the
 * thing mapped or, for a value, under the context.
 */
class IntervalLabelFactoryTest {

  /** The thing mapped, a number, as the number to place. */
  private final MappingFactory<Scope, Object, Double, Double> number = scope ->
    (context, n) ->
      n;

  /** The length of the context, a string, as the number to place. */
  private final ValueFactory<Scope, String, Double> contextLength = scope ->
    context ->
      (double) context.length();

  @Test
  void labelsTheNumberReadFromTheThingMapped() {
    final BiFunction<Object, Double, String> label = intervalLabel(
      number,
      interval(0.0, 6.0, "VL0006"),
      interval(6.0, 12.0, "VL0612"),
      interval(40.0, null, "VL40XX")
    ).get(GLOBAL_SCOPE);

    assertThat(label.apply(null, 5.999)).isEqualTo("VL0006");
    assertThat(label.apply(null, 6.0)).isEqualTo("VL0612");
    assertThat(label.apply(null, 40.0)).isEqualTo("VL40XX");
  }

  @Test
  void labelsAValue() {
    final Function<String, String> label = intervalLabel(
      contextLength,
      interval(0.0, 3.0, "short"),
      interval(3.0, null, "long")
    ).get(GLOBAL_SCOPE);

    assertThat(label.apply("ab")).isEqualTo("short");
    assertThat(label.apply("abc")).isEqualTo("long");
  }

  @Test
  void createsIntervalsViaStaticHelper() {
    assertThat(interval(6.0, 12.0, "VL0612")).isEqualTo(
      new IntervalLabelFactory.Interval(6.0, 12.0, "VL0612")
    );
  }
}

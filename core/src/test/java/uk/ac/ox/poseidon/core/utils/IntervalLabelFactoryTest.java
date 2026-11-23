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

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.functions.Factories.intervalLabel;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.functions.IntervalLabelFactory;

/**
 * The label of the interval a number falls in, the number being read from the
 * thing mapped or, for a value, under the context. Each interval runs from its
 * lower bound up to the next one.
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

  private BiFunction<Object, Double, String> threeIntervals() {
    return intervalLabel(
      number,
      entry("VL0006", 0.0),
      entry("VL0612", 6.0),
      entry("VL12XX", 12.0)
    ).get(GLOBAL_SCOPE);
  }

  @Test
  void aNumberAtABoundTakesThatIntervalsLabel() {
    final var label = threeIntervals();

    assertThat(label.apply(null, 0.0)).isEqualTo("VL0006");
    assertThat(label.apply(null, 6.0)).isEqualTo("VL0612");
    assertThat(label.apply(null, 12.0)).isEqualTo("VL12XX");
  }

  @Test
  void aNumberJustBelowABoundTakesThePreviousIntervalsLabel() {
    final var label = threeIntervals();

    assertThat(label.apply(null, Math.nextDown(6.0))).isEqualTo("VL0006");
    assertThat(label.apply(null, Math.nextDown(12.0))).isEqualTo("VL0612");
  }

  @Test
  void theLastIntervalIsOpenAbove() {
    assertThat(threeIntervals().apply(null, 1e9)).isEqualTo("VL12XX");
    assertThat(
      threeIntervals().apply(null, Double.POSITIVE_INFINITY)
    ).isEqualTo("VL12XX");
  }

  @Test
  void aNumberBelowTheFirstBoundHasNoLabel() {
    final var label = threeIntervals();

    assertThat(label.apply(null, Math.nextDown(0.0))).isNull();
    assertThat(label.apply(null, -1.0)).isNull();
  }

  @Test
  void aNegativeZeroIsAtABoundOfZero() {
    assertThat(threeIntervals().apply(null, -0.0)).isEqualTo("VL0006");
  }

  @Test
  void aNumberThatIsNotANumberHasNoLabel() {
    assertThat(threeIntervals().apply(null, Double.NaN)).isNull();
  }

  @Test
  void aFirstBoundOfMinusInfinityLabelsEveryNumberBelow() {
    final BiFunction<Object, Double, String> label = intervalLabel(
      number,
      entry("low", Double.NEGATIVE_INFINITY),
      entry("high", 0.0)
    ).get(GLOBAL_SCOPE);

    assertThat(label.apply(null, -1e300)).isEqualTo("low");
    assertThat(label.apply(null, Double.NEGATIVE_INFINITY)).isEqualTo("low");
    assertThat(label.apply(null, 0.0)).isEqualTo("high");
  }

  @Test
  void theOrderTheIntervalsAreWrittenInDoesNotMatter() {
    final BiFunction<Object, Double, String> label = intervalLabel(
      number,
      entry("VL12XX", 12.0),
      entry("VL0006", 0.0),
      entry("VL0612", 6.0)
    ).get(GLOBAL_SCOPE);

    assertThat(label.apply(null, 7.0)).isEqualTo("VL0612");
    assertThat(label.apply(null, 13.0)).isEqualTo("VL12XX");
    assertThat(label.apply(null, 1.0)).isEqualTo("VL0006");
  }

  @Test
  void labelsAValue() {
    final Function<String, String> label = intervalLabel(
      contextLength,
      entry("short", 0.0),
      entry("long", 3.0)
    ).get(GLOBAL_SCOPE);

    assertThat(label.apply("ab")).isEqualTo("short");
    assertThat(label.apply("abc")).isEqualTo("long");
  }

  @Test
  void noIntervalsIsRejected() {
    assertThatThrownBy(() ->
      IntervalLabelFactory.<Scope, Object, Double>builder()
        .number(number)
        .build()
        .get(GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aNaNBoundIsRejected() {
    assertThatThrownBy(() ->
      intervalLabel(number, entry("x", Double.NaN)).get(GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aRepeatedBoundIsRejected() {
    assertThatThrownBy(() ->
      intervalLabel(number, entry("x", 1.0), entry("y", 1.0)).get(GLOBAL_SCOPE)
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aRepeatedLabelIsRejected() {
    assertThatThrownBy(() ->
      intervalLabel(number, entry("a", 0.0), entry("a", 1.0))
    ).isInstanceOf(IllegalArgumentException.class);
  }
}

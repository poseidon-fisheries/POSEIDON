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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.functions.IntervalLabel;

class IntervalLabelTest {

  /** The thing mapped, a number, as the number to place. */
  private static final BiFunction<Object, Double, Double> NUMBER = (
    context,
    number
  ) -> number;

  @Test
  void mapsUsingInclusiveLowerAndExclusiveUpperBounds() {
    final IntervalLabel<Object, Double, String> label = new IntervalLabel<>(
      NUMBER,
      List.of(
        new IntervalLabel.Interval<>(0.0, 12.0, "VL0612"),
        new IntervalLabel.Interval<>(12.0, 18.0, "VL1218")
      )
    );

    assertThat(label.apply(null, 0.0)).isEqualTo("VL0612");
    assertThat(label.apply(null, 11.999)).isEqualTo("VL0612");
    assertThat(label.apply(null, 12.0)).isEqualTo("VL1218");
    assertThat(label.apply(null, 17.999)).isEqualTo("VL1218");
    assertThat(label.apply(null, -0.001)).isNull();
    assertThat(label.apply(null, 18.0)).isNull();
  }

  @Test
  void supportsIntervalsWithNoUpperBound() {
    final IntervalLabel<Object, Double, String> label = new IntervalLabel<>(
      NUMBER,
      List.of(
        new IntervalLabel.Interval<>(24.0, 40.0, "VL2440"),
        new IntervalLabel.Interval<>(40.0, null, "VL40XX")
      )
    );

    assertThat(label.apply(null, 39.999)).isEqualTo("VL2440");
    assertThat(label.apply(null, 40.0)).isEqualTo("VL40XX");
    assertThat(label.apply(null, 500.0)).isEqualTo("VL40XX");
  }

  @Test
  void supportsIntervalsWithNoLowerBound() {
    final IntervalLabel<Object, Double, String> label = new IntervalLabel<>(
      NUMBER,
      List.of(
        new IntervalLabel.Interval<>(null, 6.0, "VL0006"),
        new IntervalLabel.Interval<>(6.0, 12.0, "VL0612")
      )
    );

    assertThat(label.apply(null, -500.0)).isEqualTo("VL0006");
    assertThat(label.apply(null, 0.0)).isEqualTo("VL0006");
    assertThat(label.apply(null, 5.999)).isEqualTo("VL0006");
    assertThat(label.apply(null, 6.0)).isEqualTo("VL0612");
  }

  @Test
  void rejectsIntervalsWithInvertedBounds() {
    assertThatThrownBy(() ->
      new IntervalLabel<>(
        NUMBER,
        List.of(new IntervalLabel.Interval<>(12.0, 6.0, "invalid"))
      )
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("lower")
      .hasMessageContaining("upper");
  }

  @Test
  void rejectsZeroWidthIntervals() {
    assertThatThrownBy(() ->
      new IntervalLabel<>(
        NUMBER,
        List.of(new IntervalLabel.Interval<>(12.0, 12.0, "invalid"))
      )
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("lower")
      .hasMessageContaining("upper");
  }

  @Test
  void rejectsOverlappingIntervals() {
    assertThatThrownBy(() ->
      new IntervalLabel<>(
        NUMBER,
        List.of(
          new IntervalLabel.Interval<>(0.0, 12.0, "VL0612"),
          new IntervalLabel.Interval<>(11.0, 18.0, "VL1218")
        )
      )
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("overlap");
  }

  @Test
  void rejectsIntervalsWithNoBounds() {
    assertThatThrownBy(() ->
      new IntervalLabel<>(
        NUMBER,
        List.of(new IntervalLabel.Interval<>(null, null, "invalid"))
      )
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("bound");
  }
}

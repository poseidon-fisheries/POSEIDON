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

package uk.ac.ox.poseidon.calibration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class ParameterRangeTest {

  @Test
  void linearRangePreservesExistingInterpolation() {
    final var range = new LinearParameterRange(-10, 30);

    assertThat(range.decode(0)).isEqualTo(-10.0);
    assertThat(range.decode(0.25)).isEqualTo(0.0);
    assertThat(range.decode(0.5)).isEqualTo(10.0);
    assertThat(range.decode(1)).isEqualTo(30.0);
  }

  @Test
  void finiteLinearBoundsDoNotOverflowDuringInterpolation() {
    final var range = new LinearParameterRange(
      -Double.MAX_VALUE,
      Double.MAX_VALUE
    );

    assertThat(range.decode(0)).isEqualTo(-Double.MAX_VALUE);
    assertThat(range.decode(0.5)).isEqualTo(0.0);
    assertThat(range.decode(1)).isEqualTo(Double.MAX_VALUE);
  }

  @Test
  void logarithmicRangeHasEqualRatiosBetweenEqualSearchSteps() {
    final var range = new LogarithmicParameterRange(0.001, 10);

    assertThat(range.decode(0)).isEqualTo(0.001);
    assertThat(range.decode(0.25)).isCloseTo(0.01, within(1e-15));
    assertThat(range.decode(0.5)).isCloseTo(0.1, within(1e-15));
    assertThat(range.decode(0.75)).isCloseTo(1.0, within(1e-14));
    assertThat(range.decode(1)).isEqualTo(10.0);
  }

  @Test
  void integerRangeGivesEveryIntegerAnEqualWidthBin() {
    final var range = new IntegerParameterRange(-2, 2);

    for (int value = -2; value <= 2; value++) {
      final double lower = (value + 2) / 5.0;
      final double upper = (value + 3) / 5.0;
      assertThat(range.decode(lower)).isEqualTo(value);
      assertThat(range.decode((lower + upper) / 2)).isEqualTo(value);
      assertThat(range.decode(Math.nextDown(upper))).isEqualTo(value);
    }
    assertThat(range.decode(1)).isEqualTo(2);
  }

  @Test
  void integerRangeSupportsTheFullIntDomainWithoutOverflow() {
    final var range = new IntegerParameterRange(
      Integer.MIN_VALUE,
      Integer.MAX_VALUE
    );

    assertThat(range.decode(0)).isEqualTo(Integer.MIN_VALUE);
    assertThat(range.decode(0.5)).isEqualTo(0);
    assertThat(range.decode(1)).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void singletonRangesAlwaysDecodeToTheirOnlyValue() {
    assertThat(new LinearParameterRange(3, 3).decode(0.5)).isEqualTo(3.0);
    assertThat(new LogarithmicParameterRange(3, 3).decode(0.5)).isEqualTo(3.0);
    assertThat(new IntegerParameterRange(3, 3).decode(0.5)).isEqualTo(3);
  }

  @Test
  void rejectsInvalidBounds() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      new LinearParameterRange(2, 1)
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      new LogarithmicParameterRange(2, 1)
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      new IntegerParameterRange(2, 1)
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      new LogarithmicParameterRange(0, 1)
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      new LogarithmicParameterRange(-1, 1)
    );
    for (final double value : new double[] {
      Double.NaN,
      Double.NEGATIVE_INFINITY,
      Double.POSITIVE_INFINITY
    }) {
      assertThatIllegalArgumentException().isThrownBy(() ->
        new LinearParameterRange(value, 1)
      );
      assertThatIllegalArgumentException().isThrownBy(() ->
        new LinearParameterRange(0, value)
      );
      assertThatIllegalArgumentException().isThrownBy(() ->
        new LogarithmicParameterRange(value, 1)
      );
      assertThatIllegalArgumentException().isThrownBy(() ->
        new LogarithmicParameterRange(1, value)
      );
    }
  }

  @Test
  void rejectsSearchCoordinatesOutsideTheUnitInterval() {
    for (final ParameterRange range : new ParameterRange[] {
      new LinearParameterRange(1, 10),
      new LogarithmicParameterRange(1, 10),
      new IntegerParameterRange(1, 10)
    }) {
      for (final double value : new double[] {
        -0.1,
        1.1,
        Double.NaN,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY
      }) {
        assertThatIllegalArgumentException().isThrownBy(() ->
          range.decode(value)
        );
      }
    }
  }
}

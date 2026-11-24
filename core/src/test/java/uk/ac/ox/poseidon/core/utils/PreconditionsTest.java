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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkNonNegative;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkPositive;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkWholeSeconds;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class PreconditionsTest {

  @Test
  void checkNonNegativeReturnsValue() {
    assertThat(checkNonNegative(5, "test")).isEqualTo(5);
  }

  @Test
  void checkNonNegativeAcceptsZero() {
    assertThat(checkNonNegative(0, "test")).isZero();
  }

  @Test
  void checkNonNegativeRejectsNegative() {
    assertThatIllegalArgumentException()
      .isThrownBy(() -> checkNonNegative(-1, "test"))
      .withMessageContaining("test")
      .withMessageContaining("-1");
  }

  @Test
  void checkNonNegativeDoubleReturnsValue() {
    assertThat(checkNonNegative(2.5, "test")).isEqualTo(2.5);
  }

  @Test
  void checkNonNegativeDoubleAcceptsZero() {
    assertThat(checkNonNegative(0.0, "test")).isZero();
  }

  @Test
  void checkNonNegativeDoubleRejectsNegative() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      checkNonNegative(-0.1, "test")
    );
  }

  @Test
  void checkUnitRangeReturnsValue() {
    assertThat(checkUnitRange(0.5, "test")).isEqualTo(0.5);
  }

  @Test
  void checkUnitRangeAcceptsBounds() {
    assertThatNoException().isThrownBy(() -> {
      checkUnitRange(0, "test");
      checkUnitRange(1, "test");
    });
  }

  @Test
  void checkUnitRangeRejectsOutsideBounds() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      checkUnitRange(-0.1, "test")
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      checkUnitRange(1.1, "test")
    );
  }

  @Test
  void checkPositiveReturnsValue() {
    assertThat(checkPositive(1.5, "test")).isEqualTo(1.5);
  }

  @Test
  void checkPositiveRejectsZeroAndNegative() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      checkPositive(0, "test")
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      checkPositive(-1, "test")
    );
  }

  @Test
  void checkWholeSecondsReturnsAWholeSecondValue() {
    final Duration duration = Duration.ofSeconds(-3);
    final LocalDateTime dateTime = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
    final LocalTime time = LocalTime.of(3, 4, 5);
    assertThat(checkWholeSeconds(duration)).isSameAs(duration);
    assertThat(checkWholeSeconds(dateTime)).isSameAs(dateTime);
    assertThat(checkWholeSeconds(time)).isSameAs(time);
  }

  @Test
  void checkWholeSecondsRejectsAFractionalSecondWithOneMessage() {
    assertThatIllegalArgumentException()
      .isThrownBy(() -> checkWholeSeconds(Duration.ofMillis(-500)))
      .withMessage("PT-0.5S has a fractional second.");
    assertThatIllegalArgumentException()
      .isThrownBy(() ->
        checkWholeSeconds(LocalDateTime.of(2025, 1, 2, 3, 4, 5, 1))
      )
      .withMessageContaining("has a fractional second.");
    assertThatIllegalArgumentException()
      .isThrownBy(() -> checkWholeSeconds(LocalTime.of(3, 4, 5, 1)))
      .withMessageContaining("has a fractional second.");
  }
}

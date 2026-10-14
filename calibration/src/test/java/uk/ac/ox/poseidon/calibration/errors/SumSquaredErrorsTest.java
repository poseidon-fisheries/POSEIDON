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

package uk.ac.ox.poseidon.calibration.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SumSquaredErrorsTest {

  @Test
  void sumsTheSquaredErrorsOverTheKeysOfEitherMap() {
    final SumSquaredErrors<String> errors = new SumSquaredErrors<>(
      Map.of("a", 3.0, "b", 1.0),
      _ -> Map.of("a", 1.0, "c", 2.0)
    );
    // a: (3 - 1)², b: (1 - 0)², c: (0 - 2)²
    assertThat(errors.applyAsDouble(null)).isEqualTo(9.0);
  }

  @Test
  void refusesANanTarget() {
    assertThatIllegalArgumentException()
      .isThrownBy(() ->
        new SumSquaredErrors<>(Map.of("a", 1.0, "b", Double.NaN), _ -> Map.of())
      )
      .withMessage(
        "Target values must be finite numbers, but 1 of 2 are not: [b]"
      );
  }

  @Test
  void refusesAnInfiniteTarget() {
    assertThatIllegalArgumentException()
      .isThrownBy(() ->
        new SumSquaredErrors<>(Map.of("a", Double.POSITIVE_INFINITY), _ ->
          Map.of()
        )
      )
      .withMessageContaining("[a]");
  }

  @Test
  void refusesAMissingTarget() {
    final Map<String, Double> targets = new HashMap<>();
    targets.put("a", null);
    assertThatIllegalArgumentException()
      .isThrownBy(() -> new SumSquaredErrors<>(targets, _ -> Map.of()))
      .withMessageContaining("[a]");
  }
}

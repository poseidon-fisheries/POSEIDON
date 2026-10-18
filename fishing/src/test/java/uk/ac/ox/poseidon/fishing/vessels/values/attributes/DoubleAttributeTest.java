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

package uk.ac.ox.poseidon.fishing.vessels.values.attributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.HasAttributes;

class DoubleAttributeTest {

  private final DoubleAttribute length = new DoubleAttribute("loa");

  private static HasAttributes withLength(final Object value) {
    return () -> Map.of("loa", value);
  }

  @Test
  void readsANumber() {
    assertThat(length.apply(withLength(12))).isEqualTo(12.0);
  }

  @Test
  void parsesANumericString() {
    assertThat(length.apply(withLength(" 12.5 "))).isEqualTo(12.5);
  }

  @Test
  void throwsOnANonNumericString() {
    assertThatThrownBy(() ->
      length.apply(withLength("not-a-number"))
    ).isInstanceOf(NumberFormatException.class);
  }

  @Test
  void throwsWhenTheAttributeIsAbsent() {
    assertThatThrownBy(() -> length.apply(Map::of))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("loa");
  }
}

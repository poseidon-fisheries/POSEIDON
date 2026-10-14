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

import org.junit.jupiter.api.Test;

class MultiStringKeyTest {

  @Test
  void joinsMultipleFieldsWithSeparator() {
    assertThat(Utils.multiStringKey("a", "b", "c")).isEqualTo("a;b;c");
  }

  @Test
  void singleFieldHasNoSeparator() {
    assertThat(Utils.multiStringKey("a")).isEqualTo("a");
  }

  @Test
  void emptyVarargsReturnsEmptyString() {
    assertThat(Utils.multiStringKey()).isEqualTo("");
  }

  @Test
  void nullFieldContributesOnlyItsSeparator() {
    assertThat(Utils.multiStringKey("a", null, "b")).isEqualTo("a;;b");
  }

  @Test
  void allNullFieldsProduceOnlySeparators() {
    assertThat(Utils.multiStringKey(null, null, null)).isEqualTo(";;");
  }

  @Test
  void leadingNullProducesLeadingSeparator() {
    assertThat(Utils.multiStringKey(null, "a")).isEqualTo(";a");
  }

  @Test
  void emptyStringIsTreatedAsAbsent() {
    assertThat(Utils.multiStringKey("a", "", "b")).isEqualTo("a;;b");
  }

  @Test
  void naIsTreatedAsAbsent() {
    assertThat(Utils.multiStringKey("a", "NA", "b")).isEqualTo("a;;b");
  }

  @Test
  void naIsCaseInsensitive() {
    assertThat(Utils.multiStringKey("a", "na", "b")).isEqualTo("a;;b");
  }

  @Test
  void trimsValues() {
    assertThat(Utils.multiStringKey("  a  ", "\tb\t")).isEqualTo("a;b");
  }

  @Test
  void rejectsSeparatorInValue() {
    assertThatThrownBy(() -> Utils.multiStringKey("a;b"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(";");
  }

  @Test
  void rejectsClosingParenInValue() {
    assertThatThrownBy(() -> Utils.multiStringKey("a)b"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(")");
  }

  @Test
  void validatesAllFieldsBeforeAppending() {
    assertThatThrownBy(() ->
      Utils.multiStringKey("valid", "also;pipe")
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fieldWithOnlyWhitespaceIsAbsent() {
    assertThat(Utils.multiStringKey("a", "   ", "b")).isEqualTo("a;;b");
  }
}

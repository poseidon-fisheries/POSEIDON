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

package uk.ac.ox.poseidon.fishing.choices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class FallbackPickerTest {

  private final Vessel vessel = mock(Vessel.class);

  @Test
  void triesPickersOnceInOrderAndStopsAfterAnAnswer() {
    final List<Integer> called = new ArrayList<>();
    final FallbackPicker<String> picker = new FallbackPicker<>(
      List.of(
        receiver -> {
          assertThat(receiver).isSameAs(vessel);
          called.add(1);
          return Optional.empty();
        },
        receiver -> {
          called.add(2);
          return Optional.of("answer");
        },
        receiver -> {
          throw new AssertionError("called a later picker");
        }
      )
    );
    assertThat(picker.apply(vessel)).contains("answer");
    assertThat(called).containsExactly(1, 2);
  }

  @Test
  void noAnswerOrNoPickersGivesNoOption() {
    assertThat(new FallbackPicker<String>(List.of()).apply(vessel)).isEmpty();
    assertThat(
      new FallbackPicker<String>(
        List.of(receiver -> Optional.empty(), receiver -> Optional.empty())
      ).apply(vessel)
    ).isEmpty();
  }
}

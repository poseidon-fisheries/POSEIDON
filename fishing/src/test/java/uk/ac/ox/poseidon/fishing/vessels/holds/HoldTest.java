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

package uk.ac.ox.poseidon.fishing.vessels.holds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;

class HoldTest {

  @Test
  void aHoldOverItsCapacityHasNoCapacityAvailable() {
    final CategorisedCatch content = mock(CategorisedCatch.class);
    when(content.getTotalBiomassInKg()).thenReturn(10.5);
    final Hold hold = mock(Hold.class, CALLS_REAL_METHODS);
    when(hold.getTotalCapacityInKg()).thenReturn(10.0);
    when(hold.getContent()).thenReturn(content);

    assertThat(hold.getAvailableCapacityInKg()).isZero();
    assertThat(hold.isFull()).isTrue();
  }

  @Test
  void aHoldWithRoomLeftIsNotFull() {
    final CategorisedCatch content = mock(CategorisedCatch.class);
    when(content.getTotalBiomassInKg()).thenReturn(4.0);
    final Hold hold = mock(Hold.class, CALLS_REAL_METHODS);
    when(hold.getTotalCapacityInKg()).thenReturn(10.0);
    when(hold.getContent()).thenReturn(content);

    assertThat(hold.getAvailableCapacityInKg()).isEqualTo(6.0);
    assertThat(hold.isFull()).isFalse();
  }

  @Test
  void aHoldAtItsCapacityIsFull() {
    final CategorisedCatch content = mock(CategorisedCatch.class);
    when(content.getTotalBiomassInKg()).thenReturn(10.0);
    final Hold hold = mock(Hold.class, CALLS_REAL_METHODS);
    when(hold.getTotalCapacityInKg()).thenReturn(10.0);
    when(hold.getContent()).thenReturn(content);

    assertThat(hold.isFull()).isTrue();
  }

  private static Hold holdWith(
    final double contentKg,
    final double capacityKg
  ) {
    final CategorisedCatch content = mock(CategorisedCatch.class);
    when(content.getTotalBiomassInKg()).thenReturn(contentKg);
    final Hold hold = mock(Hold.class, CALLS_REAL_METHODS);
    when(hold.getTotalCapacityInKg()).thenReturn(capacityKg);
    when(hold.getContent()).thenReturn(content);
    return hold;
  }

  @Test
  void aHoldIsFilledToAProportionOnceItsContentReachesIt() {
    assertThat(holdWith(7.9, 10.0).isFilledTo(0.8)).isFalse();
    assertThat(holdWith(8.0, 10.0).isFilledTo(0.8)).isTrue();
    assertThat(holdWith(9.5, 10.0).isFilledTo(0.8)).isTrue();
  }

  @Test
  void aHoldFilledToWithinFloatingPointErrorOfItsCapacityIsFull() {
    assertThat(holdWith(60.0 - 1e-12, 60.0).isFilledTo(1)).isTrue();
    assertThat(holdWith(60.0 - 1e-3, 60.0).isFilledTo(1)).isFalse();
  }

  @Test
  void aHoldOverItsCapacityIsFilledToAnyProportion() {
    assertThat(holdWith(10.5, 10.0).isFilledTo(1)).isTrue();
  }

  @Test
  void anInfiniteHoldIsNeverFilledToAnything() {
    assertThat(
      holdWith(1e12, Double.POSITIVE_INFINITY).isFilledTo(1)
    ).isFalse();
  }

  @ParameterizedTest
  @ValueSource(doubles = { 0, -0.1, 1.1, Double.NaN })
  void rejectsAProportionOutsideZeroExclusiveToOneInclusive(
    final double proportion
  ) {
    final Hold hold = holdWith(5.0, 10.0);

    assertThatIllegalArgumentException().isThrownBy(() ->
      hold.isFilledTo(proportion)
    );
  }
}

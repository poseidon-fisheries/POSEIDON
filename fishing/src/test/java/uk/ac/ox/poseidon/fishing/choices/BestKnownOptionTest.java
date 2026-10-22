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
import static org.mockito.Mockito.when;

import ec.util.MersenneTwisterFast;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;

class BestKnownOptionTest {

  private static final Cell BEST = new Cell(4, 2);
  private static final Cell WORSE = new Cell(1, 1);
  private static final Cell FALLBACK = new Cell(2, 2);
  private static final Function<Vessel, Cell> NO_FALLBACK = vessel -> {
    throw new AssertionError("The fallback should not be asked for an option.");
  };

  private final MersenneTwisterFast rng = new MersenneTwisterFast(0);
  private final Vessel vessel = mock(Vessel.class);

  @Test
  void givesTheBestKnownOption() {
    final BestKnownOption<Cell> bestKnownOption = new BestKnownOption<>(
      new ImmutableOptionValues<>(Map.of(BEST, 2.0, WORSE, 1.0)),
      NO_FALLBACK,
      rng
    );

    assertThat(bestKnownOption.apply(vessel)).isEqualTo(BEST);
  }

  @Test
  void givesTheFallbackForTheVesselWhenNoOptionIsKnown() {
    final BestKnownOption<Cell> bestKnownOption = new BestKnownOption<>(
      new ImmutableOptionValues<>(Map.of()),
      pickedFor -> pickedFor == vessel ? FALLBACK : null,
      rng
    );

    assertThat(bestKnownOption.apply(vessel)).isEqualTo(FALLBACK);
  }

  @Test
  void readsTheOptionValuesOfTheVesselGiven() {
    final Vessel otherVessel = mock(Vessel.class);
    final OptionValues<Cell> bestValues = new ImmutableOptionValues<>(
      Map.of(BEST, 1.0)
    );
    final OptionValues<Cell> worseValues = new ImmutableOptionValues<>(
      Map.of(WORSE, 1.0)
    );
    final BestKnownOption<Cell> bestKnownOption = new BestKnownOption<>(
      pickedFor -> pickedFor == vessel ? bestValues : worseValues,
      NO_FALLBACK,
      rng
    );

    assertThat(bestKnownOption.apply(vessel)).isEqualTo(BEST);
    assertThat(bestKnownOption.apply(otherVessel)).isEqualTo(WORSE);
  }

  @Test
  void readsTheOptionValuesAnewOnEachCall() {
    @SuppressWarnings("unchecked")
    final OptionValues<Cell> optionValues = mock(OptionValues.class);
    when(optionValues.getBestOption(rng))
      .thenReturn(Optional.of(BEST))
      .thenReturn(Optional.of(WORSE));
    final BestKnownOption<Cell> bestKnownOption = new BestKnownOption<>(
      pickedFor -> optionValues,
      NO_FALLBACK,
      rng
    );

    assertThat(bestKnownOption.apply(vessel)).isEqualTo(BEST);
    assertThat(bestKnownOption.apply(vessel)).isEqualTo(WORSE);
  }
}

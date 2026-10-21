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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;

import ec.util.MersenneTwisterFast;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Epsilon is read for the vessel on each choice, so a test can drive it
 * without touching the RNG: an epsilon of 0 always exploits and an epsilon of
 * 1 always explores, whatever the seed.
 */
class EpsilonGreedyChooserTest {

  private static final String EXPLORED = "explored";
  private static final String EXPLOITED = "exploited";

  private final Vessel vessel = mock(Vessel.class);
  private final MersenneTwisterFast rng = new MersenneTwisterFast(0);
  private final AtomicReference<Double> epsilon = new AtomicReference<>(0.0);

  private EpsilonGreedyChooser<String> chooser() {
    return new EpsilonGreedyChooser<>(
      ignored -> epsilon.get(),
      ignored -> EXPLORED,
      ignored -> EXPLOITED,
      rng
    );
  }

  @Test
  void epsilonOfZeroAlwaysExploits() {
    assertThat(chooser().apply(vessel)).contains(EXPLOITED);
  }

  @Test
  void epsilonOfOneAlwaysExplores() {
    epsilon.set(1.0);
    assertThat(chooser().apply(vessel)).contains(EXPLORED);
  }

  @Test
  void epsilonIsReadOnEachChoice() {
    final EpsilonGreedyChooser<String> chooser = chooser();
    assertThat(chooser.apply(vessel)).contains(EXPLOITED);
    epsilon.set(1.0);
    assertThat(chooser.apply(vessel)).contains(EXPLORED);
  }

  @Test
  void epsilonOutsideTheUnitRangeIsRejectedOnChoice() {
    final EpsilonGreedyChooser<String> chooser = chooser();
    epsilon.set(1.5);
    assertThatIllegalArgumentException().isThrownBy(() ->
      chooser.apply(vessel)
    );
  }

  @Test
  void exploiterWithNothingToOfferFallsBackToExploring() {
    final EpsilonGreedyChooser<String> chooser = new EpsilonGreedyChooser<>(
      ignored -> 0.0,
      ignored -> EXPLORED,
      ignored -> null,
      rng
    );
    assertThat(chooser.apply(vessel)).contains(EXPLORED);
  }

  @Test
  void chooserWithNothingToOfferPicksNothing() {
    final EpsilonGreedyChooser<String> chooser = new EpsilonGreedyChooser<>(
      ignored -> 0.0,
      ignored -> null,
      ignored -> null,
      rng
    );
    assertThat(chooser.apply(vessel)).isEqualTo(Optional.empty());
  }
}

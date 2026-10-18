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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentScopeOf;
import static uk.ac.ox.poseidon.fishing.vessels.holds.Factories.infiniteBiomassHold;
import static uk.ac.ox.poseidon.fishing.vessels.holds.Factories.standardBiomassHold;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.fishing.catches.CatchCategoriser;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;

/** A hold that still holds catch refuses to have its store ended. */
class HoldStoreTest {

  private final Vessel vessel = mock(Vessel.class);
  private final ProductStore store = new ProductStore();
  private final VesselComponentScope scope = componentScopeOf(
    new VesselScope(mock(SimulationScope.class), vessel),
    store
  );

  static Stream<Factory<? super VesselComponentScope, ? extends Hold>> holds() {
    final Factory<VesselComponentScope, CatchCategoriser> categoriser = _ ->
      mock(CatchCategoriser.class);
    return Stream.of(
      infiniteBiomassHold(categoriser),
      standardBiomassHold(
        _ -> getQuantity(100, KILOGRAM),
        _ -> getQuantity(0, KILOGRAM),
        categoriser
      )
    );
  }

  @ParameterizedTest
  @MethodSource("holds")
  void aHoldWithCatchRefusesToEnd(
    final Factory<? super VesselComponentScope, ? extends Hold> holdFactory
  ) {
    when(vessel.toString()).thenReturn("V1");
    final Hold hold = holdFactory.get(scope);
    hold.addContent(
      new CategorisedCatch(
        Map.of(
          new CatchCategory("C1"),
          Bucket.of(new Species("S", "adult", null), 12.5)
        )
      )
    );

    assertThatThrownBy(store::end)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("12.5");
  }

  @ParameterizedTest
  @MethodSource("holds")
  void anEmptyHoldEnds(
    final Factory<? super VesselComponentScope, ? extends Hold> holdFactory
  ) {
    holdFactory.get(scope);

    assertThatCode(store::end).doesNotThrowAnyException();
  }
}

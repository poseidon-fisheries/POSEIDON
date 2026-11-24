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

package uk.ac.ox.poseidon.fishing.vessels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.fishing.vessels.GivenComponents.componentScopeOf;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

final class TripBlockFactoryTest {

  // Kept in fields: a scope holds its vessel weakly
  private final Simulation simulation = mock(Simulation.class);
  private final Vessel vessel = mock(Vessel.class);
  private final VesselComponentScope scope = scope();

  @SuppressWarnings("unchecked")
  private final Task<Vessel> child = mock(Task.class);

  private VesselComponentScope scope() {
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(simulation.getTemporalSchedule()).thenReturn(
      mock(TemporalSchedule.class)
    );
    return componentScopeOf(
      new VesselScope(new SimulationScope(simulation), vessel)
    );
  }

  @Test
  void buildsATripBlockWithItsChildren() {
    final TripBlockFactory factory = new TripBlockFactory(
      _ -> _ -> Optional.empty(),
      ImmutableList.of(_ -> _ -> Money.of(CurrencyUnit.EUR, 1.0)),
      ImmutableList.of(_ -> child)
    );

    final TripBlock tripBlock = factory.get(scope);

    assertThat(tripBlock).isNotNull().isNotInstanceOf(FishingTripBlock.class);
    assertThat(tripBlock.getChildCount()).isEqualTo(1);
    assertThat(tripBlock.getChild(0)).isSameAs(child);
  }

  @Test
  void buildsAFishingTripBlockWithItsChildren() {
    final TripBlockFactory factory = new FishingTripBlockFactory(
      _ -> _ -> Optional.empty(),
      ImmutableList.of(),
      ImmutableList.of(_ -> child)
    );

    final TripBlock tripBlock = factory.get(scope);

    assertThat(tripBlock).isInstanceOf(FishingTripBlock.class);
    assertThat(tripBlock.getChildCount()).isEqualTo(1);
  }
}

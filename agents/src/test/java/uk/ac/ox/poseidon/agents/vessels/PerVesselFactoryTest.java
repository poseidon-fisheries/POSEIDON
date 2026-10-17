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

package uk.ac.ox.poseidon.agents.vessels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.providers.FirstValueFromFactory;

class PerVesselFactoryTest {

  @Test
  void sameVesselReturnsSameInstance() {
    final AtomicInteger counter = new AtomicInteger(0);
    final Factory<? super VesselScope, String> delegate = scope ->
      "instance-" + counter.incrementAndGet();

    final PerVesselFactory<String> factory = new PerVesselFactory<>(delegate);

    final Vessel vessel = mock(Vessel.class);

    when(vessel.getProductStore()).thenReturn(new ProductStore());
    final VesselScope scope1 = mock(VesselScope.class);
    when(scope1.getVessel()).thenReturn(vessel);
    final VesselScope scope2 = mock(VesselScope.class);
    when(scope2.getVessel()).thenReturn(vessel);

    final String first = factory.get(scope1);
    final String second = factory.get(scope2);

    assertThat(second).isSameAs(first).isEqualTo("instance-1");
  }

  @Test
  void differentVesselsGetDifferentInstances() {
    final AtomicInteger counter = new AtomicInteger(0);
    final Factory<? super VesselScope, String> delegate = scope ->
      "instance-" + counter.incrementAndGet();

    final PerVesselFactory<String> factory = new PerVesselFactory<>(delegate);

    final Vessel vesselA = mock(Vessel.class);

    when(vesselA.getProductStore()).thenReturn(new ProductStore());
    final VesselScope scopeA = mock(VesselScope.class);
    when(scopeA.getVessel()).thenReturn(vesselA);

    final Vessel vesselB = mock(Vessel.class);

    when(vesselB.getProductStore()).thenReturn(new ProductStore());
    final VesselScope scopeB = mock(VesselScope.class);
    when(scopeB.getVessel()).thenReturn(vesselB);

    final String fromA = factory.get(scopeA);
    final String fromB = factory.get(scopeB);

    assertThat(fromA).isEqualTo("instance-1");
    assertThat(fromB).isEqualTo("instance-2");
  }

  @Test
  void delegateCalledOnlyOncePerVessel() {
    final AtomicInteger counter = new AtomicInteger(0);
    final Factory<? super VesselScope, String> delegate = scope ->
      "value-" + counter.incrementAndGet();

    final PerVesselFactory<String> factory = new PerVesselFactory<>(delegate);

    final Vessel vessel = mock(Vessel.class);

    when(vessel.getProductStore()).thenReturn(new ProductStore());
    final VesselScope scope = mock(VesselScope.class);
    when(scope.getVessel()).thenReturn(vessel);

    factory.get(scope);
    factory.get(scope);
    factory.get(scope);

    assertThat(counter).hasValue(1);
  }

  private static VesselScope scopeOfNewVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.getProductStore()).thenReturn(new ProductStore());
    final VesselScope scope = mock(VesselScope.class);
    when(scope.getVessel()).thenReturn(vessel);
    return scope;
  }

  private static class CountingValueFactory
    extends GlobalScopeFactory<Function<Object, Integer>>
    implements ValueFactory<Scope, Object, Integer>
  {

    @Override
    protected Function<Object, Integer> newInstance(final Scope scope) {
      final AtomicInteger counter = new AtomicInteger(0);
      return context -> counter.incrementAndGet();
    }
  }

  @Test
  void eachVesselGetsItsOwnInstanceEvenFromASharedDelegate() {
    final CountingValueFactory delegate = new CountingValueFactory();
    final PerVesselFactory<Function<Object, Integer>> factory =
      new PerVesselFactory<>(delegate);
    final VesselScope scopeA = scopeOfNewVessel();
    final VesselScope scopeB = scopeOfNewVessel();

    assertThat(factory.get(scopeA)).isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    assertThat(factory.get(scopeA)).isNotSameAs(delegate.get(scopeA));
  }

  @Test
  void factoriesBuiltOnItAreResolvedOncePerVessel() {
    final FirstValueFromFactory<VesselScope, Integer> factory =
      new FirstValueFromFactory<>(
        new PerVesselValueFactory<>(new CountingValueFactory())
      );
    final VesselScope scopeA = scopeOfNewVessel();
    final VesselScope scopeB = scopeOfNewVessel();

    assertThat(factory.get(scopeA)).isSameAs(factory.get(scopeA));
    assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
  }
}

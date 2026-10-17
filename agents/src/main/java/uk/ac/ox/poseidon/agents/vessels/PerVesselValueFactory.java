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

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.PerScopeFactory;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link PerVesselFactory} counterpart for a value, which keeps its shape: a
 * value built once per vessel is still a {@link ValueFactory}, so it can go
 * wherever a value is expected, e.g.
 * {@code firstValueFrom(perVessel(randomInt(1, 10)))} draws a separate number
 * for each vessel. Built via {@link Factories#perVessel(ValueFactory)}.
 *
 * @param <C> the type of the context
 * @param <R> the type of the value
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class PerVesselValueFactory<C, R>
  extends PerScopeFactory<VesselScope, Function<C, R>>
  implements ValueFactory<VesselScope, C, R>
{

  private final ValueFactory<? super VesselScope, C, R> delegate;

  @Override
  protected ProductStore productStoreFor(final VesselScope scope) {
    return scope.getProductStore();
  }

  @Override
  protected Class<VesselScope> scopeClass() {
    return VesselScope.class;
  }
}

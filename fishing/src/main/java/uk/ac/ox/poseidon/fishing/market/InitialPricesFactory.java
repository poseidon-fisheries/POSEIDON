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

package uk.ac.ox.poseidon.fishing.market;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link SimulationScopeFactory} that applies the resolved
 * {@link MarketPrice}s as soon as it is built, so that markets have them from
 * the start of the simulation, and returns them. Prices are applied when the
 * simulation is built rather than scheduled at its start, so that prices set
 * before the first step, e.g. by a SURIMI {@code UpdateSpeciesPrices} message,
 * are not overwritten. No separate plain component class here: applying the
 * prices is a side effect of building the factory. Built via
 * {@link Factories#initialPrices}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class InitialPricesFactory
  extends SimulationScopeFactory<List<MarketPrice>>
{

  @Singular
  private final ImmutableList<
    Factory<? super SimulationScope, ? extends List<MarketPrice>>
  > marketPrices;

  private final ValueFactory<
    ? super SimulationScope,
    ? super Void,
    LocalDateTime
  > currentDateTime;

  @Override
  protected List<MarketPrice> newInstance(final SimulationScope scope) {
    final List<MarketPrice> resolved = marketPrices
      .stream()
      .flatMap(factory -> factory.get(scope).stream())
      .collect(toImmutableList());
    final LocalDateTime dateTime = currentDateTime.get(scope).apply(null);
    resolved.forEach(marketPrice -> marketPrice.apply(dateTime));
    return resolved;
  }
}

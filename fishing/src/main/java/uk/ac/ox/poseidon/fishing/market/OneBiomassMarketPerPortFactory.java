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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.joda.money.CurrencyUnit;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A {@link SimulationScopeFactory} that builds one {@link BiomassMarket} per
 * port in {@link #portGrid}, without prices, all trading in the currency whose
 * code is {@link #currencyCode}, built via
 * {@link Factories#oneBiomassMarketPerPort}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class OneBiomassMarketPerPortFactory
  extends SimulationScopeFactory<List<BiomassMarket>>
{

  private final Factory<? super SimulationScope, ? extends PortGrid> portGrid;
  private final String currencyCode;
  private final Factory<
    ? super SimulationScope,
    ? extends EventManager
  > eventManager;

  @Override
  protected List<BiomassMarket> newInstance(final SimulationScope scope) {
    checkNotNull(currencyCode, "currencyCode must not be null");
    final CurrencyUnit currencyUnit = CurrencyUnit.of(currencyCode);
    final EventManager eventManager = this.eventManager.get(scope);
    return portGrid
      .get(scope)
      .getPorts()
      .map(port ->
        new BiomassMarket(port, port.getCode(), currencyUnit, eventManager)
      )
      .toList();
  }
}

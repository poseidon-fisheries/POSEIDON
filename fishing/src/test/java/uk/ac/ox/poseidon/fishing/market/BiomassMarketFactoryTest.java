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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.joda.money.CurrencyUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.geography.ports.Port;

final class BiomassMarketFactoryTest {

  private final Simulation simulation = mock(Simulation.class);
  private final SimulationScope scope = new SimulationScope(simulation);
  private final Port port = mock(Port.class);
  private final EventManager eventManager = mock(EventManager.class);

  @BeforeEach
  void setUp() {
    when(simulation.getProductStore()).thenReturn(new ProductStore());
    when(port.getCode()).thenReturn("PORT");
  }

  @Test
  void usesTheGivenMarketCode() {
    final BiomassMarket market = new BiomassMarketFactory(
      _ -> port,
      "MARKET",
      "EUR",
      _ -> eventManager
    ).get(scope);

    assertThat(market.getCode()).isEqualTo("MARKET");
    assertThat(market.getPort()).isSameAs(port);
    assertThat(market.getCurrencyUnit()).isEqualTo(CurrencyUnit.EUR);
  }

  @Test
  void defaultsTheMarketCodeToThePortsCode() {
    final BiomassMarket market = new BiomassMarketFactory(
      _ -> port,
      null,
      "EUR",
      _ -> eventManager
    ).get(scope);

    assertThat(market.getCode()).isEqualTo("PORT");
  }
}

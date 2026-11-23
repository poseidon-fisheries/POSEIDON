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

package uk.ac.ox.poseidon.fishing.vessels.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import uk.ac.ox.poseidon.fishing.vessels.Fleet;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class FixedCostCollectorTest {

  private final Vessel small = vessel(true);
  private final Vessel large = vessel(true);
  private final Vessel absent = vessel(false);
  private final Map<Vessel, Money> costs = Map.of(
    small,
    Money.of(CurrencyUnit.EUR, 10),
    large,
    Money.of(CurrencyUnit.EUR, 25),
    absent,
    Money.of(CurrencyUnit.EUR, 50)
  );
  private final FixedCostCollector collector = new FixedCostCollector(
    fleet(),
    costs::get
  );

  @Test
  void step_chargesEachPresentVesselItsOwnCostOnce() {
    collector.step(mock(SimState.class));

    assertThat(balance(small)).isEqualTo(Money.of(CurrencyUnit.EUR, -10));
    assertThat(balance(large)).isEqualTo(Money.of(CurrencyUnit.EUR, -25));
  }

  @Test
  void step_chargesAgainOnEachStep() {
    collector.step(mock(SimState.class));
    collector.step(mock(SimState.class));

    assertThat(balance(small)).isEqualTo(Money.of(CurrencyUnit.EUR, -20));
    assertThat(balance(large)).isEqualTo(Money.of(CurrencyUnit.EUR, -50));
  }

  @Test
  void step_doesNotChargeAbsentVessels() {
    collector.step(mock(SimState.class));

    assertThat(absent.getAccount().getBalances()).isEmpty();
  }

  private Fleet fleet() {
    final Fleet fleet = mock(Fleet.class);
    when(fleet.getVessels()).thenReturn(Set.of(small, large, absent));
    return fleet;
  }

  private static Vessel vessel(final boolean present) {
    final Vessel vessel = mock(Vessel.class);
    final Account account = new Account();
    when(vessel.isPresent()).thenReturn(present);
    when(vessel.getAccount()).thenReturn(account);
    return vessel;
  }

  private static Money balance(final Vessel vessel) {
    return vessel.getAccount().getBalances().get(CurrencyUnit.EUR);
  }
}

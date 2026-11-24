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

package uk.ac.ox.poseidon.fishing.travel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.geography.grids.Cell;

class TimeCostTest {

  private static final CurrencyUnit EUR = CurrencyUnit.of("EUR");

  private final Vessel vessel = mock(Vessel.class);
  private final Cell ninetyMinutesAway = new Cell(90, 0);
  private final AtomicReference<Money> hourlyCost = new AtomicReference<>(
    Money.of(EUR, 10)
  );
  private final TimeCost<Cell> timeCost = new TimeCost<>(
    (travellingVessel, cell) ->
      travellingVessel == vessel
        ? Duration.ofMinutes(cell.getX())
        : Duration.ofMinutes(cell.getX() * 2L),
    costedVessel ->
      costedVessel == vessel ? hourlyCost.get() : Money.of(EUR, 100)
  );

  @Test
  void chargesTheHourlyCostForEachHourOfTravelTime() {
    assertThat(
      timeCost.forVessel(vessel).applyAsDouble(ninetyMinutesAway)
    ).isEqualTo(15.0);
  }

  @Test
  void chargesTheHourlyCostAndTravelTimeOfTheVesselGiven() {
    assertThat(
      timeCost.forVessel(mock(Vessel.class)).applyAsDouble(ninetyMinutesAway)
    ).isEqualTo(300.0);
  }

  @Test
  void roundsHalfEvenToTheCurrencysMinorUnit() {
    hourlyCost.set(Money.of(EUR, 10));

    assertThat(
      timeCost.forVessel(vessel).applyAsDouble(new Cell(1, 0))
    ).isEqualTo(0.17);
  }

  @Test
  void readsTheVesselsHourlyCostEachTimeTheVesselIsGiven() {
    final double costBefore = timeCost
      .forVessel(vessel)
      .applyAsDouble(ninetyMinutesAway);
    hourlyCost.set(Money.of(EUR, 20));
    final double costAfter = timeCost
      .forVessel(vessel)
      .applyAsDouble(ninetyMinutesAway);

    assertThat(costBefore).isEqualTo(15.0);
    assertThat(costAfter).isEqualTo(30.0);
  }

  @Test
  void keepsTheHourlyCostReadForEveryCellCostedThen() {
    final ToDoubleFunction<Cell> costOfCell = timeCost.forVessel(vessel);
    hourlyCost.set(Money.of(EUR, 20));

    assertThat(costOfCell.applyAsDouble(ninetyMinutesAway)).isEqualTo(15.0);
  }

  @Test
  void throwsWithoutAnHourlyCost() {
    hourlyCost.set(null);

    assertThatThrownBy(() -> timeCost.forVessel(vessel)).isInstanceOf(
      NullPointerException.class
    );
  }
}

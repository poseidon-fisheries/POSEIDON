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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

class NetValuationTest {

  private final Vessel vessel = mock(Vessel.class);

  @Test
  void subtractsTheCostValuationFromTheRevenueValuation() {
    final NetValuation<String, Double> netValuation = new NetValuation<>(
      valuedFor -> (option, recollection) -> 10.0,
      valuedFor -> (option, recollection) -> 4.0
    );

    assertThat(netValuation.inContextOf(vessel).valueOf("A", 1.0)).isEqualTo(
      6.0
    );
  }

  @Test
  void givesBothValuationsTheSameVesselOptionAndRecollection() {
    final List<Object> revenueArguments = new ArrayList<>();
    final List<Object> costArguments = new ArrayList<>();
    final NetValuation<String, Double> netValuation = new NetValuation<>(
      valuedFor -> (option, recollection) -> {
        revenueArguments.add(valuedFor);
        revenueArguments.add(option);
        revenueArguments.add(recollection);
        return 0.0;
      },
      valuedFor -> (option, recollection) -> {
        costArguments.add(valuedFor);
        costArguments.add(option);
        costArguments.add(recollection);
        return 0.0;
      }
    );

    netValuation.inContextOf(vessel).valueOf("A", 1.0);

    assertThat(revenueArguments).containsExactly(vessel, "A", 1.0);
    assertThat(costArguments).containsExactly(vessel, "A", 1.0);
  }
}

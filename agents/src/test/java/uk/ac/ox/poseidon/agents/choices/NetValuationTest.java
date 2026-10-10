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

package uk.ac.ox.poseidon.agents.choices;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetValuationTest {

    @Test
    void subtractsTheCostValuationFromTheRevenueValuation() {
        final NetValuation<String, Double> netValuation = new NetValuation<>(
            (option, recollection) -> 10.0,
            (option, recollection) -> 4.0
        );

        assertThat(netValuation.applyAsDouble("A", 1.0)).isEqualTo(6.0);
    }

    @Test
    void givesBothValuationsTheSameOptionAndRecollection() {
        final List<Object> revenueArguments = new ArrayList<>();
        final List<Object> costArguments = new ArrayList<>();
        final NetValuation<String, Double> netValuation = new NetValuation<>(
            (option, recollection) -> {
                revenueArguments.add(option);
                revenueArguments.add(recollection);
                return 0.0;
            },
            (option, recollection) -> {
                costArguments.add(option);
                costArguments.add(recollection);
                return 0.0;
            }
        );

        netValuation.applyAsDouble("A", 1.0);

        assertThat(revenueArguments).containsExactly("A", 1.0);
        assertThat(costArguments).containsExactly("A", 1.0);
    }
}

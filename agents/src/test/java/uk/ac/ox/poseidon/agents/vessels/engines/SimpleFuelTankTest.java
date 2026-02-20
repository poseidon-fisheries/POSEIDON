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

package uk.ac.ox.poseidon.agents.vessels.engines;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleFuelTankTest {

    @Test
    void consumeFuelReducesCurrentFuel() {
        final SimpleFuelTank tank = new SimpleFuelTank(100.0, 70.0);

        tank.consumeFuel(20.0);

        assertThat(tank.getCurrentFuelInLitres()).isEqualTo(50.0);
    }

    @Test
    void consumeFuelRejectsNegativeVolume() {
        final SimpleFuelTank tank = new SimpleFuelTank(100.0, 70.0);

        assertThatThrownBy(() -> tank.consumeFuel(-1.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void consumeFuelRejectsVolumeAboveCurrentFuel() {
        final SimpleFuelTank tank = new SimpleFuelTank(100.0, 70.0);

        assertThatThrownBy(() -> tank.consumeFuel(71.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addFuelClipsToCapacityWhenSlightlyOver() {
        final SimpleFuelTank tank = new SimpleFuelTank(100.0, 99.9999999995);

        tank.addFuel(0.000000001);

        assertThat(tank.getCurrentFuelInLitres()).isEqualTo(100.0);
    }

    @Test
    void consumeFuelClipsToZeroWhenSlightlyUnder() {
        final SimpleFuelTank tank = new SimpleFuelTank(100.0, 0.0000000005);

        tank.consumeFuel(0.000000001);

        assertThat(tank.getCurrentFuelInLitres()).isEqualTo(0.0);
    }

    @Test
    void constructorRejectsCurrentFuelAboveCapacity() {
        assertThatThrownBy(() -> new SimpleFuelTank(100.0, 101.0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

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
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.quantities.VolumeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.agents.vessels.engines.Factories.tank;

class SimpleFuelTankFactoryTest {

    @Test
    void createsSimpleFuelTankUsingVolumeFactories() {
        final SimpleFuelTankFactory factory =
            tank(
                new VolumeFactory(2.0, "m3"),
                new VolumeFactory(0.1255, "m3")
            );

        final FuelTank tank = factory.get(vesselScope());

        assertThat(tank).isInstanceOf(SimpleFuelTank.class);
        assertThat(tank.getCapacityInLitres()).isEqualTo(2000.0);
        assertThat(tank.getCurrentFuelInLitres()).isEqualTo(125.5);
    }

    @Test
    void createsOneFuelTankPerVessel() {
        final SimpleFuelTankFactory factory =
            tank(
                new VolumeFactory(100.0, "l"),
                new VolumeFactory(100.0, "l")
            );

        final FuelTank firstTank = factory.get(vesselScope());
        final FuelTank secondTank = factory.get(vesselScope());

        firstTank.consumeFuel(10.0);

        assertThat(firstTank.getCurrentFuelInLitres()).isEqualTo(90.0);
        assertThat(secondTank.getCurrentFuelInLitres()).isEqualTo(100.0);
    }

    private static VesselScope vesselScope() {
        return new VesselScope(
            new SimulationScope(mock(Simulation.class)),
            mock(Vessel.class)
        );
    }
}

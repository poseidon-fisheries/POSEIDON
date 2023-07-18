/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class EngineTest {


    @Test
    public void moreEfficientisMoreEfficient() throws Exception {

        final Engine inefficient = new Engine(100, 100, 1);
        final Engine efficient = new Engine(100, 50, 1);

        Assertions.assertTrue(efficient.getGasConsumptionPerKm(1) < inefficient.getGasConsumptionPerKm(1));
        Assertions.assertEquals(efficient.getGasConsumptionPerKm(1), 50, .0001);

    }

    @Test
    public void consumeAndRefill() {

        FuelTank tank = new FuelTank(1000);
        tank.consume(100);
        tank.consume(25);
        Assertions.assertEquals(tank.getLitersOfFuelInTank(), 875, .0001);
        Assertions.assertEquals(tank.refill(), 125, .0001);
        Assertions.assertEquals(tank.getLitersOfFuelInTank(), 1000, .0001);
        Assertions.assertEquals(tank.getFuelCapacityInLiters(), 1000, .0001);

    }
}
package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.Test;

import static org.junit.Assert.*;


public class FuelTankTest {


    @Test
    public void consumeAndRefill() throws Exception {

        FuelTank tank = new FuelTank(1000);
        tank.consume(100);
        tank.consume(25);
        assertEquals(tank.getLitersOfFuelInTank(),875,.0001);
        assertEquals(tank.refill(),125,.0001);
        assertEquals(tank.getLitersOfFuelInTank(),1000,.0001);
        assertEquals(tank.getFuelCapacityInLiters(),1000,.0001);

    }
}
package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.Test;

import static org.junit.Assert.*;


public class EngineTest {


    @Test
    public void moreEfficientisMoreEfficient() throws Exception {

        final Engine inefficient = new Engine(100, 100,1);
        final Engine efficient = new Engine(100, 50,1);

        assertTrue(efficient.getGasConsumptionPerKm(1)<inefficient.getGasConsumptionPerKm(1));
        assertEquals(efficient.getGasConsumptionPerKm(1),50,.0001);

    }
}
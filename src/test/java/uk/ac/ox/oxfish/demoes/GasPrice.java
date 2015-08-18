package uk.ac.ox.oxfish.demoes;


import org.junit.Assert;
import org.junit.Test;

import static uk.ac.ox.oxfish.experiments.BestGearWins.efficiencyImitation;

public class GasPrice {

    @Test
    public void expensiveOilPushesEfficientGear() throws Exception {

        double averageTech = 0;
        for(int i=0; i<3; i++)
            averageTech+=efficiencyImitation(.2, 20, "Independent Logistic", System.currentTimeMillis()).getLatest();


        Assert.assertTrue(averageTech/3d < 5);



    }

    @Test
    public void freeGasNoPointImprovingGear() throws Exception {
        double averageTech = 0;
        for(int i=0; i<3; i++)
            averageTech+=efficiencyImitation(0, 20, "Independent Logistic", System.currentTimeMillis()).getLatest();


        Assert.assertTrue(averageTech/3d > 5);



    }
}

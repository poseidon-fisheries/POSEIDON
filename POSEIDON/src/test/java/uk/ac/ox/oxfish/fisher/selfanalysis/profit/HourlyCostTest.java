package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;

import static org.mockito.Mockito.mock;

public class HourlyCostTest {

    @Test
    public void additionalCost() {


        HourlyCost cost = new HourlyCost(10);
        Assertions.assertEquals(20, cost.expectedAdditionalCosts(
            mock(Fisher.class),
            2,
            1,
            -999
        ), .001);


    }
}
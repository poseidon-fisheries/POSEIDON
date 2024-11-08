package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EffortCostTest {


    @Test
    public void costCorrect() {

        final TripRecord record = mock(TripRecord.class);
        when(record.getEffort()).thenReturn(5);
        EffortCost cost = new EffortCost(12);
        final double effortCost = cost.cost(
            mock(Fisher.class),
            mock(FishState.class),
            record,
            0d,
            100
        );

        Assertions.assertEquals(effortCost, 60, .001);

    }

    @Test
    public void additionalCostCorrect() {

        EffortCost cost = new EffortCost(10);
        Assertions.assertEquals(20, cost.expectedAdditionalCosts(
            mock(Fisher.class),
            999,
            2,
            -999
        ), .001);


    }
}
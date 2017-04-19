package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishOnceFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishOnceStrategyTest
{

    @Test
    public void fishOnce() throws Exception {

        TripRecord record = new TripRecord(1,100d);

        FishOnceStrategy strategy = new FishOnceFactory().apply(mock(FishState.class));


        //should be true: you haven't fished before
        assertTrue(
                strategy.shouldFish(mock(Fisher.class),
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    record)
        );

        //record a single fish
        record.recordFishing(new FishingRecord(1,
                                               mock(Gear.class),
                                               mock(SeaTile.class),
                                               mock(Catch.class),
                                               mock(Fisher.class),
                                               11));

        //should be false: you have fished at least once~!
        assertFalse(
                strategy.shouldFish(mock(Fisher.class),
                                    new MersenneTwisterFast(),
                                    mock(FishState.class),
                                    record)
        );
    }
}
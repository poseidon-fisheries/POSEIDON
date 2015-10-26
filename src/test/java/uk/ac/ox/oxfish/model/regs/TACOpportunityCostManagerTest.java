package uk.ac.ox.oxfish.model.regs;

import javafx.collections.FXCollections;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;

import java.util.Collections;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class TACOpportunityCostManagerTest {


    @Test
    public void averagesOutCorrectly() throws Exception {


        FishState model = mock(FishState.class);
        Specie species = new Specie("dummy");
        when(model.getSpecies()).thenReturn(Collections.singletonList(species));
        when(model.getFishers()).thenReturn(FXCollections.observableList(new LinkedList<>()));
        MultiQuotaRegulation quotas = new MultiQuotaRegulation(new double[]{100}, model);
        TACOpportunityCostManager manager = new TACOpportunityCostManager(
                quotas);

        manager.start(model);


        //feed crap in the manager
        TripRecord record = mock(TripRecord.class);
        when(model.getLatestDailyObservation("dummy " + AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(10d);
        when(record.getDurationInHours()).thenReturn(10d);
        when(record.getFinalCatch()).thenReturn(new double[1]);
        manager.reactToFinishedTrip(record);
        manager.step(model);

        when(model.getLatestDailyObservation("dummy " + AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(20d);
        when(record.getDurationInHours()).thenReturn(20d);
        when(record.getFinalCatch()).thenReturn(new double[1]);
        manager.reactToFinishedTrip(record);
        manager.step(model);

        when(model.getLatestDailyObservation("dummy " + AbstractMarket.LANDINGS_COLUMN_NAME)).thenReturn(30d);
        when(record.getDurationInHours()).thenReturn(30d);
        when(record.getFinalCatch()).thenReturn(new double[1]);
        manager.reactToFinishedTrip(record);
        manager.step(model);


        //so now it should have learned that there is 1 unit of catch a day on average
        assertEquals(1d, manager.predictedHourlyCatches(0), .0001);

        //now tell him that this dude has caught 10 fish in 20 hours
        TripRecord newRecord = mock(TripRecord.class);
        when(newRecord.getDurationInHours()).thenReturn(20d);
        when(newRecord.getFinalCatch()).thenReturn(new double[]{10d});
        when(newRecord.getImplicitPriceReceived(species)).thenReturn(20d);
        manager.reactToFinishedTrip(newRecord);
        //opportunity costs predicted 0.5 fish per hour; you've been out 20 hours and each unit of fish is worth 20$
        verify(newRecord,times(1)).recordOpportunityCosts(0.5*20*20);


    }
}
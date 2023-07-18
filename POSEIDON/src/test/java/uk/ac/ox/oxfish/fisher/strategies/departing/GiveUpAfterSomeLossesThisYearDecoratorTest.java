package uk.ac.ox.oxfish.fisher.strategies.departing;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GiveUpAfterSomeLossesThisYearDecoratorTest {


    @Test
    public void countsLosses() {

        //delegate always says to go out; decorator will stop when the number of trips you make less than 100 gets to 3
        GiveUpAfterSomeLossesThisYearDecorator decorator = new GiveUpAfterSomeLossesThisYearDecorator(
            3,
            100,
            (fisher, model, random) -> true
        );
        Fisher fake = mock(Fisher.class);
        decorator.start(mock(FishState.class), fake);


        TripRecord goodTrip = mock(TripRecord.class);
        when(goodTrip.getTotalTripProfit()).thenReturn(200d);

        TripRecord badTrip = mock(TripRecord.class);
        when(badTrip.getTotalTripProfit()).thenReturn(50d);


        decorator.reactToFinishedTrip(
            goodTrip,
            fake
        );
        assertEquals(decorator.getBadTrips(), 0);
        assertEquals(decorator.isGivenUp(), false);

        for (int i = 0; i < 2; i++) {
            decorator.reactToFinishedTrip(
                badTrip,
                fake
            );
            assertEquals(decorator.getBadTrips(), i + 1);
            assertEquals(decorator.isGivenUp(), false);
        }

        decorator.reset();

        for (int i = 0; i < 2; i++) {
            decorator.reactToFinishedTrip(
                badTrip,
                fake
            );
            assertEquals(decorator.getBadTrips(), i + 1);
            assertEquals(decorator.isGivenUp(), false);
        }
        decorator.reactToFinishedTrip(
            badTrip,
            fake
        );
        assertEquals(decorator.getBadTrips(), 3);
        assertEquals(decorator.isGivenUp(), true);


    }
}
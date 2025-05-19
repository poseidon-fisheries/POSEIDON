/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;

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
        Assertions.assertEquals(decorator.getBadTrips(), 0);
        Assertions.assertEquals(decorator.isGivenUp(), false);

        for (int i = 0; i < 2; i++) {
            decorator.reactToFinishedTrip(
                badTrip,
                fake
            );
            Assertions.assertEquals(decorator.getBadTrips(), i + 1);
            Assertions.assertEquals(decorator.isGivenUp(), false);
        }

        decorator.reset();

        for (int i = 0; i < 2; i++) {
            decorator.reactToFinishedTrip(
                badTrip,
                fake
            );
            Assertions.assertEquals(decorator.getBadTrips(), i + 1);
            Assertions.assertEquals(decorator.isGivenUp(), false);
        }
        decorator.reactToFinishedTrip(
            badTrip,
            fake
        );
        Assertions.assertEquals(decorator.getBadTrips(), 3);
        Assertions.assertEquals(decorator.isGivenUp(), true);


    }
}

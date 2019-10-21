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

package uk.ac.ox.oxfish.fisher.log;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class TripLoggerTest
{


    @Test
    public void logsHistoryCorrectly() throws Exception {


        TripLogger logger = new TripLogger();
        logger.start(mock(FishState.class));
        logger.setNumberOfSpecies(1);

        assertNull(logger.getCurrentTrip());
        assertEquals(logger.getFinishedTrips().size(),0);
        //create a new trip, now there is a current trip, but it's not in the history
        logger.newTrip(0,0);
        assertNotNull(logger.getCurrentTrip());
        assertEquals(logger.getFinishedTrips().size(),0);

        logger.recordFishing(new FishingRecord(1,
                                               mock(SeaTile.class),
                                               new Catch(new double[]{100,100})));
        logger.recordEarnings(0,100,100);
        logger.recordCosts(200);
        logger.finishTrip(10, mock(Port.class), mock(Fisher.class, RETURNS_DEEP_STUBS));
        //even though it's over, it is still there as current trip
        assertTrue(logger.getCurrentTrip().isCompleted());
        assertEquals(logger.getCurrentTrip().getProfitPerHour(false),-10,.001);
        assertEquals(logger.getCurrentTrip().getProfitPerSpecie(0, false),-100,.001);
        assertEquals(logger.getCurrentTrip().getUnitProfitPerSpecie(0),-1,.001);
        assertEquals(logger.getFinishedTrips().size(),1);


    }

    @Test
    public void notifiesCorrectly() throws Exception {

        TripListener receiver = mock(TripListener.class);
        TripLogger logger = new TripLogger();
        logger.setNumberOfSpecies(0);

        logger.addTripListener(receiver);
        logger.newTrip(0,0);
        TripRecord record = logger.getCurrentTrip();

        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        logger.finishTrip(1, mock(Port.class), fisher);
        verify(receiver).reactToFinishedTrip(record, fisher);



    }
}
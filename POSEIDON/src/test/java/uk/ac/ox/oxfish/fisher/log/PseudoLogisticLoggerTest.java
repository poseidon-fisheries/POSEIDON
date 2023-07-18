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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 1/21/17.
 */
public class PseudoLogisticLoggerTest {

    @Test
    public void logs() throws Exception {

        FishState state = MovingTest.generateSimple4x4Map();
        when(state.getDay()).thenReturn(0);
        when(state.getYear()).thenReturn(123);
        when(state.getDayOfTheYear()).thenReturn(456); //meaningless, just to check that it gets logged correctly
        MapDiscretization discretization = new MapDiscretization(
            new SquaresMapDiscretizer(3, 3));
        discretization.discretize(state.getMap());

        ObservationExtractor[] extractors = new ObservationExtractor[3];
        //these extractors return the x and y of the tile ad also the time. Not realistic but it's a stub
        extractors[0] = new GridXExtractor();
        extractors[1] = new GridYExtractor();
        extractors[2] = (tile, timeOfObservation, agent, model) -> model.getDay();

        //prepare everything
        LogisticLog log = new LogisticLog(new String[]{"x", "y"}, 0);
        PseudoLogisticLogger logger = new PseudoLogisticLogger(
            discretization,
            extractors,
            log,
            mock(Fisher.class),
            state,
            new MersenneTwisterFast()
        );

        TripRecord record = mock(TripRecord.class);
        //the destination will be ignored since the logger assumes the first trip is random
        SeaTile destination = state.
            getMap().getSeaTile(1, 1);
        when(record.getMostFishedTileInTrip()).thenReturn(destination);
        logger.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));

        //this destination will not be ignored, but it will be associated with the inputs recorded at the end of previous trip
        destination = state.
            getMap().getSeaTile(0, 1);
        when(record.getMostFishedTileInTrip()).thenReturn(destination);
        when(state.getDay()).thenReturn(100);
        logger.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));

        System.out.print(log.getData().toString());
        String[] csv = log.getData().toString().trim().split("\n");
        Assertions.assertEquals(csv.length, 16); //it comes in long format
        //0,1 is actually group 4 (square goes vertical first)
        for (int row = 0; row < 16; row++) {
            if (row == 4)
                Assertions.assertTrue(csv[row].contains("yes"));
            else
                Assertions.assertFalse(csv[row].contains("yes"));

        }
        //check that rows are correct
        //id,trip#,year,day,arm,chosen,x,y,time
        Assertions.assertTrue("0,0,123,456,6,no,2.0,1.0,0.0".equals(csv[6]));


        //add one more observation
        destination = state.
            getMap().getSeaTile(0, 0);
        when(record.getMostFishedTileInTrip()).thenReturn(destination);
        logger.reactToFinishedTrip(record, mock(Fisher.class, RETURNS_DEEP_STUBS));


        System.out.println("***************************************");
        System.out.print(log.getData().toString());
        csv = log.getData().toString().trim().split("\n");
        Assertions.assertEquals(csv.length, 32); //it comes in long format
        //0,1 is actually group 4 (square goes vertical first)
        for (int row = 16; row < 32; row++) {
            if (row == 16)
                Assertions.assertTrue(csv[row].contains("yes"));
            else
                Assertions.assertFalse(csv[row].contains("yes"));

        }
        //check that rows are correct
        //id,trip#,year,day,arm,chosen,x,y,time
        Assertions.assertTrue("0,1,123,456,0,yes,0.0,0.0,100.0".equals(csv[16]));
    }
}
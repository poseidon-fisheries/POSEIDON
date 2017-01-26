package uk.ac.ox.oxfish.fisher.log;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 1/21/17.
 */
public class PseudoLogisticLoggerTest {

    @Test
    public void logs() throws Exception {

        FishState state = MovingTest.generateSimple4x4Map();
        when(state.getDay()).thenReturn(0d);
        MapDiscretization discretization = new MapDiscretization(
                new SquaresMapDiscretizer(3,3));
        discretization.discretize(state.getMap());

        ObservationExtractor[] extractors = new ObservationExtractor[3];
        //these extractors return the x and y of the tile ad also the time. Not realistic but it's a stub
        extractors[0] = (tile, timeOfObservation, agent, model) -> tile.getGridX();
        extractors[1] = (tile, timeOfObservation, agent, model) -> tile.getGridY();
        extractors[2] = (tile, timeOfObservation, agent, model) -> model.getDay();

        //prepare everything
        LogisticLog log = new LogisticLog(new String[]{"x","y"},0);
        PseudoLogisticLogger logger = new PseudoLogisticLogger(discretization,
                                                               extractors,
                                                               log,
                                                               mock(Fisher.class),
                                                               state,
                                                               new MersenneTwisterFast());

        TripRecord record = mock(TripRecord.class);
        //the destination will be ignored since the logger assumes the first trip is random
        SeaTile destination = state.
                getMap().getSeaTile(1, 1);
        when(record.getMostFishedTileInTrip()).thenReturn(destination);
        logger.reactToFinishedTrip(record);

        //this destination will not be ignored, but it will be associated with the inputs recorded at the end of previous trip
        destination = state.
                getMap().getSeaTile(0, 1);
        when(record.getMostFishedTileInTrip()).thenReturn(destination);
        when(state.getDay()).thenReturn(100d);
        logger.reactToFinishedTrip(record);

        System.out.print(log.getData().toString());
        String[] csv = log.getData().toString().trim().split("\n");
        assertEquals(csv.length, 16); //it comes in long format
        //0,1 is actually group 4 (square goes vertical first)
        for(int row=0; row<16; row++) {
            if (row == 4)
                assertTrue(csv[row].contains("yes"));
            else
                assertFalse(csv[row].contains("yes"));

        }
        //check that rows are correct
        //id,trip#,arm,chosen,x,y,time
        assertTrue("0,0,6,no,2.0,1.0,0.0".equals(csv[6]));


        //add one more observation
        destination = state.
                getMap().getSeaTile(0, 0);
        when(record.getMostFishedTileInTrip()).thenReturn(destination);
        logger.reactToFinishedTrip(record);


        System.out.println("***************************************");
        System.out.print(log.getData().toString());
        csv = log.getData().toString().trim().split("\n");
        assertEquals(csv.length, 32); //it comes in long format
        //0,1 is actually group 4 (square goes vertical first)
        for(int row=16; row<32; row++) {
            if (row == 16)
                assertTrue(csv[row].contains("yes"));
            else
                assertFalse(csv[row].contains("yes"));

        }
        //check that rows are correct
        //id,trip#,arm,chosen,x,y,time
        assertTrue("0,1,0,yes,0.0,0.0,100.0".equals(csv[16]));
    }
}
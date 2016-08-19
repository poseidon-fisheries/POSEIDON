package uk.ac.ox.oxfish.fisher.heatmap;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.HillClimberAcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/28/16.
 */
public class HillClimberAcquisitionFunctionTest {


    //best spot is 25x25


    @Test
    public void hillclimbsTo2525() throws Exception {


        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
        FishState state = MovingTest.generateSimple50x50Map();
        when(state.getRandom()).thenReturn(random);
        when(state.getHoursSinceStart()).thenReturn(120d);

        HillClimberAcquisitionFunction acquisitionFunction = new HillClimberAcquisitionFunction(1);
        HillClimberAcquisitionFunction acquisitionFunction2 = new HillClimberAcquisitionFunction(3);

        GeographicalRegression regression = mock(GeographicalRegression.class);
        when(regression.predict(any(SeaTile.class), eq(120d), any())).thenAnswer((Answer<Double>) invocation -> {
            SeaTile seaTile = (SeaTile) invocation.getArguments()[0];
            double toReturn = -Math.abs(seaTile.getGridX()-25) -Math.abs(seaTile.getGridY()-25);
            return toReturn;
        });


        SeaTile pick = acquisitionFunction.pick(state.getMap(), regression, state, null,null );
        assertEquals(pick.getGridX(),25);
        assertEquals(pick.getGridY(),25);

        pick = acquisitionFunction2.pick(state.getMap(), regression, state, null,null );
        assertEquals(pick.getGridX(),25);
        assertEquals(pick.getGridY(),25);


    }
}
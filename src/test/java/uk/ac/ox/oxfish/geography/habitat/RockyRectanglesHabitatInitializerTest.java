package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class RockyRectanglesHabitatInitializerTest {


    @Test
    public void notEmpty() throws Exception {



        for(int attempt = 0; attempt<10; attempt++)
        {
            FishState state = generateSimple4x4Map();
            when(state.getDailyDataSet()).thenReturn(mock(FishStateDailyTimeSeries.class));
            NauticalMap map = state.getMap();
            MersenneTwisterFast random = new MersenneTwisterFast();
            RockyRectanglesHabitatInitializer initializer = new RockyRectanglesHabitatInitializer(3, 3, 3, 3, 1);

            initializer.applyHabitats(map,random,state );
            int count = 0;
            for(SeaTile tile : map.getAllSeaTilesAsList())
                if(tile.getHabitat().getHardPercentage() > .99d)
                    count++;
            Assert.assertTrue(count>0);
            Assert.assertTrue(count<=9);
        }

    }
}
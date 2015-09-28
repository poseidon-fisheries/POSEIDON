package uk.ac.ox.oxfish.geography.habitat;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.fisher.actions.MovingTest.generateSimple4x4Map;


public class RockyRectanglesInitializerTest {


    @Test
    public void notEmpty() throws Exception {



        for(int attempt = 0; attempt<10; attempt++)
        {
            FishState state = generateSimple4x4Map();
            NauticalMap map = state.getMap();
            MersenneTwisterFast random = new MersenneTwisterFast();
            RockyRectanglesInitializer initializer = new RockyRectanglesInitializer(3,3,3,3,1,random);

            initializer.accept(map);
            int count = 0;
            for(SeaTile tile : map.getAllSeaTilesAsList())
                if(tile.getHabitat().getHardPercentage() > .99d)
                    count++;
            Assert.assertTrue(count>0);
            Assert.assertTrue(count<=9);
        }

    }
}
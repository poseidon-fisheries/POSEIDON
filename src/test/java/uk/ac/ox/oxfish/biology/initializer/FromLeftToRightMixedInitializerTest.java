package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 6/20/16.
 */
public class FromLeftToRightMixedInitializerTest {


    @Test
    public void moreOnTheLeft() throws Exception
    {

        SeaTile left = new SeaTile(0, 0, -1, new TileHabitat(0d));
        SeaTile middle = new SeaTile(50,0,-1, new TileHabitat(0d));
        SeaTile right = new SeaTile(100,0,-1, new TileHabitat(0d));
        FromLeftToRightMixedInitializer initializer = new FromLeftToRightMixedInitializer(5000, 2);
        final Species species1 = new Species("Specie0");
        final Species species2 = new Species("Specie1");
        GlobalBiology biology = new GlobalBiology(species1,species2);

        left.setBiology(
                initializer.generateLocal(biology, left, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );

        middle.setBiology(
                initializer.generateLocal(biology, middle, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );

        right.setBiology(
                initializer.generateLocal(biology, right, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );


        assertTrue(left.getBiomass(species1)> middle.getBiomass(species1));
        assertTrue(middle.getBiomass(species1)> right.getBiomass(species1));
        assertTrue(left.getBiomass(species2)> middle.getBiomass(species2));
        assertTrue(middle.getBiomass(species2)> right.getBiomass(species2));
        assertEquals(left.getBiomass(species1)/left.getBiomass(species2),.5,.001);
        assertEquals(middle.getBiomass(species1)/middle.getBiomass(species2),.5,.001);
    }
}


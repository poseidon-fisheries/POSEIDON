package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertTrue;


public class FromLeftToRightInitializerTest {


    //more on the left


    @Test
    public void moreOnTheLeft() throws Exception
    {

        SeaTile left = new SeaTile(0,0,-1, new TileHabitat(0d));
        SeaTile middle = new SeaTile(50,0,-1, new TileHabitat(0d));
        SeaTile right = new SeaTile(100,0,-1, new TileHabitat(0d));
        FromLeftToRightInitializer initializer = new FromLeftToRightInitializer(5000,1);
        final Species species = new Species("Specie0");
        GlobalBiology biology = new GlobalBiology(species);

        left.setBiology(
                initializer.generateLocal(biology, left, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );

        middle.setBiology(
                initializer.generateLocal(biology, middle, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );

        right.setBiology(
                initializer.generateLocal(biology, right, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );


        assertTrue(left.getBiomass(species)> middle.getBiomass(species));
        assertTrue(middle.getBiomass(species)> right.getBiomass(species));
    }
}
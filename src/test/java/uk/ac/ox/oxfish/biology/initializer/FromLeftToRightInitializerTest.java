package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
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
        final Specie specie = new Specie("Specie0");
        GlobalBiology biology = new GlobalBiology(specie);

        left.setBiology(
                initializer.generate(biology,left,new MersenneTwisterFast(System.currentTimeMillis()),100,100)
        );

        middle.setBiology(
                initializer.generate(biology, middle, new MersenneTwisterFast(System.currentTimeMillis()),100,100)
        );

        right.setBiology(
                initializer.generate(biology, right, new MersenneTwisterFast(System.currentTimeMillis()), 100, 100)
        );


        assertTrue(left.getBiomass(specie)> middle.getBiomass(specie));
        assertTrue(middle.getBiomass(specie)> right.getBiomass(specie));
    }
}
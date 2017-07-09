package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SingleSpeciesAbundanceInitializerTest {


    @Test
    public void readsCorrectly() throws Exception {


        //create a 4x4 map of the world.
        FishState model = MovingTest.generateSimple4x4Map();
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());

        Path testInput = Paths.get("inputs","tests","abundance","fake");
        Log.info("I pass the directory " + testInput + " to the single species initializer. That directory contains a fake simple species of which I know all the characteristic" +
                         "and I make sure the initializer instantiates correctly.");

        //create an initializer (scales to double the number from file)
        SingleSpeciesAbundanceInitializer initializer = new SingleSpeciesAbundanceInitializer(testInput,"fake",2.0,model);
        //create biology object
        GlobalBiology biology = initializer.generateGlobal(new MersenneTwisterFast(), mock(FishState.class));
        //check that name and meristics are correct
        assertEquals(1,biology.getSpecies().size());
        Species fakeSpecies = biology.getSpecie(0);
        assertEquals("fake", fakeSpecies.getName());
        assertEquals(3, fakeSpecies.getMaxAge());


        //put biology in there
        NauticalMap map = model.getMap();
        for(SeaTile element : map.getAllSeaTilesAsList())
        {
            element.setBiology(initializer.generateLocal(biology,
                    element, new MersenneTwisterFast(),4, 4,
                                                         mock(NauticalMap.class)
            )); //put new biology in
        }
        //by default the abundance initializer splits total count uniformly
        initializer.processMap(biology, map, new MersenneTwisterFast(), model);
        assertEquals(200,map.getSeaTile(0,0).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(1,1).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(200,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(0,0).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(1,1).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(250,map.getSeaTile(2,3).getNumberOfMaleFishPerAge(fakeSpecies)[0]);
        assertEquals(325,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);
        assertEquals(325,map.getSeaTile(2,3).getNumberOfFemaleFishPerAge(fakeSpecies)[1]);

    }
}
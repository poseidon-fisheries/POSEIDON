package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 10/25/16.
 */
public class ProtectedAreaChromosomeFactoryTest {


    @Test
    public void geneCorrectly() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapMaker = new SimpleMapInitializerFactory();
        scenario.setMapInitializer(mapMaker);
        mapMaker.setHeight(new FixedDoubleParameter(4));
        mapMaker.setWidth(new FixedDoubleParameter(4));
        mapMaker.setCoastalRoughness(new FixedDoubleParameter(0));
        ProtectedAreaChromosomeFactory factory = new ProtectedAreaChromosomeFactory();
        factory.setChromosome("0010010000000000");
        scenario.setRegulation(factory);
        scenario.setFishers(1);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        assertFalse(state.getMap().getSeaTile(0,0).isProtected());
        assertFalse(state.getMap().getSeaTile(1,0).isProtected());
        assertTrue(state.getMap().getSeaTile(2,0).isProtected());
        assertFalse(state.getMap().getSeaTile(3,0).isProtected());
        assertFalse(state.getMap().getSeaTile(0,1).isProtected());
        assertTrue(state.getMap().getSeaTile(1,1).isProtected());
    }
}
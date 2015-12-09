package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MultiTACStringFactoryTest {


    @Test
    public void createCorrectRule() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        MultiTACStringFactory regulation = new MultiTACStringFactory();
        scenario.setRegulation(regulation);
        scenario.setBiologyInitializer(new HalfBycatchFactory());

        regulation.setYearlyQuotaMaps("1:123");

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Fisher fisher = state.getFishers().get(0);
        MultiQuotaRegulation regs = (MultiQuotaRegulation) fisher.getRegulation();
        assertTrue(Double.isInfinite(regs.getQuotaRemaining(0)));
        assertEquals(regs.getQuotaRemaining(1),123d,.0001);




    }    @Test
    public void emptyRule() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        MultiTACStringFactory regulation = new MultiTACStringFactory();
        scenario.setRegulation(regulation);
        scenario.setBiologyInitializer(new HalfBycatchFactory());

        regulation.setYearlyQuotaMaps("0:1.0,1:1.0");

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        Fisher fisher = state.getFishers().get(0);
        MultiQuotaRegulation regs = (MultiQuotaRegulation) fisher.getRegulation();
        assertEquals(regs.getQuotaRemaining(0),1d,.0001);
        assertEquals(regs.getQuotaRemaining(1),1d,.0001);




    }
}
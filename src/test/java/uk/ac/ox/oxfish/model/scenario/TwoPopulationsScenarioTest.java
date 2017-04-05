package uk.ac.ox.oxfish.model.scenario;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 4/3/17.
 */
public class TwoPopulationsScenarioTest {


    @Test
    public void nonseparateRegs() throws Exception {

        TwoPopulationsScenario scenario = new TwoPopulationsScenario();
        //quick
        scenario.setLargeFishers(1);
        scenario.setSmallFishers(1);
        scenario.setRegulationSmall(new AnarchyFactory());
        scenario.setRegulationLarge(new TACMonoFactory());

        scenario.setSeparateRegulations(false); //force everybody to use small boats regulations
        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        //they are all anarchy!
        for(Fisher fisher : state.getFishers())
            assertTrue(fisher.getRegulation() instanceof Anarchy);

    }


    @Test
    public void separateRegs() throws Exception {

        TwoPopulationsScenario scenario = new TwoPopulationsScenario();
        //quick
        scenario.setLargeFishers(1);
        scenario.setSmallFishers(1);
        scenario.setRegulationSmall(new AnarchyFactory());
        scenario.setRegulationLarge(new TACMonoFactory());

        scenario.setSeparateRegulations(true); //force everybody to use small boats regulations
        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        //they are all anarchy!
        int nonAnarchy = 0;
        for(Fisher fisher : state.getFishers()) {
            if (fisher.getTags().contains("small"))
                assertTrue(fisher.getRegulation() instanceof Anarchy);
            else {
                nonAnarchy++;
                assertFalse(fisher.getRegulation() instanceof Anarchy);
            }
        }
        //make sure you are counting them!
        assertEquals(nonAnarchy,1);

    }
}
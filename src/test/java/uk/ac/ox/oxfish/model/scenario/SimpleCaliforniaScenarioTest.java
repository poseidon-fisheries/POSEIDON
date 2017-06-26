package uk.ac.ox.oxfish.model.scenario;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 6/26/17.
 */
public class SimpleCaliforniaScenarioTest {



    //if there is no fishing from the model, the biomass ought to go to about the right level


    @Test
    public void replicateTestSablefishTS() throws Exception {



        SimpleCaliforniaScenario scenario = new SimpleCaliforniaScenario();
        scenario.setLargeFishers(0);
        scenario.setSmallFishers(0);

        //8000t
        scenario.setExogenousSablefishCatches(8000000);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        while (state.getYear()<10) {

            state.schedule.step(state);
            if(state.getDayOfTheYear()==1)
                System.out.println(state.getTotalBiomass(state.getSpecies().get(0))/1000);
        }
        state.schedule.step(state);
        double finalBiomass = state.getLatestYearlyObservation("Biomass Sablefish");
        System.out.println(finalBiomass/1000);
        Assert.assertEquals(finalBiomass/1000,364137.4,1);
    }
}
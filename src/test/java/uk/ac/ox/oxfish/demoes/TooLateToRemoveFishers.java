package uk.ac.ox.oxfish.demoes;

import com.esotericsoftware.minlog.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 12/15/15.
 */
public class TooLateToRemoveFishers
{


    @Test
    public void tooLateToRemoveFishers() throws Exception {
        FishState state = new FishState(System.currentTimeMillis());

        Log.info("This demo replicates the dynamics in: http://carrknight.github.io/assets/oxfish/entryexit.html");
        Log.info("You add a bunch of fishers, and after removing them the biomass is still screwed");
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(50);
        SimpleMapInitializerFactory simpleMapInitializerFactory = new SimpleMapInitializerFactory();
        simpleMapInitializerFactory.setCoastalRoughness(new FixedDoubleParameter(0));
        scenario.setMapInitializer(simpleMapInitializerFactory);

        //run the model for a full 3 years before progressing
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < 3)
            state.schedule.step(state);

        //now keep running for 10 years adding 3 fishers every month
        while (state.getYear() < 13) {
            if (state.getDayOfTheYear() % 30 == 0) {
                state.createFisher();
                state.createFisher();
                state.createFisher();
                //   state.createFisher();
                //   state.createFisher();
            }
            state.schedule.step(state);
        }

        //for the next 10 years remove the fishers
        while (state.getYear() < 23) {
            if (state.getDayOfTheYear() % 30 == 0) {
                state.killRandomFisher();
                state.killRandomFisher();
                state.killRandomFisher();
                //    state.killRandomFisher();
                //    state.killRandomFisher();
            }
            state.schedule.step(state);
        }

        Log.info("I am assuming that the biomass is below 10% the virgin level of 10million");
        Double biomass = state.getLatestYearlyObservation("Biomass Species 0");
        Log.info("The actual remaining biomass is: " + biomass);
        assertTrue(biomass < 1000000);
    }

}

package uk.ac.ox.oxfish.experiments;

import sim.util.Bag;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * A simple main that requires a yaml file from console to run the simulation 1000 steps before quitting. Simple test
 * for more complicated efforts
 * Created by carrknight on 7/13/15.
 */
public class GUILessMain
{

    public static void main(String[] args) throws FileNotFoundException {

        String absolutePath = FishStateUtilities.getAbsolutePath(args[0]);
        //read as scenario
        FishYAML yaml = new FishYAML();
        Scenario scenario  =  yaml.loadAs(new FileReader(new File(absolutePath)), Scenario.class);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        long time = System.currentTimeMillis();
        while(state.getYear() <20)
        {
            state.schedule.step(state);
          //  System.out.println(state.timeString() + " -- " + scenario.getClass().getSimpleName());
        }
        System.out.println("speed: " + (System.currentTimeMillis()-time)/1000d);


    }

}

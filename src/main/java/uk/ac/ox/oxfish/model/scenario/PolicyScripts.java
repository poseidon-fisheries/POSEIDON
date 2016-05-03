package uk.ac.ox.oxfish.model.scenario;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Steps once a year and checks if anything is applicable
 * Created by carrknight on 5/3/16.
 */
public class PolicyScripts implements Steppable,Startable{

    final private HashMap<Integer,PolicyScript> scripts;


    public PolicyScripts(HashMap<Integer, PolicyScript> scripts) {
        this.scripts = scripts;
    }


    public static PolicyScripts fromYaml(FishYAML yaml, String toRead)
    {
        LinkedHashMap<Integer,LinkedHashMap> temp = (LinkedHashMap<Integer, LinkedHashMap>) yaml.load(toRead);
        HashMap<Integer,PolicyScript> read = new HashMap<>();
        for(Map.Entry<Integer,LinkedHashMap> entry : temp.entrySet())
            //turn value into string and read it back forcing it as a policy script
            read.put(entry.getKey(),yaml.loadAs(yaml.dump(entry.getValue()),PolicyScript.class));
        return new PolicyScripts(read);

    }

    @Override
    public void step(SimState simState)
    {
        Preconditions.checkNotNull(receipt);
        FishState model = ((FishState) simState);
        for(Map.Entry<Integer,PolicyScript> policy : scripts.entrySet())
            if (model.getYear()+1 == policy.getKey())
                policy.getValue().apply(model);
    }

    private Stoppable receipt ;

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {
        receipt = model.scheduleEveryYear(this, StepOrder.DAWN);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
        receipt = null;
    }


    /**
     * Getter for property 'scripts'.
     *
     * @return Value for property 'scripts'.
     */
    public HashMap<Integer, PolicyScript> getScripts() {
        return scripts;
    }
}

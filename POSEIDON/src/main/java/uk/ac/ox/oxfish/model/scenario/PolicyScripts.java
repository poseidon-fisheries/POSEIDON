/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * Steps once a year and checks if anything is applicable
 * Created by carrknight on 5/3/16.
 */
public class PolicyScripts implements Steppable, Startable {

    private HashMap<Integer, PolicyScript> scripts;
    private Stoppable receipt;


    public PolicyScripts(HashMap<Integer, PolicyScript> scripts) {
        this.scripts = scripts;
    }


    /*
    public static PolicyScripts fromYaml(MappingNode node)
    {

        PolicyScripts scripts = new PolicyScripts();


        assert node.getValue().size()==1;
        assert node.getValue().get(0).getKeyNode() instanceof ScalarNode;
        assert ((ScalarNode) node.getValue().get(0).getKeyNode()).getValue().equalsIgnoreCase("scripts");

        List<NodeTuple> scriptList = ((MappingNode) node.getValue().get(0).getValueNode()).getValue();
        FishYAML yamler = new FishYAML();
        for(NodeTuple script : scriptList)
        {
            Integer key = Integer.parseInt(((ScalarNode) script.getKeyNode()).getValue());
            Node value = script.getValueNode();
            value.setType(PolicyScript.class);

            String dumped = yamler.dump(value);
            PolicyScript toAdd = yamler.loadAs(dumped,PolicyScript.class);
            scripts.getScripts().put(key,toAdd);
        }

        return scripts;

    }
    */

    public PolicyScripts() {
        this.scripts = new HashMap<>();
    }

    @Override
    public void step(SimState simState) {
        Preconditions.checkNotNull(receipt);
        FishState model = ((FishState) simState);
        for (Map.Entry<Integer, PolicyScript> policy : scripts.entrySet())
            if (model.getYear() + 1 == policy.getKey())
                policy.getValue().apply(model);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
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

    public void setScripts(HashMap<Integer, PolicyScript> scripts) {
        this.scripts = scripts;
    }
}

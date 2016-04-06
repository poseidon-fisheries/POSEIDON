package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;


/**
 * All the objects that I expect were created by a Scenario
 */
public class ScenarioEssentials {

    private final GlobalBiology biology;

    private final NauticalMap map;



    public ScenarioEssentials(
            GlobalBiology biology, NauticalMap map) {
        this.biology = biology;
        this.map = map;
    }

    public GlobalBiology getBiology() {
        return biology;
    }

    public NauticalMap getMap() {
        return map;
    }


}

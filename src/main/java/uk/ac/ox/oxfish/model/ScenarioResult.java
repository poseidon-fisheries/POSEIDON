package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.market.Markets;

import java.util.List;

/**
 * All the objects that I expect were created by a Scenario
 */
public class ScenarioResult {

    private final GlobalBiology biology;

    private final NauticalMap map;

    private final List<Fisher> agents;


    public ScenarioResult(
            GlobalBiology biology, NauticalMap map, List<Fisher> agents, Markets markets) {
        this.biology = biology;
        this.map = map;
        this.agents = agents;
    }

    public GlobalBiology getBiology() {
        return biology;
    }

    public NauticalMap getMap() {
        return map;
    }

    public List<Fisher> getAgents() {
        return agents;
    }

}

package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.market.Markets;

import java.util.List;

/**
 * All the objects that I expect were created by a Scenario
 */
public class ScenarioEssentials {

    private final GlobalBiology biology;

    private final NauticalMap map;

    private final Markets markets;


    public ScenarioEssentials(
            GlobalBiology biology, NauticalMap map, Markets markets) {
        this.biology = biology;
        this.map = map;
        this.markets = markets;
    }

    public GlobalBiology getBiology() {
        return biology;
    }

    public NauticalMap getMap() {
        return map;
    }


    public Markets getMarkets() {
        return markets;
    }
}

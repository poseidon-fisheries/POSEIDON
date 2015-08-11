package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.market.MarketMap;


/**
 * All the objects that I expect were created by a Scenario
 */
public class ScenarioEssentials {

    private final GlobalBiology biology;

    private final NauticalMap map;

    private final MarketMap marketMap;


    public ScenarioEssentials(
            GlobalBiology biology, NauticalMap map, MarketMap marketMap) {
        this.biology = biology;
        this.map = map;
        this.marketMap = marketMap;
    }

    public GlobalBiology getBiology() {
        return biology;
    }

    public NauticalMap getMap() {
        return map;
    }


    public MarketMap getMarketMap() {
        return marketMap;
    }
}

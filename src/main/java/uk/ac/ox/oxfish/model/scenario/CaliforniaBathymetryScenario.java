package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.Markets;

import java.util.LinkedList;

/**
 * Created by carrknight on 5/7/15.
 */
public class CaliforniaBathymetryScenario implements Scenario {
    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioResult start(FishState model) {
        NauticalMap map = NauticalMap.initializeWithDefaultValues();


        final GlobalBiology biology = new GlobalBiology(new Specie("test test"));
        return new ScenarioResult(biology,map,new LinkedList<>(),new Markets(biology));
    }
}

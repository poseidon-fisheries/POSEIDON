package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the bathymetry file of california and for now not much else.
 * Created by carrknight on 5/7/15.
 */
public class CaliforniaBathymetryScenario implements Scenario {

    private int numberOfSpecies;


    public CaliforniaBathymetryScenario(int numberOfSpecies) {
        this.numberOfSpecies = numberOfSpecies;
    }

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {
        NauticalMap map = NauticalMapFactory.fromBathymetryAndShapeFiles(
                new StraightLinePathfinder(), Paths.get("inputs", "california", "california1000.asc").toString(),
                Paths.get("inputs", "california", "cssr_mpa", "reprojected","mpa_central.shp").toString(),
                Paths.get("inputs", "california", "ncssr_mpa", "reprojected","mpa_north.shp").toString()
        );

        final GlobalBiology biology = GlobalBiology.genericListOfSpecies(numberOfSpecies);

        final HashMap<Specie, String> biomassFiles = new HashMap<>();
        biomassFiles.put(biology.getSpecie(0), FishStateUtilities.getAbsolutePath(
                Paths.get("inputs", "california", "soletest.asc").toString()));
        final Map<SeaTile, double[]> speciesForEachCellFromData = NauticalMapFactory.getSpeciesForEachCellFromData(
                biomassFiles, map);




        for(Map.Entry<SeaTile,double[]> tile : speciesForEachCellFromData.entrySet())
        {
            double totalBiomass = 0;
            for(int i=0; i<tile.getValue().length; i++)
                totalBiomass += tile.getValue()[i];
            assert  totalBiomass >=0;
            if(totalBiomass > 0)
                tile.getKey().setBiology(new ConstantLocalBiology(totalBiomass*2000));
            else
                tile.getKey().setBiology(new EmptyLocalBiology());
        }
        return new ScenarioEssentials(biology,map,new MarketMap(biology));
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {


        return new ScenarioPopulation(new ArrayList<>(),new SocialNetwork(new EmptyNetworkBuilder()));
    }


    public int getNumberOfSpecies() {
        return numberOfSpecies;
    }

    public void setNumberOfSpecies(int numberOfSpecies) {
        this.numberOfSpecies = numberOfSpecies;
    }
}




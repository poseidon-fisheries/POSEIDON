package uk.ac.ox.oxfish.model.scenario;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseBoundedMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.PortReader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The scenario to run Osmose WFS.
 * Created by carrknight on 11/17/16.
 */
public class OsmoseWFSScenario implements Scenario{


    public final static Path mainDirectory = Paths.get("temp_wfs");

    private final OsmoseBiologyFactory biologyInitializer = new OsmoseBiologyFactory();
    {
        biologyInitializer.setIndexOfSpeciesToBeManagedByThisModel("2");
        biologyInitializer.setOsmoseConfigurationFile(mainDirectory.resolve("wfs").resolve("osm_all-parameters.csv").toAbsolutePath().toString());
        biologyInitializer.setPreInitializedConfiguration(false);
        //biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(114*12);
        biologyInitializer.setNumberOfOsmoseStepsToPulseBeforeSimulationStart(10);
    }

    private final OsmoseBoundedMapInitializerFactory mapInitializer = new OsmoseBoundedMapInitializerFactory();
    {
        mapInitializer.setLowRightEasting(584600.702);

        mapInitializer.setLowRightNorthing(2791787.489);

        mapInitializer.setUpLeftEasting(-73291.664);

        mapInitializer.setUpLeftNorthing(3445097.299);
    }


    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer = new ConstantWeatherFactory();

    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(FishState model) {
        try {

            BiologyInitializer biology = biologyInitializer.apply(model);
            WeatherInitializer weather = weatherInitializer.apply(model);


            //create global biology
            GlobalBiology global = biology.generateGlobal(model.getRandom(), model);


            MapInitializer mapMaker = mapInitializer.apply(model);
            NauticalMap map = mapMaker.makeMap(model.getRandom(), global, model);

            //set habitats
            HabitatInitializer habitat = habitatInitializer.apply(model);
            habitat.applyHabitats(map, model.getRandom(), model);


            //this next static method calls biology.initialize, weather.initialize and the like
            NauticalMapFactory.initializeMap(map, model.getRandom(), biology,
                                             weather,
                                             global, model);


            LinkedHashMap<Port,Integer> numberOfFishersPerPort =
                    PortReader.readFile(
                            mainDirectory.resolve("longline_ports.csv"),
                            map,
                            () -> {
                                MarketMap markets = new MarketMap(global);
                                for(Species species : global.getSpecies())
                                    markets.addMarket(species, new FixedPriceMarket(1));

                                return markets;
                            },
                            0.1234);


            for(Port port : numberOfFishersPerPort.keySet())
                map.addPort(port);


            return new ScenarioEssentials(global,map);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read file");
        }
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {
        return new ScenarioPopulation(
                new LinkedList<>(),
                new SocialNetwork(new EmptyNetworkBuilder()),
                null
        );
    }

    /**
     * Getter for property 'biologyInitializer'.
     *
     * @return Value for property 'biologyInitializer'.
     */
    public OsmoseBiologyFactory getBiologyInitializer() {
        return biologyInitializer;
    }

    /**
     * Getter for property 'mapInitializer'.
     *
     * @return Value for property 'mapInitializer'.
     */
    public OsmoseBoundedMapInitializerFactory getMapInitializer() {
        return mapInitializer;
    }
}

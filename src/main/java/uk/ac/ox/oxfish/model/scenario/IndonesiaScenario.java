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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.RememberedProfitsExtractor;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SNALSARutilities;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.plugins.TowAndAltitudePluginFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.ports.FromFilePortInitializer;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.gui.drawing.BoatPortrayalFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FixedMap;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.SimplePortAdaptation;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Very ugly repeat of prototype scenario because I need a proper refactor of port
 * initializers
 * Created by carrknight on 7/2/17.
 */
public class IndonesiaScenario implements Scenario {




    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
            new DiffusingLogisticFactory();

    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
            new ConstantWeatherFactory();


    private FromFileMapInitializerFactory mapInitializer =
            new FromFileMapInitializerFactory();





    private final FromFilePortInitializer portInitializer =
            new FromFilePortInitializer(Paths.get("inputs","indonesia","712713_ports.csv"));
    private boolean cheaters = false;

    /**
     * Getter for property 'filePath'.
     *
     * @return Value for property 'filePath'.
     */
    public Path getPortFilePath() {
        return portInitializer.getFilePath();
    }

    /**
     * Setter for property 'filePath'.
     *
     * @param filePath Value to set for property 'filePath'.
     */
    public void setPortFilePath(Path filePath) {
        portInitializer.setFilePath(filePath);
    }

    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);


    private List<FisherDefinition> fisherDefinitions = new LinkedList<>();
    {
        FisherDefinition fisherDefinition = new FisherDefinition();
        fisherDefinition.getInitialFishersPerPort().put("Brondong",10);
        fisherDefinition.getInitialFishersPerPort().put("Probolinggo",12);
        fisherDefinition.setTags("small,canoe");
        fisherDefinitions.add(fisherDefinition);


        FisherDefinition largeBoats = new FisherDefinition();
        largeBoats.getInitialFishersPerPort().put("Galesong",15);
        largeBoats.setHoldSize(new FixedDoubleParameter(1000d));
        largeBoats.setTags("large,boat");
        fisherDefinitions.add(largeBoats);


    }


    private NetworkBuilder networkBuilder =
            new EquidegreeBuilder();


    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();


    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();


    /**
     * if this is not NaN then it is used as the random seed to feed into the map-making function. This allows for randomness
     * in the biology/fishery
     */
    private Long mapMakerDedicatedRandomSeed =  null;


    private boolean portSwitching = false;



    final private HashMap<Port,String> portColorTags = new HashMap<>();


    private List<AlgorithmFactory<? extends AdditionalStartable>> plugins =
            new LinkedList<>();
    {
        TowAndAltitudePluginFactory tow = new TowAndAltitudePluginFactory();
        tow.setIdentifier("cluster1_");
        tow.setHistogrammerStartYear(4);
        tow.setTagSusbset("cluster1");
        plugins.add(
                tow
        );
        tow = new TowAndAltitudePluginFactory();
        tow.setIdentifier("cluster2_");
        tow.setHistogrammerStartYear(4);
        tow.setTagSusbset("cluster2");
        plugins.add(
                tow
        );
        tow = new TowAndAltitudePluginFactory();
        tow.setIdentifier("cluster3_");
        tow.setHistogrammerStartYear(4);
        tow.setTagSusbset("cluster3");
        plugins.add(
                tow
        );
    }

    public IndonesiaScenario() {
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

        MersenneTwisterFast originalRandom = model.random;

        MersenneTwisterFast mapMakerRandom = model.random;
        if(mapMakerDedicatedRandomSeed != null)
            mapMakerRandom = new MersenneTwisterFast(mapMakerDedicatedRandomSeed);
        //force the mapMakerRandom as the new random until the start is completed.
        model.random = mapMakerRandom;




        BiologyInitializer biology = biologyInitializer.apply(model);
        WeatherInitializer weather = weatherInitializer.apply(model);

        //create global biology
        GlobalBiology global = biology.generateGlobal(mapMakerRandom, model);


        MapInitializer mapMaker = mapInitializer.apply(model);
        NauticalMap map = mapMaker.makeMap(mapMakerRandom, global, model);

        //set habitats
        HabitatInitializer habitat = habitatInitializer.apply(model);
        habitat.applyHabitats(map, mapMakerRandom, model);


        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(map, mapMakerRandom, biology,
                                         weather,
                                         global, model);


        List<Port> ports = portInitializer.buildPorts(map,
                                                      mapMakerRandom,
                                                      new Function<SeaTile, MarketMap>() {
                                                          @Override
                                                          public MarketMap apply(SeaTile seaTile) {
                                                              //create fixed price market
                                                              MarketMap marketMap = new MarketMap(global);
                                                              //set market for each species
                                                              for (Species species : global.getSpecies())
                                                                  marketMap.addMarket(species, market.apply(model));
                                                              return marketMap;
                                                          }
                                                      },
                                                      model,
                                                      new FixedGasPrice(gasPricePerLiter.apply(mapMakerRandom))
        );
        Iterator<String> colorIterator = BoatPortrayalFactory.BOAT_COLORS.keySet().iterator();
        for(Port port : ports) {
            portColorTags.put(port,
                              colorIterator.next());
        }

        //substitute back the original randomizer
        model.random = originalRandom;




        return new ScenarioEssentials(global,map);
    }


    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(FishState model) {

        final NauticalMap map = model.getMap();


        //no friends from separate ports
        networkBuilder.addPredicate(new NetworkPredicate() {
            @Override
            public boolean test(Fisher from, Fisher to) {
                return from.getHomePort().equals(to.getHomePort());
            }
        });


        Port[] ports =map.getPorts().toArray(new Port[map.getPorts().size()]);

        List<Fisher> fishers = new LinkedList<>();
        //arbitrary fisher factory
        FisherFactory lastFactory = null;
        int definitionIndex = 0;
        for (FisherDefinition fisherDefinition : fisherDefinitions) {
            Pair<FisherFactory, List<Fisher>> generated = fisherDefinition.instantiateFishers(
                    model,
                    map.getPorts(),
                    definitionIndex * 10000,
                    new Consumer<Fisher>() {
                        @Override
                        public void accept(Fisher fisher) {
                            fisher.setCheater(cheaters);
                            //todo move this somewhere else
                            fisher.addFeatureExtractor(
                                    SNALSARutilities.PROFIT_FEATURE,
                                    new RememberedProfitsExtractor(true)
                            );
                            fisher.addFeatureExtractor(
                                    FeatureExtractor.AVERAGE_PROFIT_FEATURE,
                                    new FeatureExtractor<SeaTile>() {
                                        @Override
                                        public HashMap<SeaTile, Double> extractFeature(
                                                Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {
                                            double averageProfits = model.getLatestDailyObservation(
                                                    FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                                            return new FixedMap<>(averageProfits,
                                                                  toRepresent);
                                        }
                                    }
                            );
                        }
                    },
                    new Consumer<Fisher>() {
                        @Override
                        public void accept(Fisher fisher) {
                            fisher.getTags().add(
                                    portColorTags.get(fisher.getHomePort())
                            );
                        }
                    },
                    new Consumer<Fisher>() {
                        @Override
                        public void accept(Fisher fisher) {
                            if (portSwitching)
                                fisher.addYearlyAdaptation(new SimplePortAdaptation());

                        }
                    });


            lastFactory = generated.getFirst();
            fishers.addAll(generated.getSecond());
            definitionIndex++;
        }


        //start additional elements
        for (AlgorithmFactory<? extends AdditionalStartable> additionalElement : plugins) {
            model.registerStartable(
                    additionalElement.apply(model)
            );

        }

        addTagLandingTimeSeries(model);
        HashMap<String, FisherFactory> factory = new HashMap<>();
        factory.put(FishState.DEFAULT_POPULATION_NAME,
                    lastFactory);
        if(fishers.size() <=1)
            return new ScenarioPopulation(fishers, new SocialNetwork(new EmptyNetworkBuilder()), factory );
        else {
            return new ScenarioPopulation(fishers, new SocialNetwork(networkBuilder), factory);
        }
    }


    /**
     * extract landing time series by classic tags
     */
    private void  addTagLandingTimeSeries(FishState state){

        for(String tag : new String[]{"small","big"})
        {
            //landings
            for(Species species : state.getBiology().getSpecies())
                state.getYearlyDataSet().registerGatherer(
                        species.getName() + " " + AbstractMarket.LANDINGS_COLUMN_NAME + " of " + tag,
                        fishState -> fishState.getFishers().stream().
                                filter(fisher -> fisher.getTags().contains(tag)).
                                mapToDouble(value -> value.getLatestYearlyObservation(
                                        species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)).sum(), Double.NaN);

            state.getYearlyDataSet().registerGatherer("Total Income of " +tag,
                                                      fishState ->
                                                              fishState.getFishers().stream().
                                                                      filter(fisher -> fisher.getTags().contains(tag)).
                                                                      mapToDouble(value -> value.getLatestYearlyObservation(
                                                                              FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).sum(), Double.NaN);

            state.getYearlyDataSet().registerGatherer("Average Distance From Port of " +tag,
                                                      fishState ->
                                                              fishState.getFishers().stream().
                                                                      filter(fisher -> fisher.getTags().contains(tag)).
                                                                      mapToDouble(value -> value.getLatestYearlyObservation(
                                                                              FisherYearlyTimeSeries.FISHING_DISTANCE)).
                                                                      filter(Double::isFinite).average().
                                                                      orElse(Double.NaN), Double.NaN);

            state.getYearlyDataSet().registerGatherer("Average Number of Trips of " +tag,
                                                      fishState ->
                                                              fishState.getFishers().stream().
                                                                      filter(fisher -> fisher.getTags().contains(tag)).
                                                                      mapToDouble(value -> value.getLatestYearlyObservation(
                                                                              FisherYearlyTimeSeries.TRIPS)).average().
                                                                      orElse(Double.NaN), 0 );



            state.getYearlyDataSet().registerGatherer("Average Number of Hours Out of " +tag,
                                                      fishState ->
                                                              fishState.getFishers().stream().
                                                                      filter(fisher -> fisher.getTags().contains(tag)).
                                                                      mapToDouble(value -> value.getLatestYearlyObservation(
                                                                              FisherYearlyTimeSeries.HOURS_OUT)).average().
                                                                      orElse(Double.NaN), 0 );
            //do not just average the trip duration per fisher because otherwise you don't weigh them according to how many trips they actually did
            state.getYearlyDataSet().registerGatherer("Average Trip Duration of "+tag, new Gatherer<FishState>() {
                @Override
                public Double apply(FishState observed) {

                    List<Fisher> taggedFishers = observed.getFishers().stream().
                            filter(new Predicate<Fisher>() {
                                @Override
                                public boolean test(Fisher fisher) {
                                    return fisher.getTags().contains(tag);
                                }
                            }).collect(Collectors.toList());

                    //skip boats that made no trips
                    double hoursOut = taggedFishers.stream().mapToDouble(
                            value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT)).
                            filter(value -> Double.isFinite(value)).sum();
                    double trips = taggedFishers.stream().mapToDouble(
                            value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS)).
                            filter(new DoublePredicate() { //skip boats that made no trips
                        @Override
                        public boolean test(double value) {
                            return Double.isFinite(value);
                        }
                    }).sum();

                    return trips > 0 ? hoursOut/trips : 0d;
                }
            }, 0d);

            state.getYearlyDataSet().registerGatherer("Number Of Fishers of " +tag,
                                                      fishState ->
                                                              (double)fishState.getFishers().stream().
                                                                      filter(fisher ->
                                                                                     fisher.getTags().contains(tag)).count(),
                                                      Double.NaN);

            state.getYearlyDataSet().registerGatherer("Number Of Active Fishers of " +tag,
                                                      fishState ->
                                                              (double)fishState.getFishers().stream().
                                                                      filter(new Predicate<Fisher>() {
                                                                          @Override
                                                                          public boolean test(Fisher fisher) {
                                                                              return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS)>0;
                                                                          }
                                                                      }).
                                                                      filter(fisher ->
                                                                                     fisher.getTags().contains(tag)).count(),
                                                      Double.NaN);





            state.getYearlyDataSet().registerGatherer("Average Cash-Flow of " +tag,
                                                      new Gatherer<FishState>() {
                                                          @Override
                                                          public Double apply(FishState observed) {
                                                              List<Fisher> fishers = observed.getFishers().stream().
                                                                      filter(new Predicate<Fisher>() {
                                                                          @Override
                                                                          public boolean test(Fisher fisher) {
                                                                              return fisher.getTags().contains(tag);
                                                                          }
                                                                      }).collect(Collectors.toList());
                                                              return fishers.stream().
                                                                      mapToDouble(
                                                                              new ToDoubleFunction<Fisher>() {
                                                                                  @Override
                                                                                  public double applyAsDouble(Fisher value) {
                                                                                      return value.getLatestYearlyObservation(
                                                                                              FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                                                  }
                                                                              }).sum() /
                                                                      fishers.size();
                                                          }
                                                      }, Double.NaN);



        }


    }




    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(
            AlgorithmFactory<? extends BiologyInitializer> biologyInitializer) {
        this.biologyInitializer = biologyInitializer;
    }


    public NetworkBuilder getNetworkBuilder() {
        return networkBuilder;
    }

    public void setNetworkBuilder(
            NetworkBuilder networkBuilder) {
        this.networkBuilder = networkBuilder;
    }



    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }


    public AlgorithmFactory<? extends Market> getMarket() {
        return market;
    }

    public void setMarket(AlgorithmFactory<? extends Market> market) {
        this.market = market;
    }


    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    public void setWeatherInitializer(
            AlgorithmFactory<? extends WeatherInitializer> weatherInitializer) {
        this.weatherInitializer = weatherInitializer;
    }


    public Long getMapMakerDedicatedRandomSeed() {
        return mapMakerDedicatedRandomSeed;
    }

    public void setMapMakerDedicatedRandomSeed(Long mapMakerDedicatedRandomSeed) {
        this.mapMakerDedicatedRandomSeed = mapMakerDedicatedRandomSeed;
    }

    public AlgorithmFactory<? extends HabitatInitializer> getHabitatInitializer() {
        return habitatInitializer;
    }

    public void setHabitatInitializer(
            AlgorithmFactory<? extends HabitatInitializer> habitatInitializer) {
        this.habitatInitializer = habitatInitializer;
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializer() {
        return mapInitializer;
    }



    /**
     * Getter for property 'cheaters'.
     *
     * @return Value for property 'cheaters'.
     */
    public boolean isCheaters() {
        return cheaters;
    }

    /**
     * Setter for property 'cheaters'.
     *
     * @param cheaters Value to set for property 'cheaters'.
     */
    public void setCheaters(boolean cheaters) {
        this.cheaters = cheaters;
    }


    public boolean isPortSwitching() {
        return portSwitching;
    }

    public void setPortSwitching(boolean portSwitching) {
        this.portSwitching = portSwitching;
    }


    /**
     * Setter for property 'mapInitializer'.
     *
     * @param mapInitializer Value to set for property 'mapInitializer'.
     */
    public void setMapInitializer(FromFileMapInitializerFactory mapInitializer) {
        this.mapInitializer = mapInitializer;
    }

    /**
     * Getter for property 'fisherDefinitions'.
     *
     * @return Value for property 'fisherDefinitions'.
     */
    public List<FisherDefinition> getFisherDefinitions() {
        return fisherDefinitions;
    }

    /**
     * Setter for property 'fisherDefinitions'.
     *
     * @param fisherDefinitions Value to set for property 'fisherDefinitions'.
     */
    public void setFisherDefinitions(List<FisherDefinition> fisherDefinitions) {
        this.fisherDefinitions = fisherDefinitions;
    }


    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<AlgorithmFactory<? extends AdditionalStartable>> plugins) {
        this.plugins = plugins;
    }
}

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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortInitializer;
import uk.ac.ox.oxfish.geography.ports.RandomPortFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.event.SimpleExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.BoatColors;
import uk.ac.ox.oxfish.utility.FixedMap;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * this is the conceptual scenario, but using fisher definitions.
 * The advantage is that it's easy to add multiple populations and distribute them by port.
 * <p>
 * The problem is that so far the GUI doesn't really handle definitions so you need to
 * go and modify it manually
 */
public class FlexibleScenario implements Scenario {


    final private HashMap<Port, String> portColorTags = new HashMap<>();
    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
        new DiffusingLogisticFactory();
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
        new ConstantWeatherFactory();
    private AlgorithmFactory<? extends MapInitializer> mapInitializer =
        new SimpleMapInitializerFactory();
    private AlgorithmFactory<? extends PortInitializer> portInitializer =
        new RandomPortFactory();
    /**
     * can people of different ports still be "friends" (that is, exchange info?); set to false by default
     * because this will break imitation unless a good objective function is used.
     */
    private boolean allowFriendshipsAcrossPorts = false;
    private boolean cheaters = false;

    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);

    private List<FisherDefinition> fisherDefinitions = new LinkedList<>();
    private NetworkBuilder networkBuilder =
        new EquidegreeBuilder();
    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer = new AllSandyHabitatFactory();
    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();
    private List<AlgorithmFactory<? extends AdditionalStartable>> plugins =
        new LinkedList<>();
    private boolean portSwitching = false;
    private AlgorithmFactory<? extends ExogenousCatches> exogenousCatches = new SimpleExogenousCatchesFactory();
    /**
     * if this is not NaN then it is used as the random seed to feed into the map-making function. This allows for randomness
     * in the biology/fishery
     */
    private Long mapMakerDedicatedRandomSeed = null;
    /**
     * all tags for which we would like to have separate time series recording, separated by ",";
     * so for example "small,big"
     */
    private String tagsToTrackSeparately = "";

    {
        ((RandomPortFactory) portInitializer).setNumberOfPorts(new FixedDoubleParameter(2));
    }

    {
        final FisherDefinition fisherDefinition = new FisherDefinition();
        fisherDefinition.getInitialFishersPerPort().put("Port 0", 50);
        fisherDefinition.getInitialFishersPerPort().put("Port 1", 50);
        //fisherDefinition.setTags("small,canoe");
        fisherDefinitions.add(fisherDefinition);

    }

    public FlexibleScenario() {
    }

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    @Override
    public ScenarioEssentials start(final FishState model) {

        final MersenneTwisterFast originalRandom = model.random;

        MersenneTwisterFast mapMakerRandom = model.random;
        if (mapMakerDedicatedRandomSeed != null)
            mapMakerRandom = new MersenneTwisterFast(mapMakerDedicatedRandomSeed);
        //force the mapMakerRandom as the new random until the start is completed.
        model.random = mapMakerRandom;


        final BiologyInitializer biology = biologyInitializer.apply(model);
        final WeatherInitializer weather = weatherInitializer.apply(model);

        //create global biology
        final GlobalBiology global = biology.generateGlobal(mapMakerRandom, model);


        final MapInitializer mapMaker = mapInitializer.apply(model);
        final NauticalMap map = mapMaker.makeMap(mapMakerRandom, global, model);

        //set habitats
        final HabitatInitializer habitat = habitatInitializer.apply(model);
        habitat.applyHabitats(map, mapMakerRandom, model);


        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(map, mapMakerRandom, biology,
            weather,
            global, model
        );


        final List<Port> ports = portInitializer.apply(model).buildPorts(
            map,
            mapMakerRandom,
            seaTile -> {
                //create fixed price market
                final MarketMap marketMap = new MarketMap(global);
                //set market for each species
                for (final Species species : global.getSpecies())
                    marketMap.addMarket(species, market.apply(model));
                return marketMap;
            },
            model,
            new FixedGasPrice(gasPricePerLiter.applyAsDouble(mapMakerRandom))
        );
        final Iterator<String> colorIterator = BoatColors.BOAT_COLORS.keySet().iterator();
        for (final Port port : ports) {
            portColorTags.put(
                port,
                colorIterator.next()
            );
        }

        //substitute back the original randomizer
        model.random = originalRandom;


        return new ScenarioEssentials(global, map);
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(final FishState model) {

        final NauticalMap map = model.getMap();


        if (!allowFriendshipsAcrossPorts) {
            //no friends from separate ports
            networkBuilder.addPredicate((NetworkPredicate) (from, to) -> from.getHomePort().equals(to.getHomePort()));
        }

        final Port[] ports = map.getPorts().toArray(new Port[map.getPorts().size()]);

        final List<Fisher> fishers = new LinkedList<>();
        //arbitrary fisher factory
        final Map<String, FisherFactory> factory = new LinkedHashMap<>();
        int definitionIndex = 0;
        for (final FisherDefinition fisherDefinition : fisherDefinitions) {
            final int populationNumber = definitionIndex;
            final Map.Entry<FisherFactory, List<Fisher>> generated = fisherDefinition.instantiateFishers(
                model,
                map.getPorts(),
                definitionIndex * 10000,
                fisher -> {
                    fisher.setCheater(cheaters);
                    //todo move this somewhere else
                    fisher.addFeatureExtractor(
                        SNALSARutilities.PROFIT_FEATURE,
                        new RememberedProfitsExtractor(true)
                    );
                    fisher.addFeatureExtractor(
                        FeatureExtractor.AVERAGE_PROFIT_FEATURE,
                        (toRepresent, model1, fisher1) -> {
                            final double averageProfits = model1.getLatestDailyObservation(
                                FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                            return new FixedMap<>(
                                averageProfits,
                                toRepresent
                            );
                        }
                    );
                },
                fisher -> {
                    //add population tag
                    fisher.getTagsList().add("population" + populationNumber);


                    //do not over-write given color tags!
                    if (!BoatColors.hasColorTag(fisher))
                        fisher.getTagsList().add(
                            portColorTags.get(fisher.getHomePort())
                        );
                }
            );


            factory.put("population" + populationNumber, generated.getKey());
            fishers.addAll(generated.getValue());
            definitionIndex++;
            //add population number in
            if (fisherDefinitions.size() > 1) {
                if (tagsToTrackSeparately.equals("")) {
                    assert populationNumber == 0;
                    tagsToTrackSeparately = "population0";
                } else {
                    tagsToTrackSeparately = tagsToTrackSeparately + ",population" + populationNumber;

                }
            }
        }


        //start additional elements
        for (final AlgorithmFactory<? extends AdditionalStartable> additionalElement : plugins) {
            model.registerStartable(
                additionalElement.apply(model)
            );

        }

        addTagLandingTimeSeries(model);


        //add exogenous catches
        //start it!

        final ExogenousCatches catches = exogenousCatches.apply(model);
        model.registerStartable(catches);


        if (fishers.size() <= 1)
            return new ScenarioPopulation(fishers, new SocialNetwork(new EmptyNetworkBuilder()), factory);
        else {
            return new ScenarioPopulation(fishers, new SocialNetwork(networkBuilder), factory);
        }
    }

    /**
     * extract landing time series by classic tags
     */
    private void addTagLandingTimeSeries(final FishState state) {

        for (final String tag : tagsToTrackSeparately.split(",")) {
            //landings
            for (final Species species : state.getBiology().getSpecies())
                state.getYearlyDataSet().registerGatherer(
                    species.getName() + " " + AbstractMarket.LANDINGS_COLUMN_NAME + " of " + tag,
                    fishState -> fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)).sum(), Double.NaN
                );

            state.getYearlyDataSet().registerGatherer("Total Landings of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> {

                            double sum = 0;
                            for (final Species species : state.getBiology().getSpecies())
                                sum += value.getLatestYearlyObservation(
                                    species + " " + AbstractMarket.LANDINGS_COLUMN_NAME);
                            return sum;
                        }).sum(), Double.NaN
            );

            state.getYearlyDataSet().registerGatherer("Average Earnings of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.EARNINGS)).average().orElse(Double.NaN), Double.NaN
            );

            state.getYearlyDataSet().registerGatherer("Total Earnings of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.EARNINGS)).sum(), Double.NaN
            );

            state.getYearlyDataSet().registerGatherer("Average Variable Costs of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.VARIABLE_COSTS)).average().orElse(Double.NaN), Double.NaN
            );

            state.getYearlyDataSet().registerGatherer("Total Variable Costs of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.VARIABLE_COSTS)).sum(), Double.NaN
            );


            state.getYearlyDataSet().registerGatherer("Actual Average Variable Costs of " + tag,
                (Gatherer<FishState>) observed -> observed.getFishers().stream().
                    filter(fisher -> fisher.hasBeenActiveThisYear() &&
                        fisher.getTagsList().contains(tag)).
                    mapToDouble(value -> value.getLatestYearlyObservation(
                        FisherYearlyTimeSeries.VARIABLE_COSTS)).
                    filter(Double::isFinite).average().
                    orElse(Double.NaN), Double.NaN
            );

            state.getYearlyDataSet().registerGatherer("Average Distance From Port of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.FISHING_DISTANCE)).
                        filter(Double::isFinite).average().
                        orElse(Double.NaN), Double.NaN
            );

            state.getYearlyDataSet().registerGatherer("Actual Average Distance From Port of " + tag,
                (Gatherer<FishState>) observed -> observed.getFishers().stream().
                    filter(fisher -> fisher.hasBeenActiveThisYear() &&
                        fisher.getTagsList().contains(tag)).
                    mapToDouble(value -> value.getLatestYearlyObservation(
                        FisherYearlyTimeSeries.FISHING_DISTANCE)).
                    filter(Double::isFinite).average().
                    orElse(Double.NaN), Double.NaN
            );


            state.getYearlyDataSet().registerGatherer("Average Number of Trips of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.TRIPS)).average().
                        orElse(Double.NaN), 0
            );


            state.getYearlyDataSet().registerGatherer("Average Number of Hours Out of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.HOURS_OUT)).average().
                        orElse(Double.NaN), 0
            );


            state.getYearlyDataSet().registerGatherer(
                "Total Hours Out of " + tag,
                fishState ->
                    fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.HOURS_OUT)).sum(),
                0
            );

            //do not just average the trip duration per fisher because otherwise you don't weigh them according to how many trips they actually did
            state.getYearlyDataSet().registerGatherer("Average Trip Duration of " + tag,
                (Gatherer<FishState>) observed -> {

                    final List<Fisher> taggedFishers = observed.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).collect(Collectors.toList());

                    //skip boats that made no trips
                    final double hoursOut = taggedFishers.stream().mapToDouble(
                            value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT)).
                        filter(Double::isFinite).sum();
                    //skip boats that made no trips
                    final double trips = taggedFishers.stream().mapToDouble(
                            value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS)).
                        filter(Double::isFinite).sum();

                    return trips > 0 ? hoursOut / trips : 0d;
                }, 0d
            );

            state.getYearlyDataSet().registerGatherer(
                "Number Of Fishers of " + tag,
                fishState ->
                    (double) fishState.getFishers().stream().
                        filter(fisher ->
                            fisher.getTagsList().contains(tag)).count(),
                Double.NaN
            );

            state.getYearlyDataSet().registerGatherer(
                "Number Of Active Fishers of " + tag,
                fishState ->
                    (double) fishState.getFishers().stream().
                        filter(fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0).
                        filter(fisher ->
                            fisher.getTagsList().contains(tag)).count(),
                Double.NaN
            );


            state.getYearlyDataSet().registerGatherer("Average Cash-Flow of " + tag,
                (Gatherer<FishState>) observed -> {
                    final List<Fisher> fishers = observed.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains(tag)).collect(Collectors.toList());
                    return fishers.stream().
                        mapToDouble(
                            value -> value.getLatestYearlyObservation(
                                FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).sum() /
                        fishers.size();
                }, Double.NaN
            );


            state.getYearlyDataSet().registerGatherer("Actual Average Cash-Flow of " + tag,
                (Gatherer<FishState>) observed -> {
                    final List<Fisher> fishers = observed.getFishers().stream().
                        filter(fisher -> fisher.hasBeenActiveThisYear() &&
                            fisher.getTagsList().contains(tag)).collect(Collectors.toList());
                    return fishers.stream().
                        mapToDouble(
                            value -> value.getLatestYearlyObservation(
                                FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).sum() /
                        fishers.size();
                }, Double.NaN
            );

        }


    }

    /**
     * generate time serieses for populations of fishers
     *
     * @param state
     * @param number
     */
    private void addTimeSeriesToSubgroup(final FishState state, final int number) {

    }

    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(final AlgorithmFactory<? extends BiologyInitializer> biologyInitializer) {
        this.biologyInitializer = biologyInitializer;
    }

    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    public void setWeatherInitializer(final AlgorithmFactory<? extends WeatherInitializer> weatherInitializer) {
        this.weatherInitializer = weatherInitializer;
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(final AlgorithmFactory<? extends MapInitializer> mapInitializer) {
        this.mapInitializer = mapInitializer;
    }


    public AlgorithmFactory<? extends PortInitializer> getPortInitializer() {
        return portInitializer;
    }

    public void setPortInitializer(final AlgorithmFactory<? extends PortInitializer> portInitializer) {
        this.portInitializer = portInitializer;
    }

    public boolean isCheaters() {
        return cheaters;
    }

    public void setCheaters(final boolean cheaters) {
        this.cheaters = cheaters;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(final DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    public List<FisherDefinition> getFisherDefinitions() {
        return fisherDefinitions;
    }

    public void setFisherDefinitions(final List<FisherDefinition> fisherDefinitions) {
        this.fisherDefinitions = fisherDefinitions;
    }

    public NetworkBuilder getNetworkBuilder() {
        return networkBuilder;
    }

    public void setNetworkBuilder(final NetworkBuilder networkBuilder) {
        this.networkBuilder = networkBuilder;
    }

    public AlgorithmFactory<? extends HabitatInitializer> getHabitatInitializer() {
        return habitatInitializer;
    }

    public void setHabitatInitializer(final AlgorithmFactory<? extends HabitatInitializer> habitatInitializer) {
        this.habitatInitializer = habitatInitializer;
    }

    public AlgorithmFactory<? extends Market> getMarket() {
        return market;
    }

    public void setMarket(final AlgorithmFactory<? extends Market> market) {
        this.market = market;
    }


    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return plugins;
    }

    public void setPlugins(final List<AlgorithmFactory<? extends AdditionalStartable>> plugins) {
        this.plugins = plugins;
    }


    public boolean isPortSwitching() {
        return portSwitching;
    }

    public void setPortSwitching(final boolean portSwitching) {
        this.portSwitching = portSwitching;
    }

    public Long getMapMakerDedicatedRandomSeed() {
        return mapMakerDedicatedRandomSeed;
    }

    public void setMapMakerDedicatedRandomSeed(final Long mapMakerDedicatedRandomSeed) {
        this.mapMakerDedicatedRandomSeed = mapMakerDedicatedRandomSeed;
    }

    /**
     * Getter for property 'allowFriendshipsAcrossPorts'.
     *
     * @return Value for property 'allowFriendshipsAcrossPorts'.
     */
    public boolean isAllowFriendshipsAcrossPorts() {
        return allowFriendshipsAcrossPorts;
    }

    /**
     * Setter for property 'allowFriendshipsAcrossPorts'.
     *
     * @param allowFriendshipsAcrossPorts Value to set for property 'allowFriendshipsAcrossPorts'.
     */
    public void setAllowFriendshipsAcrossPorts(final boolean allowFriendshipsAcrossPorts) {
        this.allowFriendshipsAcrossPorts = allowFriendshipsAcrossPorts;
    }

    /**
     * Getter for property 'tagsToTrackSeparately'.
     *
     * @return Value for property 'tagsToTrackSeparately'.
     */
    public String getTagsToTrackSeparately() {
        return tagsToTrackSeparately;
    }

    /**
     * Setter for property 'tagsToTrackSeparately'.
     *
     * @param tagsToTrackSeparately Value to set for property 'tagsToTrackSeparately'.
     */
    public void setTagsToTrackSeparately(final String tagsToTrackSeparately) {
        this.tagsToTrackSeparately = tagsToTrackSeparately;
    }


    /**
     * Getter for property 'exogenousCatches'.
     *
     * @return Value for property 'exogenousCatches'.
     */
    public AlgorithmFactory<? extends ExogenousCatches> getExogenousCatches() {
        return exogenousCatches;
    }

    /**
     * Setter for property 'exogenousCatches'.
     *
     * @param exogenousCatches Value to set for property 'exogenousCatches'.
     */
    public void setExogenousCatches(
        final AlgorithmFactory<? extends ExogenousCatches> exogenousCatches
    ) {
        this.exogenousCatches = exogenousCatches;
    }
}

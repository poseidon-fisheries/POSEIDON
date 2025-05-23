/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.FixedGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.habitat.AllSandyHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortInitializer;
import uk.ac.ox.oxfish.geography.ports.RandomPortsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.market.gas.FixedGasPrice;
import uk.ac.ox.oxfish.model.network.*;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by carrknight on 7/25/16.
 */
public class TwoPopulationsScenario implements Scenario {

    private AlgorithmFactory<? extends BiologyInitializer> biologyInitializer =
        new DiffusingLogisticFactory();

    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
        new ConstantWeatherFactory();

    private AlgorithmFactory<? extends MapInitializer> mapInitializer =
        new SimpleMapInitializerFactory();

    /**
     * the number of fishers
     */
    private int smallFishers = 100;

    private int largeFishers = 10;

    /**
     * when this flag is true, agents use their memory to predict future catches and profits. It is
     * necessary for ITQs to work
     */
    private boolean usePredictors = false;

    /**
     * positions the port(s)
     */
    private AlgorithmFactory<? extends PortInitializer> ports = new RandomPortsFactory();

    /**
     * boat speed
     */
    private DoubleParameter smallSpeed = new FixedDoubleParameter(5);

    private DoubleParameter largeSpeed = new FixedDoubleParameter(5);

    /**
     * hold size
     */
    private DoubleParameter smallHoldSize = new FixedDoubleParameter(100);

    private DoubleParameter largeHoldSize = new FixedDoubleParameter(100);

    /**
     * gear used
     */
    private AlgorithmFactory<? extends Gear> gearSmall = new RandomCatchabilityTrawlFactory();

    private AlgorithmFactory<? extends Gear> gearLarge = new RandomCatchabilityTrawlFactory();

    private DoubleParameter enginePower = new NormalDoubleParameter(5000, 100);

    private DoubleParameter smallFuelTankSize = new FixedDoubleParameter(100000);
    private DoubleParameter largeFuelTankSize = new FixedDoubleParameter(100000);

    private DoubleParameter smallLitersPerKilometer = new FixedDoubleParameter(10);
    private DoubleParameter largeLitersPerKilometer = new FixedDoubleParameter(10);

    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategySmall =
        new FixedRestTimeDepartingFactory();

    private AlgorithmFactory<? extends DepartingStrategy> departingStrategyLarge =
        new FixedRestTimeDepartingFactory();

    /**
     * factory to produce destination strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategySmall =
        new PerTripImitativeDestinationFactory();

    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategyLarge =
        new PerTripImitativeDestinationFactory();

    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategySmall =
        new NoDiscardingFactory();

    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategyLarge =
        new NoDiscardingFactory();
    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategyLarge =
        new MaximumStepsFactory();

    private AlgorithmFactory<? extends FishingStrategy> fishingStrategySmall =
        new MaximumStepsFactory();

    private AlgorithmFactory<? extends GearStrategy> gearStrategy =
        new FixedGearStrategyFactory();

    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy =
        new IgnoreWeatherFactory();

    private boolean separateRegulations = true;

    private AlgorithmFactory<? extends Regulation> regulationSmall =
        new ProtectedAreasOnlyFactory();

    private AlgorithmFactory<? extends Regulation> regulationLarge =
        new ProtectedAreasOnlyFactory();

    private boolean allowTwoPopulationFriendships = false;

    private boolean allowFriendshipsBetweenPorts = false;

    private NetworkBuilder networkBuilder =
        new EquidegreeBuilder();

    private AlgorithmFactory<? extends HabitatInitializer> habitatInitializer =
        new AllSandyHabitatFactory();

    private AlgorithmFactory<? extends Market> market = new FixedPriceMarketFactory();

    private DoubleParameter hourlyTravellingCostLarge = new FixedDoubleParameter(0);

    private DoubleParameter hourlyTravellingCostSmall = new FixedDoubleParameter(0);

    private List<StartingMPA> startingMPAs = new LinkedList<>();
    /**
     * If flag set to true, small boats will come from port 1, large boats from port 2. Careful, if
     * you set this to true and there is only one port you'll get an exception
     */
    private boolean separatePorts = false;
    /**
     * if this is not NaN then it is used as the random seed to feed into the map-making function.
     * This allows for randomness in the biology/fishery
     */
    private Long mapMakerDedicatedRandomSeed = null;

    {
        // best first: startingMPAs.add(new StartingMPA(5,33,35,18));
        // best third:
        // startingMPAs.add(new StartingMPA(0,26,34,40));
    }

    public TwoPopulationsScenario() {
    }

    /**
     * this is the very first method called by the model when it is started. The scenario needs to
     * instantiate all the essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology
     * object
     */
    @Override
    public ScenarioEssentials start(final FishState model) {

        final MersenneTwisterFast random = model.random;

        MersenneTwisterFast mapMakerRandom = model.random;
        if (mapMakerDedicatedRandomSeed != null)
            mapMakerRandom = new MersenneTwisterFast(mapMakerDedicatedRandomSeed);
        // force the mapMakerRandom as the new random until the start is completed.
        model.random = mapMakerRandom;

        final BiologyInitializer biology = biologyInitializer.apply(model);
        final WeatherInitializer weather = weatherInitializer.apply(model);

        // create global biology
        final GlobalBiology global = biology.generateGlobal(mapMakerRandom, model);

        final MapInitializer mapMaker = mapInitializer.apply(model);
        final NauticalMap map = mapMaker.makeMap(random, global, model);

        // set habitats
        final HabitatInitializer habitat = habitatInitializer.apply(model);
        habitat.applyHabitats(map, mapMakerRandom, model);

        // this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(map, random, biology,
            weather,
            global, model
        );

        // create fixed price market
        final MarketMap marketMap = new MarketMap(global);
        /*
      market prices for each species
     */

        for (final Species species : global.getSpecies())
            marketMap.addMarket(species, market.apply(model));

        final PortInitializer portInitializer = ports.apply(model);
        portInitializer.buildPorts(map,
            mapMakerRandom,
            seaTile -> marketMap, model,
            new FixedGasPrice(gasPricePerLiter.applyAsDouble(mapMakerRandom))
        );

        // create initial mpas
        if (startingMPAs != null)
            for (final StartingMPA mpa : startingMPAs) {
                Logger.getGlobal().info(() -> "building MPA at " +
                    mpa.getTopLeftX() +
                    ", " +
                    mpa.getTopLeftY());
                mpa.buildMPA(map);
            }

        // substitute back the original randomizer
        model.random = random;

        return new ScenarioEssentials(global, map);
    }

    /**
     * called shortly after the essentials are set, it is time now to return a list of all the
     * agents
     *
     * @param model the model
     * @return a list of agents
     */
    @Override
    public ScenarioPopulation populateModel(final FishState model) {

        final LinkedList<Fisher> fisherList = new LinkedList<>();
        final NauticalMap map = model.getMap();
        final GlobalBiology biology = model.getBiology();
        final MersenneTwisterFast random = model.random;

        final Port[] ports = map.getPorts().toArray(new Port[map.getPorts().size()]);
        for (final Port port : ports)
            port.setGasPricePerLiter(gasPricePerLiter.applyAsDouble(random));

        // create the fisher factory object, this is for the small fishers
        final FisherFactory smallFisherFactory = new FisherFactory(
            getSmallPortSupplier(random, ports),
            regulationSmall,
            departingStrategySmall,
            destinationStrategySmall,
            fishingStrategySmall,
            discardingStrategySmall,
            gearStrategy,
            weatherStrategy,
            () -> new Boat(10, 10, new Engine(
                enginePower.applyAsDouble(random),
                smallLitersPerKilometer.applyAsDouble(random),
                smallSpeed.applyAsDouble(random)
            ),
                new FuelTank(smallFuelTankSize.applyAsDouble(random))
            ),
            () -> new Hold(smallHoldSize.applyAsDouble(random), biology),
            gearSmall,

            0
        );

        // create a factory for the large boats too
        final FisherFactory largeFishersFactory = new FisherFactory(
            getLargePortSupplier(random, ports),
            separateRegulations ? regulationLarge : regulationSmall,
            departingStrategyLarge,
            destinationStrategyLarge,
            fishingStrategyLarge,
            discardingStrategyLarge,
            gearStrategy,
            weatherStrategy,
            () -> new Boat(10, 10, new Engine(
                enginePower.applyAsDouble(random),
                largeLitersPerKilometer.applyAsDouble(random),
                largeSpeed.applyAsDouble(random)
            ),
                new FuelTank(largeFuelTankSize.applyAsDouble(random))
            ),
            () -> new Hold(largeHoldSize.applyAsDouble(random), biology),
            gearLarge,

            0
        );

        // adds predictors to the fisher if the usepredictors flag is up.
        // without predictors agents do not participate in ITQs
        final Consumer<Fisher> predictorSetup = FishStateUtilities.predictorSetup(
            usePredictors,
            biology
        );

        smallFisherFactory.getAdditionalSetups().add(predictorSetup);
        largeFishersFactory.getAdditionalSetups().add(predictorSetup);
        // add tags
        smallFisherFactory.getAdditionalSetups().add(fisher -> {
            fisher.getTagsList().add("small");
            fisher.getTagsList().add("yellow");
            fisher.getTagsList().add("canoe");
            // add hourly cost
            fisher.getAdditionalTripCosts().add(
                new HourlyCost(hourlyTravellingCostSmall.applyAsDouble(model.getRandom()))
            );
        });
        largeFishersFactory.getAdditionalSetups().add(fisher -> {
            fisher.getTagsList().add("large");
            fisher.getTagsList().add("ship");
            fisher.getTagsList().add("red");
            fisher.getAdditionalTripCosts().add(
                new HourlyCost(hourlyTravellingCostLarge.applyAsDouble(model.getRandom()))
            );
        });

        // add the small fishers
        for (int i = 0; i < smallFishers; i++) {
            final Fisher newFisher = smallFisherFactory.buildFisher(model);
            fisherList.add(newFisher);
        }

        // set the id so that all fishers have a different id
        largeFishersFactory.setNextID(smallFisherFactory.getNextID());
        for (int i = 0; i < largeFishers; i++) {
            final Fisher newFisher = largeFishersFactory.buildFisher(model);
            fisherList.add(newFisher);
        }

        // don't let large boats befriend small boats
        if (!allowTwoPopulationFriendships) {
            networkBuilder.addPredicate((NetworkPredicate) (from, to) -> (from.getTagsList()
                .contains("small") && to.getTagsList().contains("small")) ||
                (from.getTagsList().contains("large") && to.getTagsList().contains("large")));
        }

        if (!allowFriendshipsBetweenPorts) {
            // no friends from separate ports
            networkBuilder.addPredicate((NetworkPredicate) (from, to) -> from
                .getHomePort()
                .equals(to.getHomePort()));
        }

        model.getYearlyDataSet().registerGatherer("Small Fishers Total Income",
            fishState ->
                fishState.getFishers().stream().
                    filter(fisher -> fisher.getTagsList().contains("small")).
                    mapToDouble(value -> value.getLatestYearlyObservation(
                        FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).sum(), Double.NaN
        );

        model.getYearlyDataSet().registerGatherer("Large Fishers Total Income",
            fishState -> fishState.getFishers().stream().
                filter(fisher -> !fisher.getTagsList().contains("small")).
                mapToDouble(value -> value.getLatestYearlyObservation(
                    FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).sum(), Double.NaN
        );

        for (final Species species : biology.getSpecies()) {
            model.getYearlyDataSet()
                .registerGatherer("Small Fishers " +
                        species.getName() +
                        " " +
                        AbstractMarket.LANDINGS_COLUMN_NAME,
                    fishState -> fishState.getFishers().stream().
                        filter(fisher -> fisher.getTagsList().contains("small")).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)).sum(), Double.NaN
                );

            model.getYearlyDataSet()
                .registerGatherer("Large Fishers " +
                        species.getName() +
                        " " +
                        AbstractMarket.LANDINGS_COLUMN_NAME,
                    fishState -> fishState.getFishers().stream().
                        filter(fisher -> !fisher.getTagsList().contains("small")).
                        mapToDouble(value -> value.getLatestYearlyObservation(
                            species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)).sum(), Double.NaN
                );

        }

        // count effort too!
        final DataColumn smallEffort
            = model.getDailyDataSet().registerGatherer("Small Fishers Total Effort",
            (Gatherer<FishState>) ignored -> model.getFishers().stream().
                filter(fisher -> fisher.getTagsList().contains(
                    "small")).
                mapToDouble(
                    value -> value.getDailyCounter().getColumn(
                        FisherYearlyTimeSeries.EFFORT)).sum(), 0d
        );
        model.getYearlyDataSet().registerGatherer("Small Fishers Total Effort",
            FishStateUtilities.generateYearlySum(smallEffort), Double.NaN
        );
        final DataColumn largeEffort
            = model.getDailyDataSet().registerGatherer("Large Fishers Total Effort",
            (Gatherer<FishState>) ignored -> model.getFishers().stream().
                filter(fisher -> fisher.getTagsList().contains(
                    "large")).
                mapToDouble(
                    value -> value.getDailyCounter().getColumn(
                        FisherYearlyTimeSeries.EFFORT)).sum(), 0d
        );
        model.getYearlyDataSet().registerGatherer("Large Fishers Total Effort",
            FishStateUtilities.generateYearlySum(largeEffort), Double.NaN
        );

        final HashMap<String, FisherFactory> factory = new HashMap<>();
        factory.put(
            "large",
            largeFishersFactory
        );
        factory.put(
            "small",
            smallFisherFactory
        );

        if (fisherList.size() <= 1)
            return new ScenarioPopulation(
                fisherList,
                new SocialNetwork(new EmptyNetworkBuilder()),
                factory
            );
        else {
            return new ScenarioPopulation(fisherList, new SocialNetwork(networkBuilder), factory);
        }
    }

    protected Supplier<Port> getSmallPortSupplier(
        final MersenneTwisterFast random,
        final Port[] ports
    ) {
        return separatePorts ? () -> ports[0] : () -> ports[random.nextInt(ports.length)];
    }

    protected Supplier<Port> getLargePortSupplier(
        final MersenneTwisterFast random,
        final Port[] ports
    ) {
        return separatePorts ? () -> ports[1] : () -> ports[random.nextInt(ports.length)];
    }

    public AlgorithmFactory<? extends BiologyInitializer> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(
        final AlgorithmFactory<? extends BiologyInitializer> biologyInitializer
    ) {
        this.biologyInitializer = biologyInitializer;
    }

    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    public void setWeatherInitializer(
        final AlgorithmFactory<? extends WeatherInitializer> weatherInitializer
    ) {
        this.weatherInitializer = weatherInitializer;
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
        final AlgorithmFactory<? extends MapInitializer> mapInitializer
    ) {
        this.mapInitializer = mapInitializer;
    }

    public int getSmallFishers() {
        return smallFishers;
    }

    public void setSmallFishers(final int smallFishers) {
        this.smallFishers = smallFishers;
    }

    public int getLargeFishers() {
        return largeFishers;
    }

    public void setLargeFishers(final int largeFishers) {
        this.largeFishers = largeFishers;
    }

    public boolean isUsePredictors() {
        return usePredictors;
    }

    public void setUsePredictors(final boolean usePredictors) {
        this.usePredictors = usePredictors;
    }

    public DoubleParameter getSmallSpeed() {
        return smallSpeed;
    }

    public void setSmallSpeed(final DoubleParameter smallSpeed) {
        this.smallSpeed = smallSpeed;
    }

    public DoubleParameter getLargeSpeed() {
        return largeSpeed;
    }

    public void setLargeSpeed(final DoubleParameter largeSpeed) {
        this.largeSpeed = largeSpeed;
    }

    public DoubleParameter getSmallHoldSize() {
        return smallHoldSize;
    }

    public void setSmallHoldSize(final DoubleParameter smallHoldSize) {
        this.smallHoldSize = smallHoldSize;
    }

    public DoubleParameter getLargeHoldSize() {
        return largeHoldSize;
    }

    public void setLargeHoldSize(final DoubleParameter largeHoldSize) {
        this.largeHoldSize = largeHoldSize;
    }

    public DoubleParameter getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(final DoubleParameter enginePower) {
        this.enginePower = enginePower;
    }

    public DoubleParameter getSmallFuelTankSize() {
        return smallFuelTankSize;
    }

    public void setSmallFuelTankSize(final DoubleParameter smallFuelTankSize) {
        this.smallFuelTankSize = smallFuelTankSize;
    }

    public DoubleParameter getLargeFuelTankSize() {
        return largeFuelTankSize;
    }

    public void setLargeFuelTankSize(final DoubleParameter largeFuelTankSize) {
        this.largeFuelTankSize = largeFuelTankSize;
    }

    public DoubleParameter getSmallLitersPerKilometer() {
        return smallLitersPerKilometer;
    }

    public void setSmallLitersPerKilometer(final DoubleParameter smallLitersPerKilometer) {
        this.smallLitersPerKilometer = smallLitersPerKilometer;
    }

    public DoubleParameter getLargeLitersPerKilometer() {
        return largeLitersPerKilometer;
    }

    public void setLargeLitersPerKilometer(final DoubleParameter largeLitersPerKilometer) {
        this.largeLitersPerKilometer = largeLitersPerKilometer;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(final DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategySmall() {
        return departingStrategySmall;
    }

    public void setDepartingStrategySmall(
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategySmall
    ) {
        this.departingStrategySmall = departingStrategySmall;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategyLarge() {
        return departingStrategyLarge;
    }

    public void setDepartingStrategyLarge(
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategyLarge
    ) {
        this.departingStrategyLarge = departingStrategyLarge;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategySmall() {
        return destinationStrategySmall;
    }

    public void setDestinationStrategySmall(
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategySmall
    ) {
        this.destinationStrategySmall = destinationStrategySmall;
    }

    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategyLarge() {
        return destinationStrategyLarge;
    }

    public void setDestinationStrategyLarge(
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategyLarge
    ) {
        this.destinationStrategyLarge = destinationStrategyLarge;
    }

    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategyLarge() {
        return fishingStrategyLarge;
    }

    public void setFishingStrategyLarge(
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyLarge
    ) {
        this.fishingStrategyLarge = fishingStrategyLarge;
    }

    public AlgorithmFactory<? extends GearStrategy> getGearStrategy() {
        return gearStrategy;
    }

    public void setGearStrategy(
        final AlgorithmFactory<? extends GearStrategy> gearStrategy
    ) {
        this.gearStrategy = gearStrategy;
    }

    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getWeatherStrategy() {
        return weatherStrategy;
    }

    public void setWeatherStrategy(
        final AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy
    ) {
        this.weatherStrategy = weatherStrategy;
    }

    public AlgorithmFactory<? extends Regulation> getRegulationSmall() {
        return regulationSmall;
    }

    public void setRegulationSmall(
        final AlgorithmFactory<? extends Regulation> regulationSmall
    ) {
        this.regulationSmall = regulationSmall;
    }

    public AlgorithmFactory<? extends Regulation> getRegulationLarge() {
        return regulationLarge;
    }

    public void setRegulationLarge(
        final AlgorithmFactory<? extends Regulation> regulationLarge
    ) {
        this.regulationLarge = regulationLarge;
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

    public void setHabitatInitializer(
        final AlgorithmFactory<? extends HabitatInitializer> habitatInitializer
    ) {
        this.habitatInitializer = habitatInitializer;
    }

    public AlgorithmFactory<? extends Market> getMarket() {
        return market;
    }

    public void setMarket(final AlgorithmFactory<? extends Market> market) {
        this.market = market;
    }

    public List<StartingMPA> getStartingMPAs() {
        return startingMPAs;
    }

    public void setStartingMPAs(final List<StartingMPA> startingMPAs) {
        this.startingMPAs = startingMPAs;
    }

    public Long getMapMakerDedicatedRandomSeed() {
        return mapMakerDedicatedRandomSeed;
    }

    public void setMapMakerDedicatedRandomSeed(final Long mapMakerDedicatedRandomSeed) {
        this.mapMakerDedicatedRandomSeed = mapMakerDedicatedRandomSeed;
    }

    /**
     * Getter for property 'gearSmall'.
     *
     * @return Value for property 'gearSmall'.
     */
    public AlgorithmFactory<? extends Gear> getGearSmall() {
        return gearSmall;
    }

    /**
     * Setter for property 'gearSmall'.
     *
     * @param gearSmall Value to set for property 'gearSmall'.
     */
    public void setGearSmall(
        final AlgorithmFactory<? extends Gear> gearSmall
    ) {
        this.gearSmall = gearSmall;
    }

    /**
     * Getter for property 'gearLarge'.
     *
     * @return Value for property 'gearLarge'.
     */
    public AlgorithmFactory<? extends Gear> getGearLarge() {
        return gearLarge;
    }

    /**
     * Setter for property 'gearLarge'.
     *
     * @param gearLarge Value to set for property 'gearLarge'.
     */
    public void setGearLarge(
        final AlgorithmFactory<? extends Gear> gearLarge
    ) {
        this.gearLarge = gearLarge;
    }

    /**
     * Getter for property 'separateRegulations'.
     *
     * @return Value for property 'separateRegulations'.
     */
    public boolean isSeparateRegulations() {
        return separateRegulations;
    }

    /**
     * Setter for property 'separateRegulations'.
     *
     * @param separateRegulations Value to set for property 'separateRegulations'.
     */
    public void setSeparateRegulations(final boolean separateRegulations) {
        this.separateRegulations = separateRegulations;
    }

    /**
     * Getter for property 'ports'.
     *
     * @return Value for property 'ports'.
     */
    public AlgorithmFactory<? extends PortInitializer> getPorts() {
        return ports;
    }

    /**
     * Setter for property 'ports'.
     *
     * @param ports Value to set for property 'ports'.
     */
    public void setPorts(
        final AlgorithmFactory<? extends PortInitializer> ports
    ) {
        this.ports = ports;
    }

    /**
     * Getter for property 'separatePorts'.
     *
     * @return Value for property 'separatePorts'.
     */
    public boolean isSeparatePorts() {
        return separatePorts;
    }

    /**
     * Setter for property 'separatePorts'.
     *
     * @param separatePorts Value to set for property 'separatePorts'.
     */
    public void setSeparatePorts(final boolean separatePorts) {
        this.separatePorts = separatePorts;
    }

    /**
     * Getter for property 'allowTwoPopulationFriendships'.
     *
     * @return Value for property 'allowTwoPopulationFriendships'.
     */
    public boolean isAllowTwoPopulationFriendships() {
        return allowTwoPopulationFriendships;
    }

    /**
     * Setter for property 'allowTwoPopulationFriendships'.
     *
     * @param allowTwoPopulationFriendships Value to set for property
     *                                      'allowTwoPopulationFriendships'.
     */
    public void setAllowTwoPopulationFriendships(final boolean allowTwoPopulationFriendships) {
        this.allowTwoPopulationFriendships = allowTwoPopulationFriendships;
    }

    /**
     * Getter for property 'fishingStrategySmall'.
     *
     * @return Value for property 'fishingStrategySmall'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategySmall() {
        return fishingStrategySmall;
    }

    /**
     * Setter for property 'fishingStrategySmall'.
     *
     * @param fishingStrategySmall Value to set for property 'fishingStrategySmall'.
     */
    public void setFishingStrategySmall(
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategySmall
    ) {
        this.fishingStrategySmall = fishingStrategySmall;
    }

    /**
     * Getter for property 'hourlyTravellingCostLarge'.
     *
     * @return Value for property 'hourlyTravellingCostLarge'.
     */
    public DoubleParameter getHourlyTravellingCostLarge() {
        return hourlyTravellingCostLarge;
    }

    /**
     * Setter for property 'hourlyTravellingCostLarge'.
     *
     * @param hourlyTravellingCostLarge Value to set for property 'hourlyTravellingCostLarge'.
     */
    public void setHourlyTravellingCostLarge(final DoubleParameter hourlyTravellingCostLarge) {
        this.hourlyTravellingCostLarge = hourlyTravellingCostLarge;
    }

    /**
     * Getter for property 'hourlyTravellingCostSmall'.
     *
     * @return Value for property 'hourlyTravellingCostSmall'.
     */
    public DoubleParameter getHourlyTravellingCostSmall() {
        return hourlyTravellingCostSmall;
    }

    /**
     * Setter for property 'hourlyTravellingCostSmall'.
     *
     * @param hourlyTravellingCostSmall Value to set for property 'hourlyTravellingCostSmall'.
     */
    public void setHourlyTravellingCostSmall(final DoubleParameter hourlyTravellingCostSmall) {
        this.hourlyTravellingCostSmall = hourlyTravellingCostSmall;
    }

    /**
     * Getter for property 'discardingStrategySmall'.
     *
     * @return Value for property 'discardingStrategySmall'.
     */
    public AlgorithmFactory<? extends DiscardingStrategy> getDiscardingStrategySmall() {
        return discardingStrategySmall;
    }

    /**
     * Setter for property 'discardingStrategySmall'.
     *
     * @param discardingStrategySmall Value to set for property 'discardingStrategySmall'.
     */
    public void setDiscardingStrategySmall(
        final AlgorithmFactory<? extends DiscardingStrategy> discardingStrategySmall
    ) {
        this.discardingStrategySmall = discardingStrategySmall;
    }

    /**
     * Getter for property 'discardingStrategyLarge'.
     *
     * @return Value for property 'discardingStrategyLarge'.
     */
    public AlgorithmFactory<? extends DiscardingStrategy> getDiscardingStrategyLarge() {
        return discardingStrategyLarge;
    }

    /**
     * Setter for property 'discardingStrategyLarge'.
     *
     * @param discardingStrategyLarge Value to set for property 'discardingStrategyLarge'.
     */
    public void setDiscardingStrategyLarge(
        final AlgorithmFactory<? extends DiscardingStrategy> discardingStrategyLarge
    ) {
        this.discardingStrategyLarge = discardingStrategyLarge;
    }

    /**
     * Getter for property 'allowFriendshipsBetweenPorts'.
     *
     * @return Value for property 'allowFriendshipsBetweenPorts'.
     */
    public boolean isAllowFriendshipsBetweenPorts() {
        return allowFriendshipsBetweenPorts;
    }

    /**
     * Setter for property 'allowFriendshipsBetweenPorts'.
     *
     * @param allowFriendshipsBetweenPorts Value to set for property
     *                                     'allowFriendshipsBetweenPorts'.
     */
    public void setAllowFriendshipsBetweenPorts(final boolean allowFriendshipsBetweenPorts) {
        this.allowFriendshipsBetweenPorts = allowFriendshipsBetweenPorts;
    }
}

/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.base.Splitter;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.log.initializers.LogbookInitializer;
import uk.ac.ox.oxfish.fisher.log.initializers.NoLogbookFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.EffortCost;
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
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NullParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * a bunch of constructors that define a fisher
 */
public class FisherDefinition {

    private final DoubleParameter enginePower = new NormalDoubleParameter(5000, 100);
    /**
     * other additional setups we may want to add to the fisher;
     */
    private final List<Consumer<Fisher>> additionalSetups = new LinkedList<>();
    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DepartingStrategy> departingStrategy =
        new FixedRestTimeDepartingFactory();
    /**
     * factory to produce departing strategy
     */
    private AlgorithmFactory<? extends DestinationStrategy> destinationStrategy =
        new PerTripImitativeDestinationFactory();
    /**
     * factory to produce fishing strategy
     */
    private AlgorithmFactory<? extends FishingStrategy> fishingStrategy =
        new MaximumStepsFactory();
    private AlgorithmFactory<? extends GearStrategy> gearStrategy =
        new FixedGearStrategyFactory();
    private AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy = new NoDiscardingFactory();
    private AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy =
        new IgnoreWeatherFactory();
    private AlgorithmFactory<? extends Regulation> regulation = new AnarchyFactory();
    private DoubleParameter fuelTankSize = new FixedDoubleParameter(100000);
    private DoubleParameter literPerKilometer = new FixedDoubleParameter(10);
    /**
     * boat speed
     */
    private DoubleParameter speedInKmh = new FixedDoubleParameter(5);
    /**
     * hold size
     */
    private DoubleParameter holdSize = new FixedDoubleParameter(100);
    /**
     * efficiency
     */
    private AlgorithmFactory<? extends Gear> gear = new RandomCatchabilityTrawlFactory();
    private LinkedHashMap<String, Number> initialFishersPerPort = new LinkedHashMap<>();
    private AlgorithmFactory<? extends LogbookInitializer> logbook =
        new NoLogbookFactory();
    /**
     * additional variable cost (excluding oil) that we want to impose to the fishers;
     * negative numbers are ignored!
     */
    private DoubleParameter hourlyVariableCost = new FixedDoubleParameter(0);
    /**
     * the cost in money for each hour spent actually fishing
     */
    private DoubleParameter hourlyEffortCost = new NullParameter();
    /**
     * tags to add to each fisher created
     */
    //the format would be something like "boat,black"
    private String tags = "";
    private boolean usePredictors = false;
    /**
     * here we store an array with port names. If we need to create 3 fishers, 2 in portA and 1 in port B this will look:
     * [portA portA portB]
     */
    private String[] flatPortArray;
    //keeps track of how many fish have been built so far
    //so that we can match each to a specific port
    //when we have built enough, fishCreation resets to 0
    private int fishCreationIndex = 0;

    @SafeVarargs
    public final Entry<FisherFactory, List<Fisher>> instantiateFishers(
        final FishState model, final List<Port> ports, final int firstFisherID,
        final Consumer<Fisher>... additionalSetups
    ) {
        final FisherFactory factory = getFisherFactory(model, ports, firstFisherID);

        final List<Fisher> fishers = new LinkedList<>();
        for (final Consumer<Fisher> additionalSetup : additionalSetups)
            factory.getAdditionalSetups().add(additionalSetup);

        for (int i = 0; i < flatPortArray.length; i++)
            fishers.add(factory.buildFisher(model));

        return entry(factory, fishers);
    }

    public FisherFactory getFisherFactory(final FishState model, final List<Port> ports, final int firstFisherID) {

        final GlobalBiology biology = model.getBiology();
        final MersenneTwisterFast random = model.random;

        updateNumberOfInitialFishers();
        //   Preconditions.checkState(flatPortArray.length>0,"No fisher can be built because no port was provided!");


        //create logbook initializer
        final LogbookInitializer log = logbook.apply(model);
        log.start(model);

        //create the fisher factory object, it will be used by the fishstate object to create and kill fishers
        //while the model is running
        final Supplier<Boat> boatSupplier = makeBoatSupplier(random);
        final Supplier<Hold> holdSupplier = makeHoldSupplier(random, biology);

        final FisherFactory fisherFactory = new FisherFactory(
            //default to grabbing first port!
            () -> getNextFisher(ports, model),
            regulation,
            departingStrategy,
            destinationStrategy,
            fishingStrategy,
            discardingStrategy,
            gearStrategy,
            weatherStrategy,
            boatSupplier,
            holdSupplier,
            gear,
            firstFisherID
        );

        //add variable costs, if needed:
        fisherFactory.getAdditionalSetups().add(
            fisher -> {

                ///////////////////////////////////////////////////////////////////////
                // VARIABLE COSTS
                //don't bother if we don't have any variable costs
                if (hourlyVariableCost == null || hourlyVariableCost instanceof NullParameter)
                    return;
                final double vc = hourlyVariableCost.applyAsDouble(fisher.grabRandomizer());
                //don't bother if variable costs are negative!
                if (vc > 0)
                    fisher.getAdditionalTripCosts().add(
                        new HourlyCost(vc)
                    );

                ///////////////////////////////////////////////////////////////////////
                // EFFORT COSTS
                if (hourlyEffortCost == null || hourlyEffortCost instanceof NullParameter)
                    return;
                final double effortCost = hourlyEffortCost.applyAsDouble(fisher.grabRandomizer());
                //don't bother if variable costs are negative!
                if (effortCost > 0)
                    fisher.getAdditionalTripCosts().add(
                        new EffortCost(effortCost)
                    );
            }
        );

        fisherFactory.getAdditionalSetups().add(fisher -> log.add(fisher, model));

        //adds predictors to the fisher if the usepredictors flag is up.
        //without predictors agents do not participate in ITQs

        fisherFactory.getAdditionalSetups().add(
            FishStateUtilities.predictorSetup(
                usePredictors,
                model.getBiology()
            )
        );

        //add tags to fisher definition
        final List<String> tags = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(this.tags);
        if (!tags.isEmpty()) fisherFactory.getAdditionalSetups().add(fisher -> fisher.getTagsList().addAll(tags));

        //add other setups
        fisherFactory.getAdditionalSetups().addAll(additionalSetups);


        return fisherFactory;
    }

    private void updateNumberOfInitialFishers() {

        int numberOfInitialFishers = 0;
        for (final Entry<String, ? extends Number> portNumber : initialFishersPerPort.entrySet()) {
            numberOfInitialFishers += portNumber.getValue().intValue();
        }
        flatPortArray = new String[numberOfInitialFishers];
        int i = 0;
        for (final Entry<String, ? extends Number> portNumber : initialFishersPerPort.entrySet()) {
            for (int fishersToAdd = 0; fishersToAdd < portNumber.getValue().intValue(); fishersToAdd++) {
                flatPortArray[i] = portNumber.getKey();
                i++;
            }
        }


    }

    Supplier<Boat> makeBoatSupplier(final MersenneTwisterFast random) {
        return () -> new Boat(10, 10,
            makeEngineSupplier(random).get(),
            makeFuelTankSupplier(random).get()
        );
    }

    Supplier<Hold> makeHoldSupplier(final MersenneTwisterFast random, final GlobalBiology biology) {
        return () -> new Hold(holdSize.applyAsDouble(random), biology);
    }

    public Port getNextFisher(
        final List<Port> ports,
        final FishState model
    ) {
        // this assret is not true anymore; now after creating the first batch of fishers the rest is randomized assert fishCreationIndex <flatPortArray.length;
        final String portName;
        //you are creating the original fishers!
        if (fishCreationIndex < flatPortArray.length) {
            portName = flatPortArray[fishCreationIndex];
        }
        //you are creating additional fishers!, then pick stochastically
        else {
            assert model.getDay() > 0;
            portName = flatPortArray[model.getRandom().nextInt(flatPortArray.length)];
        }


        final Port toReturn = ports.stream()
            .filter(port -> port.getName().trim().equalsIgnoreCase(portName.trim()))
            .findFirst()
            .orElseGet(() -> {
                throw new RuntimeException(portName + " not found!");
            });

        fishCreationIndex++;

        return toReturn;
    }

    Supplier<Engine> makeEngineSupplier(final MersenneTwisterFast random) {
        return () -> new Engine(
            enginePower.applyAsDouble(random),
            literPerKilometer.applyAsDouble(random),
            speedInKmh.applyAsDouble(random)
        );
    }

    Supplier<FuelTank> makeFuelTankSupplier(final MersenneTwisterFast random) {
        return () -> new FuelTank(fuelTankSize.applyAsDouble(random));
    }

    /**
     * Getter for property 'departingStrategy'.
     *
     * @return Value for property 'departingStrategy'.
     */
    public AlgorithmFactory<? extends DepartingStrategy> getDepartingStrategy() {
        return departingStrategy;
    }

    /**
     * Setter for property 'departingStrategy'.
     *
     * @param departingStrategy Value to set for property 'departingStrategy'.
     */
    public void setDepartingStrategy(
        final AlgorithmFactory<? extends DepartingStrategy> departingStrategy
    ) {
        this.departingStrategy = departingStrategy;
    }

    /**
     * Getter for property 'destinationStrategy'.
     *
     * @return Value for property 'destinationStrategy'.
     */
    public AlgorithmFactory<? extends DestinationStrategy> getDestinationStrategy() {
        return destinationStrategy;
    }

    /**
     * Setter for property 'destinationStrategy'.
     *
     * @param destinationStrategy Value to set for property 'destinationStrategy'.
     */
    public void setDestinationStrategy(
        final AlgorithmFactory<? extends DestinationStrategy> destinationStrategy
    ) {
        this.destinationStrategy = destinationStrategy;
    }

    /**
     * Getter for property 'fishingStrategy'.
     *
     * @return Value for property 'fishingStrategy'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getFishingStrategy() {
        return fishingStrategy;
    }

    /**
     * Setter for property 'fishingStrategy'.
     *
     * @param fishingStrategy Value to set for property 'fishingStrategy'.
     */
    public void setFishingStrategy(
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategy
    ) {
        this.fishingStrategy = fishingStrategy;
    }

    /**
     * Getter for property 'gearStrategy'.
     *
     * @return Value for property 'gearStrategy'.
     */
    public AlgorithmFactory<? extends GearStrategy> getGearStrategy() {
        return gearStrategy;
    }

    /**
     * Setter for property 'gearStrategy'.
     *
     * @param gearStrategy Value to set for property 'gearStrategy'.
     */
    public void setGearStrategy(
        final AlgorithmFactory<? extends GearStrategy> gearStrategy
    ) {
        this.gearStrategy = gearStrategy;
    }

    /**
     * Getter for property 'discardingStrategy'.
     *
     * @return Value for property 'discardingStrategy'.
     */
    public AlgorithmFactory<? extends DiscardingStrategy> getDiscardingStrategy() {
        return discardingStrategy;
    }

    /**
     * Setter for property 'discardingStrategy'.
     *
     * @param discardingStrategy Value to set for property 'discardingStrategy'.
     */
    public void setDiscardingStrategy(
        final AlgorithmFactory<? extends DiscardingStrategy> discardingStrategy
    ) {
        this.discardingStrategy = discardingStrategy;
    }

    /**
     * Getter for property 'weatherStrategy'.
     *
     * @return Value for property 'weatherStrategy'.
     */
    public AlgorithmFactory<? extends WeatherEmergencyStrategy> getWeatherStrategy() {
        return weatherStrategy;
    }

    /**
     * Setter for property 'weatherStrategy'.
     *
     * @param weatherStrategy Value to set for property 'weatherStrategy'.
     */
    public void setWeatherStrategy(
        final AlgorithmFactory<? extends WeatherEmergencyStrategy> weatherStrategy
    ) {
        this.weatherStrategy = weatherStrategy;
    }

    /**
     * Getter for property 'regulation'.
     *
     * @return Value for property 'regulation'.
     */
    public AlgorithmFactory<? extends Regulation> getRegulation() {
        return regulation;
    }

    /**
     * Setter for property 'regulation'.
     *
     * @param regulation Value to set for property 'regulation'.
     */
    public void setRegulation(
        final AlgorithmFactory<? extends Regulation> regulation
    ) {
        this.regulation = regulation;
    }

    /**
     * Getter for property 'fuelTankSize'.
     *
     * @return Value for property 'fuelTankSize'.
     */
    public DoubleParameter getFuelTankSize() {
        return fuelTankSize;
    }

    /**
     * Setter for property 'fuelTankSize'.
     *
     * @param fuelTankSize Value to set for property 'fuelTankSize'.
     */
    public void setFuelTankSize(final DoubleParameter fuelTankSize) {
        this.fuelTankSize = fuelTankSize;
    }

    /**
     * Getter for property 'literPerKilometer'.
     *
     * @return Value for property 'literPerKilometer'.
     */
    public DoubleParameter getLiterPerKilometer() {
        return literPerKilometer;
    }

    /**
     * Setter for property 'literPerKilometer'.
     *
     * @param literPerKilometer Value to set for property 'literPerKilometer'.
     */
    public void setLiterPerKilometer(final DoubleParameter literPerKilometer) {
        this.literPerKilometer = literPerKilometer;
    }

    /**
     * Getter for property 'speedInKmh'.
     *
     * @return Value for property 'speedInKmh'.
     */
    public DoubleParameter getSpeedInKmh() {
        return speedInKmh;
    }

    /**
     * Setter for property 'speedInKmh'.
     *
     * @param speedInKmh Value to set for property 'speedInKmh'.
     */
    public void setSpeedInKmh(final DoubleParameter speedInKmh) {
        this.speedInKmh = speedInKmh;
    }

    /**
     * Getter for property 'holdSize'.
     *
     * @return Value for property 'holdSize'.
     */
    public DoubleParameter getHoldSize() {
        return holdSize;
    }

    /**
     * Setter for property 'holdSize'.
     *
     * @param holdSize Value to set for property 'holdSize'.
     */
    public void setHoldSize(final DoubleParameter holdSize) {
        this.holdSize = holdSize;
    }

    /**
     * Getter for property 'gear'.
     *
     * @return Value for property 'gear'.
     */
    public AlgorithmFactory<? extends Gear> getGear() {
        return gear;
    }

    /**
     * Setter for property 'gear'.
     *
     * @param gear Value to set for property 'gear'.
     */
    public void setGear(final AlgorithmFactory<? extends Gear> gear) {
        this.gear = gear;
    }

    /**
     * Getter for property 'initialFishersPerPort'.
     *
     * @return Value for property 'initialFishersPerPort'.
     */
    public LinkedHashMap<String, Number> getInitialFishersPerPort() {
        return initialFishersPerPort;
    }

    /**
     * Setter for property 'initialFishersPerPort'.
     *
     * @param initialFishersPerPort Value to set for property 'initialFishersPerPort'.
     */
    public void setInitialFishersPerPort(final LinkedHashMap<String, Number> initialFishersPerPort) {

        this.initialFishersPerPort = initialFishersPerPort;
    }


    /**
     * Getter for property 'usePredictors'.
     *
     * @return Value for property 'usePredictors'.
     */
    public boolean isUsePredictors() {
        return usePredictors;
    }

    /**
     * Setter for property 'usePredictors'.
     *
     * @param usePredictors Value to set for property 'usePredictors'.
     */
    public void setUsePredictors(final boolean usePredictors) {
        this.usePredictors = usePredictors;
    }

    /**
     * Getter for property 'tags'.
     *
     * @return Value for property 'tags'.
     */
    public String getTags() {
        return tags;
    }

    /**
     * Setter for property 'tags'.
     *
     * @param tags Value to set for property 'tags'.
     */
    public void setTags(final String tags) {
        this.tags = tags;
    }


    /**
     * Getter for property 'logbook'.
     *
     * @return Value for property 'logbook'.
     */
    public AlgorithmFactory<? extends LogbookInitializer> getLogbook() {
        return logbook;
    }

    /**
     * Setter for property 'logbook'.
     *
     * @param logbook Value to set for property 'logbook'.
     */
    public void setLogbook(
        final AlgorithmFactory<? extends LogbookInitializer> logbook
    ) {
        this.logbook = logbook;
    }

    public DoubleParameter getHourlyVariableCost() {
        return hourlyVariableCost;
    }

    public void setHourlyVariableCost(final DoubleParameter hourlyVariableCost) {
        this.hourlyVariableCost = hourlyVariableCost;
    }

    /**
     * not sure about exposing this to YAML constructor; that's why it's "grab" and not "get"
     */
    public List<Consumer<Fisher>> grabAdditionalSetups() {
        return additionalSetups;
    }

    public DoubleParameter getHourlyEffortCost() {
        return hourlyEffortCost;
    }

    public void setHourlyEffortCost(final DoubleParameter hourlyEffortCost) {
        this.hourlyEffortCost = hourlyEffortCost;
    }
}

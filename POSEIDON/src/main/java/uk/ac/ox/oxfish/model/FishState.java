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

package uk.ac.ox.oxfish.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;
import uk.ac.ox.oxfish.model.scenario.*;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * The main model object. Like all the other simstates it holds the reference
 * to schedule and randomizer
 * Created by carrknight on 3/29/15.
 */
public class FishState extends SimState {

    public static final String DEFAULT_POPULATION_NAME = "default_population";
    private final String uniqueID = UUID.randomUUID().toString();
    /**
     * Dataset of all the columns that are updated daily
     */
    private final FishStateDailyTimeSeries dailyDataSet = new FishStateDailyTimeSeries();
    /**
     * Dataset of all the columns that are updated yearly.
     */
    private final FishStateYearlyTimeSeries yearlyDataSet = new FishStateYearlyTimeSeries(dailyDataSet);
    /**
     * all the objects that need to be started when this model starts but also need a reference to the original fisher
     */
    private final List<Entry<Fisher, FisherStartable>> fisherStartables = new LinkedList<>();
    /**
     * x steps equal 1 day
     */
    final private int stepsPerDay;
    /**
     * any object that wants to output a file at the end of the simulation can register here
     */
    private final List<OutputPlugin> outputPlugins = new LinkedList<>();
    /**
     * generic yearly counter keeping track of yearly model stuff (biology mostly)
     */
    private final Counter yearlyCounter = new Counter(IntervalPolicy.EVERY_YEAR);
    /**
     * generic yearly counter keeping track of yearly model stuff (biology mostly)
     */
    private final Counter dailyCounter = new Counter(IntervalPolicy.EVERY_DAY);
    /**
     * list of all objects that need to be started when the model actually starts
     */
    private final List<Startable> toStart;
    /**
     * aggregate steppables for phases where there is no need for randomization
     */
    private final LinkedHashMap<StepOrder, AggregateSteppable> aggregateYearlySteppables = new LinkedHashMap<>();
    private final LinkedHashMap<StepOrder, AggregateSteppable> aggregateDailySteppables = new LinkedHashMap<>();
    /**
     * contains the geography of the map
     */
    private NauticalMap map;
    /**
     * a list of all the species
     */
    private GlobalBiology biology;
    /**
     * the list of agents. Observable so it can be listened to for changes
     */
    private ObservableList<Fisher> fishers;
    /**
     * may contain the fad map, if fads are used in the scenario (null otherwise)
     */
    private FadMap fadMap = null;
    /**
     * created by the scenario (optionally, could be null) this object is used to add fishers on the fly.
     */
    private Map<String, FisherFactory> fisherFactory;
    /**
     * the scenario object responsible for the initialization of the model
     */
    private Scenario scenario = new PrototypeScenario();
    /**
     * flag that is set to true when start() is called
     */
    private boolean started = false;
    /**
     * the social network
     */
    private SocialNetwork socialNetwork;
    /**
     * here you store all entryPlugins (any steppable that can automatically generate new fishers over time).
     * This is useful for regulations to pause them
     */

    private List<EntryPlugin> entryPlugins;

    public FishState() {
        this(System.currentTimeMillis(), 1);
    }

    public FishState(final long seed, final int stepsPerDay) {
        super(seed);
        this.stepsPerDay = stepsPerDay;

        toStart = new LinkedList<>();

        for (final StepOrder order : StepOrder.values())
            if (!order.isToRandomize()) {
                aggregateYearlySteppables.put(order, new AggregateSteppable());
                aggregateDailySteppables.put(order, new AggregateSteppable());
            }


    }


    /**
     * create a fishstate model with one step per day
     *
     * @param seed the random seedf
     */
    public FishState(final long seed) {
        this(seed, 1);
    }

    public static double round(final double value, final int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public int getStepsPerDay() {
        return stepsPerDay;
    }

    public double getHoursSinceStart() {
        return getStep() * getHoursPerStep();
    }

    public int getStep() {
        return (int) Math.round(schedule.getTime());
    }

    /**
     * how many hours in a step, basically.
     */
    public double getHoursPerStep() {
        return 24.0 / (double) stepsPerDay;
    }

    /**
     * so far it does the following:
     * * read in the data into a the raster
     */
    @Override
    public void start() {


        Preconditions.checkState(!started, "Already started!");
        super.start();

        //prepare containers
        entryPlugins = new LinkedList<>();


        //start the counter
        yearlyCounter.start(this);
        dailyCounter.start(this);

        //schedule aggregate steppables

        for (final Entry<StepOrder, AggregateSteppable> steppable : aggregateYearlySteppables.entrySet())
            schedule.scheduleRepeating(steppable.getValue(), steppable.getKey().ordinal(), stepsPerDay * 365);
        for (final Entry<StepOrder, AggregateSteppable> steppable : aggregateDailySteppables.entrySet())
            schedule.scheduleRepeating(steppable.getValue(), steppable.getKey().ordinal(), stepsPerDay);

        final ScenarioEssentials initialization = scenario.start(this);

        //read raster bathymetry
        //  map = NauticalMap.initializeWithDefaultValues();
        map = initialization.getMap();
        //      map.addCities("cities/cities.shp");

        biology = initialization.getBiology();


        final ScenarioPopulation scenarioPopulation = scenario.populateModel(this);
        fisherFactory = scenarioPopulation.getFactory();
        fishers = ObservableList.observableList(scenarioPopulation.getPopulation());

        socialNetwork = scenarioPopulation.getNetwork();
        socialNetwork.populate(this);
        map.start(this);


        //start the markets (for each port
        for (final Port port : getPorts()) {
            for (final Market market : port.getDefaultMarketMap().getMarkets()) {
                market.start(this);
            }
            for (final Species species : biology.getSpecies())
                dailyDataSet.registerGatherer("Price of " + species + " at " + port.getName(),
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(final FishState fishState) {
                            return port.getMarginalPrice(species);
                        }
                    }
                    , Double.NaN
                );
        }

        //start the fishers
        for (final Fisher fisher : fishers)
            fisher.start(this);


        //start everything else that required to be started
        for (final Startable startable : toStart)
            startable.start(this);

        fisherStartables.forEach(entry ->
            entry.getValue().start(this, entry.getKey())
        );
        dailyDataSet.start(this, this);
        yearlyDataSet.start(this, this);
        started = true;


    }

    /**
     * a short-cut from map.getPorts()
     *
     * @return the set of ports in the model
     */
    public List<Port> getPorts() {
        return map.getPorts();
    }

    /**
     * @return an unmodifiable list of all the species available
     */
    public List<Species> getSpecies() {
        return biology.getSpecies();
    }

    /**
     * @return an unmodifiable list of all the species available
     */
    public Species getSpecies(final String speciesName) {
        return biology.getSpecie(speciesName);
    }

    public GeomGridField getRasterBathymetry() {
        return map.getRasterBathymetry();
    }

    public GeomVectorField getMpaVectorField() {
        return map.getMpaVectorField();
    }

    public SparseGrid2D getPortGrid() {
        return map.getPortMap();
    }

    public SparseGrid2D getFisherGrid() {
        return map.getFisherGrid();
    }

    public GlobalBiology getBiology() {
        return biology;
    }

    public int getDay() {
        return (int) (schedule.getTime() / stepsPerDay);
    }

    public int getCalendarYear() {
        return getDate().getYear();
    }

    public LocalDate getDate() {
        // we add year and "day of the year" separately because our simulation years
        // are always 365 days, so adding `getDays()` would get us out of sync for
        // when leap years occur
        return getScenario().getStartDate().plusYears(getYear()).plusDays(getDayOfTheYear() - 1);
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(final Scenario scenario) {
        this.scenario = scenario;
    }

    public int getYear() {
        return (int) getYear(schedule.getTime());
    }

    /**
     * what day of the year (from 1 to 365) is this?
     *
     * @return the day of the year
     */
    public int getDayOfTheYear() {
        return getDayOfTheYear((int) schedule.getTime());
    }

    public double getYear(final double scheduleTime) {
        return ((scheduleTime / stepsPerDay) / 365);
    }

    public int getDayOfTheYear(final int timeStep) {
        return ((timeStep / stepsPerDay) % 365) + 1;
    }

    public void scheduleOnce(final Steppable steppable, final StepOrder order) {
        schedule.scheduleOnce(steppable, order.ordinal());
    }

    /**
     * after x days pass step this object once and forget it
     *
     * @param steppable   steppable
     * @param order       order at which to step it
     * @param daysFromNow how many days from now
     */
    public void scheduleOnceInXDays(final Steppable steppable, final StepOrder order, final int daysFromNow) {
        schedule.scheduleOnceIn(stepsPerDay * daysFromNow, steppable, order.ordinal());
    }

    /**
     * will step this object only once when the specific year starts. If that year is in the past, it won't step.
     * Implementation wise unfortunately I just check every year to see whether to step this or not. It's quite silly.
     *
     * @param steppable the action to step
     * @param order     what order should it be stepped on
     * @param year      what year should this happen.
     */
    public Stoppable scheduleOnceAtTheBeginningOfYear(
        final Steppable steppable,
        final StepOrder order,
        final int year
    ) {

        final Steppable container = new Steppable() {
            @Override
            public void step(final SimState simState) {
                //the plus one is because when this is stepped it's the 365th day
                if (((FishState) simState).getYear() + 1 == year) {
                    steppable.step(simState);
                }

            }
        };
        return scheduleEveryYear(container, order);

    }

    public Stoppable scheduleEveryYear(final Steppable steppable, final StepOrder order) {
        if (order.isToRandomize())
            return schedule.scheduleRepeating(steppable, order.ordinal(), 365 * stepsPerDay);
        else
            return aggregateYearlySteppables.get(order).add(steppable);
    }

    public Stoppable schedulePerPolicy(final Steppable steppable, final StepOrder order, final IntervalPolicy policy) {
        switch (policy) {
            case EVERY_STEP:
                return scheduleEveryStep(steppable, order);
            case EVERY_DAY:
                return scheduleEveryDay(steppable, order);
            case EVERY_MONTH:
                return scheduleEveryXDay(steppable, order, 30);
            case EVERY_YEAR:
                return scheduleEveryYear(steppable, order);
            default:
                Preconditions.checkState(false, "Reset Policy not found");
        }

        return null;
    }

    public Stoppable scheduleEveryStep(final Steppable steppable, final StepOrder order) {
        return schedule.scheduleRepeating(steppable, order.ordinal(), 1.0);
    }

    public Stoppable scheduleEveryDay(final Steppable steppable, final StepOrder order) {
        if (order.isToRandomize())
            return schedule.scheduleRepeating(steppable, order.ordinal(), stepsPerDay);
        else
            return aggregateDailySteppables.get(order).add(steppable);
    }

    public Stoppable scheduleEveryXDay(final Steppable steppable, final StepOrder order, final int periodInDays) {
        return schedule.scheduleRepeating(steppable, order.ordinal(), stepsPerDay * periodInDays);
    }

    /**
     * Returns a map from species to their total biomass (including FAD biomass).
     */
    public Map<Species, Double> getTotalBiomasses() {
        return biology.getSpecies().stream()
            .collect(toImmutableMap(identity(), this::getTotalBiomass));
    }

    public double getTotalBiomass(final Species species) {
        final double fadBiomass = fadMap == null ? 0 : fadMap.getTotalBiomass(species);
        return fadBiomass + map.getTotalBiomass(species);
    }

    public double getTotalAbundance(final Species species, final int bin) {
        return
            map.getAllSeaTilesExcludingLandAsList().stream().filter(
                    new Predicate<SeaTile>() {
                        @Override
                        public boolean test(final SeaTile seaTile) {
                            return !(seaTile.getBiology() instanceof EmptyLocalBiology);
                        }
                    }
                ).
                mapToDouble(
                    new ToDoubleFunction<SeaTile>() {
                        @Override
                        public double applyAsDouble(final SeaTile value) {
                            return value.getAbundance(species).getAbundanceInBin(bin);
                        }
                    }
                ).sum();
    }

    /**
     * sums up the count of fish in all sea tiles and returns it as an array (safe to modify)
     *
     * @param species
     * @return
     */
    public double[][] getTotalAbundance(final Species species) {

        final double[][] totalAbundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for (final SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {

            final StructuredAbundance localAbundance = seaTile.getAbundance(species);

            for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {
                for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                    if (seaTile.isFishingEvenPossibleHere()) {
                        totalAbundance[subdivision][bin] += localAbundance.getAbundance(subdivision, bin);
                    }
                }
            }
        }
        assert totalAbundance[species.getNumberOfSubdivisions() - 1][species.getNumberOfBins() - 1] ==
            getTotalAbundance(species, species.getNumberOfSubdivisions() - 1, species.getNumberOfBins() - 1);
        return totalAbundance;
    }

    public double getTotalAbundance(final Species species, final int subdivision, final int bin) {
        double sum = 0;
        for (final SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            if (seaTile.isFishingEvenPossibleHere())
                sum += seaTile.getAbundance(species).getAbundance(subdivision, bin);
        }
        return sum;
    }

    /**
     * if the model hasn't started, register this object to be started when the model is. Otherwise start it now
     *
     * @param startable the object to start
     */
    public void registerStartable(final FisherStartable startable, final Fisher fisher) {
        if (started)
            startable.start(this, fisher);
        else
            fisherStartables.add(entry(fisher, startable));
    }

    /**
     * record the fact that somebody fished somewhere
     *
     * @param tile where it has been fished
     */
    public void recordFishing(final SeaTile tile) {
        map.recordFishing(tile);
    }

    public IntGrid2D getDailyTrawlsMap() {
        return map.getDailyTrawlsMap();
    }

    public List<Market> getAllMarketsForThisSpecie(final Species species) {
        //ports can share markets and we don't want to double count
        final Set<Market> toAggregate = new HashSet<>();
        //now get for each port, its markets
        for (final Port port : getPorts()) {
            final Market market = port.getDefaultMarketMap().getMarket(species);
            if (market != null)
                toAggregate.add(market);
        }

        return new LinkedList<>(toAggregate);
    }

    public String timeString() {
        return "Year: " + getYear() + " day: " + getDayOfTheYear() + " hour: " + getHour();
    }

    public int getHour() {
        return (int) (schedule.getTime() % stepsPerDay * 24 / stepsPerDay);
    }

    public MersenneTwisterFast getRandom() {
        return random;
    }

    public Double getLatestDailyObservation(final String columnName) {
        return getDailyDataSet().getColumn(columnName).getLatest();
    }

    public FishStateDailyTimeSeries getDailyDataSet() {
        return dailyDataSet;
    }

    public Double getLatestYearlyObservation(final String columnName) {
        return getYearlyDataSet().getColumn(columnName).getLatest();
    }

    public FishStateYearlyTimeSeries getYearlyDataSet() {
        return yearlyDataSet;
    }

    public Double getAverageYearlyObservation(final String columnName) {

        final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
        for (final Double observation : getYearlyDataSet().getColumn(columnName))
            statistics.accept(observation.doubleValue());

        return statistics.getAverage();
    }

    /**
     * called usually by GUI. Call this after the scenario has started not before!
     *
     * @return
     */
    public Fisher createFisher(final String nameOfPopulation) {
        Preconditions.checkState(canCreateMoreFishers());
        final Fisher newborn = fisherFactory.get(nameOfPopulation).buildFisher(this);
        getFishers().add(newborn);
        getSocialNetwork().addFisher(newborn, this);
        registerStartable(newborn);
        return newborn;
    }

    /**
     * @return true if the model has a fisher factory it can use to create more fishers
     */
    public boolean canCreateMoreFishers() {
        return fisherFactory != null && !fisherFactory.isEmpty();
    }

    public ObservableList<Fisher> getFishers() {
        return fishers;
    }

    public SocialNetwork getSocialNetwork() {
        return socialNetwork;
    }

    /**
     * if the model hasn't started, register this object to be started when the model is. Otherwise start it now
     *
     * @param startable the object to start
     */
    public void registerStartable(final Startable startable) {
        if (started) {
            startable.start(this);
            //store it anyway
            toStart.add(startable);

            //  scheduleOnce((Steppable) simState -> startable.start(FishState.this), StepOrder.DAWN);
        } else
            toStart.add(startable);
    }

    /**
     * Getter for property 'fisherFactory'.
     *
     * @return Value for property 'fisherFactory'.
     */
    public FisherFactory getFisherFactory(final String nameOfPopulation) {
        return fisherFactory.get(nameOfPopulation);


    }

    public Set<Entry<String, FisherFactory>> getFisherFactories() {
        return fisherFactory.entrySet();

    }

    public void killRandomFisher() {
        Preconditions.checkState(fishers.size() > 0, "There are no more fishers left to kill");
        final Fisher sacrifice = fishers.remove(random.nextInt(fishers.size()));
        killSpecificFisher(sacrifice);

    }

    public void killSpecificFisher(final Fisher sacrifice) {
        sacrifice.turnOff();
        map.getFisherGrid().setObjectLocation(sacrifice, -1, -1);
        fishers.remove(sacrifice);

    }

    public int getNumberOfFishers() {
        return fishers.size();
    }

    /**
     * Getter for property 'started'.
     *
     * @return Value for property 'started'.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * //todo move this to a config file rather than an all or nothing switch
     */
    public void attachAdditionalGatherers() {
        //keep track of average X location at the end of the year
        final DataColumn dailyX = this.dailyDataSet.registerGatherer(
            "Average X Towed",
            makeMostFishedTileGatherer((fisher, mostFishedTileInTrip) ->
                mostFishedTileInTrip.getGridX()
            ),
            Double.NaN
        );

        //yearly you account for the average
        this.yearlyDataSet.registerGatherer(
            "Average X Towed",
            FishStateUtilities.generateYearlyAverage(dailyX),
            Double.NaN
        );

        //keep track of average X location at the end of the year
        this.dailyDataSet.registerGatherer(
            "Average Distance From Port",
            makeMostFishedTileGatherer((fisher, mostFishedTileInTrip) ->
                map.distance(fisher.getHomePort().getLocation(), mostFishedTileInTrip)
            ),
            Double.NaN
        );

        this.dailyDataSet.registerGatherer("% of Tows on the Line",
            new Gatherer<FishState>() {
                @Override
                public Double apply(final FishState state) {

                    double trawlsSum = 0;
                    double lineSum = 0;
                    final NauticalMap map = state.getMap();
                    for (final SeaTile tile : map.getAllSeaTilesExcludingLandAsList()) {
                        final int trawlsHere = map.getDailyTrawlsMap().get(
                            tile.getGridX(),
                            tile.getGridY()
                        );
                        trawlsSum += trawlsHere;
                        if (map.getTilesOnTheMPALine().contains(tile)) {
                            lineSum += trawlsHere;
                        }
                    }
                    if (trawlsSum == 0)
                        return Double.NaN;
                    assert trawlsSum >= lineSum;
                    return lineSum / trawlsSum;

                }
            }
            , Double.NaN
        );


        this.yearlyDataSet.registerGatherer("Mileage-Catch Correlation",
            new Gatherer<FishState>() {
                @Override
                public Double apply(final FishState state) {

                    final LinkedList<Double> mileage = new LinkedList<>();
                    final LinkedList<Double> catches = new LinkedList<>();

                    final Species first = biology.getSpecie(0);

                    for (final Fisher fisher : fishers) {
                        final Gear gear = fisher.getGear();
                        if (gear instanceof RandomCatchabilityTrawl) {
                            mileage.add(((RandomCatchabilityTrawl) gear).getGasPerHourFished());
                            catches.add(fisher.getLatestYearlyObservation(first.getName() + " Landings"));
                        }
                    }

                    if (mileage.size() > 0)
                        return FishStateUtilities.computeCorrelation(
                            Doubles.toArray(mileage),
                            Doubles.toArray(catches)
                        );
                    else
                        return Double.NaN;


                }
            }
            , Double.NaN
        );
    }

    private Gatherer<FishState> makeMostFishedTileGatherer(final ToDoubleBiFunction<Fisher, SeaTile> extractor) {
        return state -> {
            double sum = 0;
            double observations = 0;
            for (final Fisher fisher : getFishers()) {
                final TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                if (lastFinishedTrip != null) {
                    final SeaTile mostFishedTileInTrip = lastFinishedTrip.getMostFishedTileInTrip();
                    if (mostFishedTileInTrip != null) {
                        sum += extractor.applyAsDouble(fisher, mostFishedTileInTrip);
                        observations++;
                    }
                }
            }
            if (observations == 0)
                return Double.NaN;
            return sum / observations;
        };
    }

    public NauticalMap getMap() {
        return map;
    }

    public Bag getFishersAtLocation(final int x, final int y) {
        return map.getFishersAtLocation(x, y);
    }

    public Bag getFishersAtLocation(final SeaTile tile) {
        return map.getFishersAtLocation(tile);
    }


    /**
     * Getter for property 'outputPlugins'.
     *
     * @return Value for property 'outputPlugins'.
     */
    public List<OutputPlugin> getOutputPlugins() {
        return outputPlugins;
    }

    /**
     * Getter for property 'yearlyCounter'.
     *
     * @return Value for property 'yearlyCounter'.
     */
    public Counter getYearlyCounter() {
        return yearlyCounter;
    }

    /**
     * Getter for property 'dailyCounter'.
     *
     * @return Value for property 'dailyCounter'.
     */
    public Counter getDailyCounter() {
        return dailyCounter;
    }

    @Override
    public void finish() {
        super.finish();
        if (fishers != null) {
            for (final Fisher fisher : fishers)
                fisher.turnOff();
            fishers.clear();

        }
        for (final Startable startable : toStart) {
            startable.turnOff();
        }
        toStart.clear();
        yearlyDataSet.turnOff();
        yearlyCounter.turnOff();
        dailyCounter.turnOff();
        dailyDataSet.turnOff();
        if (map != null)
            map.turnOff();
        aggregateYearlySteppables.clear();
        aggregateDailySteppables.clear();
        entryPlugins.clear(); //should automatically turn off because they were scheduled

    }

    public FadMap getFadMap() {
        return fadMap;
    }

    public void setFadMap(final FadMap fadMap) {
        this.fadMap = fadMap;
    }

    public List<Startable> viewStartables() {
        return ImmutableList.copyOf(toStart);
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public List<EntryPlugin> getEntryPlugins() {
        return entryPlugins;
    }

    public void setEntryPlugins(final List<EntryPlugin> entryPlugins) {
        this.entryPlugins = entryPlugins;
    }
}

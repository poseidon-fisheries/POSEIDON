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
import com.google.common.primitives.Doubles;
import ec.util.MersenneTwisterFast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.model.data.collectors.*;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.ThreePricesMarket;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.scenario.*;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 *
 * The main model object. Like all the other simstates it holds the reference
 * to schedule and randomizer
 * Created by carrknight on 3/29/15.
 */
public class FishState  extends SimState{


    public static final String DEFAULT_POPULATION_NAME = "default_population";
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
     * list of all objects that need to be started when the model actually starts
     */
    private List<Startable> toStart;

    /**
     * Dataset of all the columns that are updated daily
     */
    private final FishStateDailyTimeSeries dailyDataSet = new FishStateDailyTimeSeries();

    /**
     * Dataset of all the columns that are updated yearly.
     */
    private final FishStateYearlyTimeSeries yearlyDataSet = new FishStateYearlyTimeSeries(dailyDataSet);

    /**
     * created by the scenario (optionally, could be null) this object is used to add fishers on the fly.
     */
    private Map<String,FisherFactory> fisherFactory;

    /**
     * all the objects that need to be started when this model starts but also need a reference to the original fisher
     */
    private final List<Pair<Fisher,FisherStartable>> fisherStartables = new LinkedList<>();

    /**
     * the scenario object responsible for the initialization of the model
     */
    private Scenario scenario = new PrototypeScenario();

    /**
     * flag that is set to true when start() is called
     */
    private boolean started = false;

    /**
     * x steps equal 1 day
     */
    final private int stepsPerDay;

    /**
     * the social network
     */
    private SocialNetwork socialNetwork;


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
     * aggregate steppables for phases where there is no need for randomization
     */
    private HashMap<StepOrder,AggregateSteppable> aggregateYearlySteppables = new HashMap<>();

    private HashMap<StepOrder,AggregateSteppable> aggregateDailySteppables = new HashMap<>();

    public int getStepsPerDay() {
        return stepsPerDay;
    }

    /**
     * how many hours in a step, basically.
     */
    public double getHoursPerStep() {
        return 24.0/(double) stepsPerDay;
    }


    public double getHoursSinceStart()
    {
        return getStep() * getHoursPerStep();
    }



    public FishState(){
        this(System.currentTimeMillis(),1);
    }

    /**
     * create a fishstate model with one step per day
     * @param seed the random seedf
     */
    public FishState(long seed) {
        this(seed,1);
    }

    public FishState(long seed, int stepsPerDay)
    {
        super(seed);
        this.stepsPerDay = stepsPerDay;
        toStart = new LinkedList<>();

        for(StepOrder order : StepOrder.values())
            if(!order.isToRandomize()) {
                aggregateYearlySteppables.put(order, new AggregateSteppable());
                aggregateDailySteppables.put(order, new AggregateSteppable());
            }

    }



    /**
     * so far it does the following:
     *  * read in the data into a the raster
     */
    @Override
    public void start() {



        Preconditions.checkState(!started, "Already started!");
        super.start();

        //start the counter
        yearlyCounter.start(this);
        dailyCounter.start(this);

        //schedule aggregate steppables
        for(Map.Entry<StepOrder,AggregateSteppable> steppable :aggregateYearlySteppables.entrySet()  )
            schedule.scheduleRepeating(steppable.getValue(),steppable.getKey().ordinal(), stepsPerDay*365);
        for(Map.Entry<StepOrder,AggregateSteppable> steppable :aggregateDailySteppables.entrySet()  )
            schedule.scheduleRepeating(steppable.getValue(),steppable.getKey().ordinal(), stepsPerDay);



        ScenarioEssentials initialization = scenario.start(this);

        //read raster bathymetry
        //  map = NauticalMap.initializeWithDefaultValues();
        map = initialization.getMap();
        //      map.addCities("cities/cities.shp");

        biology = initialization.getBiology();
        //add counters for catches if there is any need (aggregate catches are counted by fishers, here we want abundance based)
        for(Species species : biology.getSpecies())
            if(species.getNumberOfBins()>0)
                for(int age=0; age<species.getNumberOfBins(); age++)
                {
                    String columnName = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME + ThreePricesMarket.AGE_BIN_PREFIX + age;
                    int finalAge = age;
                    DataColumn dailyCatches = dailyDataSet.registerGatherer(
                            columnName,
                            new Gatherer<FishState>() {
                                @Override
                                public Double apply(FishState state) {

                                    double sum = 0;
                                    for(Fisher fisher : state.getFishers())
                                    {
                                        sum+= fisher.getCountedLandingsPerBin(species, finalAge);
                                    }

                                    return sum;

                                }
                            },0
                    );
                    yearlyDataSet.registerGatherer(columnName,
                                                   FishStateUtilities.generateYearlySum(dailyCatches),
                                                   0d);

                }




        final ScenarioPopulation scenarioPopulation = scenario.populateModel(this);
        fisherFactory = scenarioPopulation.getFactory();
        fishers = FXCollections.observableList(scenarioPopulation.getPopulation());

        socialNetwork = scenarioPopulation.getNetwork();
        socialNetwork.populate(this);
        map.start(this);


        //start the markets (for each port
        for(Port port : getPorts()) {
            for (Market market : port.getDefaultMarketMap().getMarkets()) {
                market.start(this);
            }
            for(Species species : biology.getSpecies())
                dailyDataSet.registerGatherer("Price of " + species + " at " + port.getName(),
                                              new Gatherer<FishState>() {
                                                  @Override
                                                  public Double apply(FishState fishState) {
                                                      return port.getMarginalPrice(species);
                                                  }
                                              }
                        , Double.NaN);
        }

        //start the fishers
        for(Fisher fisher : fishers)
            fisher.start(this);


        //start everything else that required to be started
        for(Startable startable : toStart)
            startable.start(this);

        for( Pair<Fisher,FisherStartable> startable : fisherStartables)
            startable.getSecond().start(this,startable.getFirst());
        dailyDataSet.start(this,this);
        yearlyDataSet.start(this,this);
        started=true;








    }

    /**
     * a short-cut from map.getPorts()
     * @return the set of ports in the model
     */
    public List<Port> getPorts() {
        return map.getPorts();
    }

    /**
     *
     * @return an unmodifiable list of all the species available
     */
    public List<Species> getSpecies() {
        return biology.getSpecies();
    }

    public NauticalMap getMap() {
        return map;
    }

    public GeomGridField getRasterBathymetry() {
        return map.getRasterBathymetry();
    }

    public GeomVectorField getMpaVectorField() {
        return map.getMpaVectorField();
    }

    public SparseGrid2D getPortGrid(){
        return map.getPortMap();
    }



    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public ObservableList<Fisher> getFishers() {
        return fishers;
    }

    public SparseGrid2D getFisherGrid() {
        return map.getFisherGrid();
    }

    public GlobalBiology getBiology() {
        return biology;
    }


    /**
     * what day of the year (from 1 to 365) is this?
     * @return the day of the year
     */
    public int getDayOfTheYear()
    {
        return (int) ((schedule.getTime() / stepsPerDay) % 365) + 1;
    }

    public int getDay()
    {
        return (int) (schedule.getTime() / stepsPerDay);
    }

    public int getYear()
    {
        return (int) getYear(schedule.getTime());
    }

    public double getYear(double scheduleTime)
    {
        return  ((scheduleTime / stepsPerDay) / 365);
    }

    public int getHour() {
        return (int) (schedule.getTime() % stepsPerDay * 24 / stepsPerDay);
    }

    public Stoppable scheduleEveryYear(Steppable steppable, StepOrder order)
    {
        if(order.isToRandomize())
            return schedule.scheduleRepeating(steppable,order.ordinal(),365* stepsPerDay);
        else
            return aggregateYearlySteppables.get(order).add(steppable);
    }

    public Stoppable scheduleEveryStep(Steppable steppable, StepOrder order)
    {
        return schedule.scheduleRepeating(steppable,order.ordinal(),1.0);
    }

    public void scheduleOnce(Steppable steppable, StepOrder order)
    {
        schedule.scheduleOnce(steppable,order.ordinal());
    }

    public Stoppable scheduleEveryDay(Steppable steppable, StepOrder order)
    {
        if(order.isToRandomize())
            return schedule.scheduleRepeating(steppable,order.ordinal(), stepsPerDay);
        else
            return aggregateDailySteppables.get(order).add(steppable);
    }



    public Stoppable scheduleEveryXDay(Steppable steppable, StepOrder order, int periodInDays)
    {
        return schedule.scheduleRepeating(steppable,order.ordinal(), stepsPerDay * periodInDays);
    }


    /**
     * after x days pass step this object once and forget it
     * @param steppable steppable
     * @param order order at which to step it
     * @param daysFromNow how many days from now
     */
    public void scheduleOnceInXDays(Steppable steppable, StepOrder order, int daysFromNow)
    {
        schedule.scheduleOnceIn(stepsPerDay*daysFromNow,steppable,order.ordinal());
    }


    /**
     * will step this object only once when the specific year starts. If that year is in the past, it won't step.
     * Implementation wise unfortunately I just check every year to see whether to step this or not. It's quite silly.
     * @param steppable the action to step
     * @param order what order should it be stepped on
     * @param year what year should this happen.
     */
    public Stoppable scheduleOnceAtTheBeginningOfYear(Steppable steppable,StepOrder order, int year)
    {

        final Steppable container = new Steppable() {
            @Override
            public void step(SimState simState) {
                //the plus one is because when this is stepped it's the 365th day
                if(((FishState) simState).getYear()+1 == year) {
                    steppable.step(simState);
                }

            }
        };
        return scheduleEveryYear(container,order);

    }

    public Stoppable schedulePerPolicy(Steppable steppable, StepOrder order, IntervalPolicy policy)
    {
        switch (policy){
            case EVERY_STEP:
                return scheduleEveryStep(steppable,order);
            case EVERY_DAY:
                return scheduleEveryDay(steppable,order);
            case EVERY_MONTH:
                return scheduleEveryXDay(steppable,order,30);
            case EVERY_YEAR:
                return scheduleEveryYear(steppable, order);
            default:
                Preconditions.checkState(false,"Reset Policy not found");
        }

        return null;
    }

    public double getTotalBiomass(Species species)
    {
        return map.getTotalBiology(species);
    }

    public double getTotalAbundance(Species species,int bin)
    {
        return
                map.getAllSeaTilesExcludingLandAsList().stream().filter(
                        new Predicate<SeaTile>() {
                            @Override
                            public boolean test(SeaTile seaTile) {
                                return ! (seaTile.getBiology() instanceof EmptyLocalBiology);
                            }
                        }
                ).
                        mapToDouble(
                                new ToDoubleFunction<SeaTile>() {
                                    @Override
                                    public double applyAsDouble(SeaTile value) {
                                        return value.getAbundance(species).getAbundanceInBin(bin);
                                    }
                                }
                        ).sum();
    }


    public double getTotalAbundance(Species species,int subdivision, int bin)
    {
        double sum = 0;
        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            if(seaTile.isFishingEvenPossibleHere())
                sum += seaTile.getAbundance(species).getAbundance(subdivision,bin);
        }
        return sum;
    }

    /**
     * if the model hasn't started, register this object to be started when the model is. Otherwise start it now
     * @param startable the object to start
     */
    public void registerStartable(Startable startable)
    {
        if(started) {
            startable.start(this);
            //  scheduleOnce((Steppable) simState -> startable.start(FishState.this), StepOrder.DAWN);
        }
        else
            toStart.add(startable);
    }

    /**
     * if the model hasn't started, register this object to be started when the model is. Otherwise start it now
     * @param startable the object to start
     */
    public void registerStartable(FisherStartable startable,Fisher fisher)
    {
        if(started)
            startable.start(this,fisher);
        else
            fisherStartables.add(new Pair<>(fisher,startable));
    }

    /**
     * record the fact that somebody fished somewhere
     * @param tile where it has been fished
     */
    public void recordFishing(SeaTile tile) {
        map.recordFishing(tile);
    }

    public IntGrid2D getDailyTrawlsMap() {
        return map.getDailyTrawlsMap();
    }

    public List<Market> getAllMarketsForThisSpecie(Species species)
    {
        //ports can share markets and we don't want to double count
        Set<Market> toAggregate = new HashSet<>();
        //now get for each port, its markets
        for (Port port : getPorts())
        {
            final Market market = port.getDefaultMarketMap().getMarket(species);
            if (market != null)
                toAggregate.add(market);
        }

        return new LinkedList<>(toAggregate);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public FishStateDailyTimeSeries getDailyDataSet() {
        return dailyDataSet;
    }

    public FishStateYearlyTimeSeries getYearlyDataSet() {
        return yearlyDataSet;
    }

    public String timeString()
    {
        return "Year: " + getYear() + " day: " + getDayOfTheYear() + " hour: " + getHour();
    }

    public int getStep() {
        return (int) Math.round(schedule.getTime());
    }

    public MersenneTwisterFast getRandom()
    {
        return random;
    }

    public SocialNetwork getSocialNetwork() {
        return socialNetwork;
    }

    public Double getLatestDailyObservation(String columnName)
    {
        return getDailyDataSet().getColumn(columnName).getLatest();
    }
    public Double getLatestYearlyObservation(String columnName)
    {
        return getYearlyDataSet().getColumn(columnName).getLatest();
    }


    public Double getAverageYearlyObservation(String columnName)
    {

        DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
        for(Double observation : getYearlyDataSet().getColumn(columnName))
            statistics.accept(observation.doubleValue());

        return statistics.getAverage();
    }
    /**
     *
     * @return true if the model has a fisher factory it can use to create more fishers
     */
    public boolean canCreateMoreFishers()
    {
        return fisherFactory!=null && !fisherFactory.isEmpty();
    }

    /**
     * called usually by GUI. Call this after the scenario has started not before!
     * @return
     */
    public Fisher createFisher(String nameOfPopulation)
    {
        Preconditions.checkState(canCreateMoreFishers());
        Fisher newborn = fisherFactory.get(nameOfPopulation).buildFisher(this);
        getFishers().add(newborn);
        getSocialNetwork().addFisher(newborn,this);
        registerStartable(newborn);
        return newborn;
    }


    /**
     * Getter for property 'fisherFactory'.
     *
     * @return Value for property 'fisherFactory'.
     */
    public FisherFactory getFisherFactory(String nameOfPopulation) {
        return fisherFactory.get(nameOfPopulation);


    }

    public Set<Map.Entry<String, FisherFactory>> getFisherFactories(){
        return  fisherFactory.entrySet();

    }

    public void killRandomFisher()
    {
        Preconditions.checkState(fishers.size()>0, "There are no more fishers left to kill");
        Fisher sacrifice = fishers.remove(random.nextInt(fishers.size()));
        killSpecificFisher(sacrifice);

    }

    public void killSpecificFisher(Fisher sacrifice)
    {
        sacrifice.turnOff();
        map.getFisherGrid().setObjectLocation(sacrifice,-1,-1);
        fishers.remove(sacrifice);

    }


    public int getNumberOfFishers(){
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
    public void attachAdditionalGatherers()
    {
        //keep track of average X location at the end of the year
        DataColumn dailyX = this.dailyDataSet.registerGatherer("Average X Towed", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState state) {
                double sum = 0;
                double observations = 0;
                for (Fisher fisher : getFishers()) {
                    TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                    if(lastFinishedTrip != null) {
                        SeaTile mostFishedTileInTrip = lastFinishedTrip.getMostFishedTileInTrip();
                        if (mostFishedTileInTrip != null) {
                            sum += mostFishedTileInTrip.getGridX();
                            observations++;
                        }
                    }
                }
                if (observations == 0)
                    return Double.NaN;
                return sum / observations;
            }
        }, Double.NaN);




        //yearly you account for the average
        this.yearlyDataSet.registerGatherer("Average X Towed",
                                            FishStateUtilities.generateYearlyAverage(dailyX),
                                            Double.NaN)
        ;


        //keep track of average X location at the end of the year
        this.dailyDataSet.registerGatherer("Average Distance From Port",
                                           new Gatherer<FishState>() {
                                               @Override
                                               public Double apply(FishState state) {
                                                   double sum = 0;
                                                   double observations = 0;
                                                   for (Fisher fisher : getFishers()) {
                                                       TripRecord lastFinishedTrip = fisher.getLastFinishedTrip();
                                                       if(lastFinishedTrip != null) {
                                                           SeaTile mostFishedTileInTrip = lastFinishedTrip.getMostFishedTileInTrip();
                                                           if (mostFishedTileInTrip != null) {
                                                               sum +=
                                                                       map.distance(fisher.getHomePort().getLocation(),
                                                                                    mostFishedTileInTrip);
                                                               observations++;
                                                           }
                                                       }
                                                   }
                                                   if (observations == 0)
                                                       return Double.NaN;
                                                   return sum / observations;
                                               }
                                           }, Double.NaN);


        this.dailyDataSet.registerGatherer("% of Tows on the Line",
                                           new Gatherer<FishState>() {
                                               @Override
                                               public Double apply(FishState state) {

                                                   double trawlsSum = 0;
                                                   double lineSum = 0;
                                                   NauticalMap map = state.getMap();
                                                   for(SeaTile tile : map.getAllSeaTilesExcludingLandAsList())
                                                   {
                                                       int trawlsHere = map.getDailyTrawlsMap().get(tile.getGridX(),
                                                                                                    tile.getGridY());
                                                       trawlsSum += trawlsHere;
                                                       if(map.getTilesOnTheMPALine().contains(tile))
                                                       {
                                                           lineSum +=trawlsHere;
                                                       }
                                                   }
                                                   if(trawlsSum == 0)
                                                       return Double.NaN;
                                                   assert trawlsSum>=lineSum;
                                                   return lineSum/trawlsSum;

                                               }
                                           }
                , Double.NaN);


        this.yearlyDataSet.registerGatherer("Mileage-Catch Correlation",
                                            new Gatherer<FishState>() {
                                                @Override
                                                public Double apply(FishState state) {

                                                    LinkedList<Double> mileage = new LinkedList<>();
                                                    LinkedList<Double> catches = new LinkedList<>();

                                                    Species first = biology.getSpecie(0);

                                                    for(Fisher fisher : fishers)
                                                    {
                                                        Gear gear = fisher.getGear();
                                                        if(gear instanceof RandomCatchabilityTrawl)
                                                        {
                                                            mileage.add(((RandomCatchabilityTrawl) gear).getGasPerHourFished());
                                                            catches.add(fisher.getLatestYearlyObservation(first.getName() + " Landings"));
                                                        }
                                                    }

                                                    if(mileage.size()>0)
                                                        return FishStateUtilities.computeCorrelation(
                                                                Doubles.toArray(mileage),
                                                                Doubles.toArray(catches)
                                                        );
                                                    else
                                                        return Double.NaN;



                                                }
                                            }
                , Double.NaN);
    }


    public Bag getFishersAtLocation(int x, int y) {
        return map.getFishersAtLocation(x, y);
    }

    public Bag getFishersAtLocation(SeaTile tile) {
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
        if(fishers!=null) {
            for (Fisher fisher : fishers)
                fisher.turnOff();
            fishers.clear();

        }
        yearlyDataSet.turnOff();
        yearlyCounter.turnOff();
        dailyCounter.turnOff();
        dailyDataSet.turnOff();
        if(map!=null)
            map.turnOff();
        aggregateYearlySteppables.clear();
        aggregateDailySteppables.clear();

    }
}

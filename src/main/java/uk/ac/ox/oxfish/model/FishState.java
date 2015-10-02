package uk.ac.ox.oxfish.model;

import com.google.common.base.Preconditions;
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
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFishStateTimeSeries;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.scenario.GeneticLocationScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioEssentials;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.utility.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 *
 * The main model object. Like all the other simstates it holds the reference
 * to schedule and randomizer
 * Created by carrknight on 3/29/15.
 */
public class FishState  extends SimState{


    private NauticalMap map;

    private GlobalBiology biology;

    private ObservableList<Fisher> fishers;

    private List<Startable> toStart;

    private final FishStateDailyTimeSeries dailyDataSet = new FishStateDailyTimeSeries();

    private final YearlyFishStateTimeSeries yearlyDataSet = new YearlyFishStateTimeSeries(dailyDataSet);


    private final List<Pair<Fisher,FisherStartable>> fisherStartables = new LinkedList<>();


    private Scenario scenario = new GeneticLocationScenario();

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
     * aggregate steppables for phases where there is no need for randomization
     */
    Map<StepOrder,AggregateSteppable> aggregateYearlySteppables = new EnumMap<>(StepOrder.class);
    Map<StepOrder,AggregateSteppable> aggregateDailySteppables = new EnumMap<>(StepOrder.class);

    public int getStepsPerDay() {
        return stepsPerDay;
    }

    /**
     * how many hours in a step, basically.
     */
    public double getHoursPerStep() {
        return 24.0/(double) stepsPerDay;
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
        super.start();

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


        final ScenarioPopulation scenarioPopulation = scenario.populateModel(this);
        fishers = FXCollections.observableList(scenarioPopulation.getPopulation());
        socialNetwork = scenarioPopulation.getNetwork();
        socialNetwork.populate(this);

        map.start(this);
        //start the fishers
        for(Fisher fisher : fishers)
            fisher.start(this);
        //start the markets (for each port
        for(Port port : getPorts())
            for(Market market : port.getMarketMap().getMarkets())
                market.start(this);

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
    public HashSet<Port> getPorts() {
        return map.getPorts();
    }

    /**
     *
     * @return an unmodifiable list of all the species available
     */
    public List<Specie> getSpecies() {
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

    public GeomVectorField getCities() {
        return map.getCities();
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

    public double getDay()
    {
        return (schedule.getTime() / stepsPerDay);
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

    public Stoppable schedulePerPolicy(Steppable steppable, StepOrder order, IntervalPolicy policy)
    {
        switch (policy){
            case EVERY_STEP:
                return scheduleEveryStep(steppable,order);
            case EVERY_DAY:
                return scheduleEveryDay(steppable,order);
            case EVERY_YEAR:
                return scheduleEveryYear(steppable, order);
            default:
                Preconditions.checkState(false,"Reset Policy not found");
        }
        return null;
    }

    public double getTotalBiomass(Specie specie)
    {
        return map.getTotalBiology(specie);
    }

    /**
     * if the model hasn't started, register this object to be started when the model is. Otherwise start it now
     * @param startable the object to start
     */
    public void registerStartable(Startable startable)
    {
        if(started)
            startable.start(this);
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

    public List<Market> getAllMarketsForThisSpecie(Specie specie)
    {
        List<Market> toAggregate = new LinkedList<>();
        //now get for each port, its markets
        for (Port port : getPorts())
        {
            final Market market = port.getMarket(specie);
            if (market != null)
                toAggregate.add(market);
        }

        return toAggregate;
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

    public YearlyFishStateTimeSeries getYearlyDataSet() {
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

}

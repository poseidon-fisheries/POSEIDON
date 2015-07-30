package uk.ac.ox.oxfish.model;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.FishStateYearlyDataSet;
import uk.ac.ox.oxfish.model.data.IntervalPolicy;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * The main model object. Like all the other simstates it holds the reference
 * to schedule and randomizer
 * Created by carrknight on 3/29/15.
 */
public class FishState  extends SimState{


    private NauticalMap map;

    private GlobalBiology biology;

    private List<Fisher> fishers;

    private List<Startable> toStart;

    private final FishStateDailyDataSet dailyDataSet = new FishStateDailyDataSet();

    private final FishStateYearlyDataSet yearlyDataSet = new FishStateYearlyDataSet(dailyDataSet);




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
     * @param seed the random seed
     */
    public FishState(long seed) {
        this(seed,1);
    }

    public FishState(long seed, int stepsPerDay)
    {
        super(seed);
        this.stepsPerDay = stepsPerDay;
        toStart = new LinkedList<>();

    }



    /**
     * so far it does the following:
     *  * read in the data into a the raster
     */
    @Override
    public void start() {
        super.start();

        ScenarioEssentials initialization = scenario.start(this);

        //read raster bathymetry
        //  map = NauticalMap.initializeWithDefaultValues();
        map = initialization.getMap();
        //      map.addCities("cities/cities.shp");

        biology = initialization.getBiology();


        final ScenarioPopulation scenarioPopulation = scenario.populateModel(this);
        fishers = scenarioPopulation.getPopulation();
        socialNetwork = scenarioPopulation.getNetwork();
        socialNetwork.populate(this);

        map.start(this);
        //start the fishers
        for(Fisher fisher : fishers)
                fisher.start(this);
        //start the markets (for each port
        for(Port port : getPorts())
            for(Market market : port.getMarkets().getMarkets())
                market.start(this);

        for(Startable startable : toStart)
                startable.start(this);
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

    public List<Fisher> getFishers() {
        return fishers;
    }

    public SparseGrid2D getFisherGrid() {
        return map.getFisherGrid();
    }

    public GlobalBiology getBiology() {
        return biology;
    }

    public void setRegulationsForAllFishers(Regulation regulation)
    {
        for(Fisher fisher : fishers)
            fisher.setRegulation(regulation);
    }



    /**
     * what day of the year (from 1 to 365) is this?
     * @return the day of the year
     */
    public int getDayOfTheYear()
    {
        return (int) ((schedule.getTime() / stepsPerDay) % 365) + 1;
    }

    public double getDay(double scheduleTime)
    {
        return (scheduleTime / stepsPerDay);
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
        return schedule.scheduleRepeating(steppable,order.ordinal(),365* stepsPerDay);
    }

    public Stoppable scheduleEveryStep(Steppable steppable, StepOrder order)
    {
        return schedule.scheduleRepeating(steppable,order.ordinal(),1.0);
    }

    public Stoppable scheduleEveryDay(Steppable steppable, StepOrder order)
    {
        return schedule.scheduleRepeating(steppable,order.ordinal(), stepsPerDay);
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
     * record the fact that somebody fished somewhere
     * @param tile where it has been fished
     */
    public void recordFishing(SeaTile tile) {
        map.recordFishing(tile);
    }

    public DoubleGrid2D getFishedMap() {
        return map.getFishedMap();
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

    public FishStateDailyDataSet getDailyDataSet() {
        return dailyDataSet;
    }

    public FishStateYearlyDataSet getYearlyDataSet() {
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

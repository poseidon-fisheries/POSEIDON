package uk.ac.ox.oxfish.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.regs.Regulations;
import uk.ac.ox.oxfish.model.scenario.*;

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




    private Scenario scenario = new GeneticLocationScenario();

    private boolean started = false;

    /**
     * x steps equal 1 day
     */
    final public static int STEPS_PER_DAY = 1;
    /**
     * how many hours in a step, basically.
     */
    final public static double HOURS_AVAILABLE_TO_TRAVEL_EACH_STEP = 24.0/(double)STEPS_PER_DAY;


    public FishState(long seed) {
        super(seed);
        toStart = new LinkedList<>();
    }


    /**
     * so far it does the following:
     *  * read in the data into a the raster
     */
    @Override
    public void start() {
        super.start();

        ScenarioResult initialization = scenario.start(this);

        //read raster bathymetry
        //  map = NauticalMap.initializeWithDefaultValues();
        map = initialization.getMap();
        //      map.addCities("cities/cities.shp");

        biology = initialization.getBiology();

        fishers = initialization.getAgents();


        map.start(this);
        //start the fishers
        for(Fisher fisher : fishers)
                fisher.start(this);
        //start the markets (for each port
        for(Port port : map.getPorts())
            for(Market market : port.getMarkets().asList())
                market.start(this);

        for(Startable startable : toStart)
                startable.start(this);
        started=true;








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

    public void setRegulationsForAllFishers(Regulations regulations)
    {
        for(Fisher fisher : fishers)
            fisher.setRegulations(regulations);
    }



    /**
     * what day of the year (from 1 to 365) is this?
     * @return the day of the year
     */
    public int getDayOfTheYear()
    {
        return (int) ((schedule.getSteps() / STEPS_PER_DAY) % 365) + 1;
    }

    public int getYear()
    {
        return (int) ((schedule.getSteps() / STEPS_PER_DAY) / 365);
    }

    public Stoppable scheduleEveryYear(Steppable steppable, StepOrder order)
    {
        return schedule.scheduleRepeating(steppable,order.ordinal(),365*STEPS_PER_DAY);
    }

    public Stoppable scheduleEveryStep(Steppable steppable, StepOrder order)
    {
        return schedule.scheduleRepeating(steppable,order.ordinal(),1.0);
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

    public IntGrid2D getFishedMap() {
        return map.getFishedMap();
    }
}

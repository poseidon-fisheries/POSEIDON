package uk.ac.ox.oxfish.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.SparseGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;

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


    private Scenario scenario = new PrototypeScenario();
    {
        //todo delete this!
        ((PrototypeScenario)scenario).setFishers(1);
    }
    /**
     * how many hours in a step, basically.
     */
    final public static double HOURS_AVAILABLE_TO_TRAVEL_EACH_STEP = 24;


    public FishState(long seed) {
        super(seed);
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
        //start the fishers
        for(Fisher fisher : fishers)
                fisher.start(this);





        //schedule to print repeatedly the day
        schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState simState) {
                System.out.println("the time is " + simState.schedule.getTime());
            }
        });




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
}

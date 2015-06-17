package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.YearlyFisherDataSet;

/**
 * A random hill-climber, every year randomly shocking its favorite destination and comparing it with the previous one
 * Created by carrknight on 6/17/15.
 */
public class HillClimberDestinationStrategy implements DestinationStrategy, Steppable
{


    /**
     * this strategy works by modifying the "favorite" destination of its delegate
     */
    private final FavoriteDestinationStrategy delegate;

    /**
     * the previous location tried
     */
    private SeaTile previousLocation = null;

    /**
     * the changes in cash when we tried the previous location
     */
    private double previousYearCashFlow =  Double.NaN;

    /**
     * when we randomly choose a new sea-tile, how far (in grid terms) do we look?
     */
    private int maxStepSize = 5;

    /**
     * how many random attempts we make to find a new sea-tile before giving up (failures are things like choosing a land
     * sea-tile, a port or similar)
     */
    private int attempts = 20;

    /**
     * this gets set when chooseDestination is called the first time. I am assuming that it's called more than once a year
     */
    private Fisher fisher;

    private final NauticalMap map;

    private final MersenneTwisterFast random;

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {
        delegate.start(model);
        model.scheduleEveryYear(this, StepOrder.AFTER_DATA);
    }

    public HillClimberDestinationStrategy(
            FavoriteDestinationStrategy delegate, NauticalMap map, MersenneTwisterFast random)
    {
        this.delegate = delegate;
        this.map = map;
        this.random = random;
    }


    public HillClimberDestinationStrategy(
            NauticalMap map, MersenneTwisterFast random)
    {
        this.delegate = new FavoriteDestinationStrategy(map,random);
        this.random = random;
        this.map = map;
    }

    public HillClimberDestinationStrategy(SeaTile tile, NauticalMap map, MersenneTwisterFast random)
    {
        this.delegate = new FavoriteDestinationStrategy(tile);
        this.map = map;
        this.random = random;
    }

    /**
     * tell the startable to turnoff
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    /**
     * decides where to go.
     *
     * @param fisher        the agent that needs to choose
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        if(this.fisher == null)
            this.fisher = fisher;
        assert this.fisher == fisher : "the hill-climber is a personal strategy and should not be shared";

        return delegate.chooseDestination(fisher,random,model,currentAction);
    }


    private SeaTile randomStep()
    {
        assert  fisher != null; //can't be called otherwise

        SeaTile current = delegate.getFavoriteSpot();
        for(int i=0; i<attempts; i++)
        {
            int x = current.getGridX() + (random.nextBoolean() ? random.nextInt(maxStepSize+1) : -random.nextInt(maxStepSize+1));
            int y = current.getGridY() + (random.nextBoolean() ? random.nextInt(maxStepSize+1) : -random.nextInt(maxStepSize+1));
            SeaTile candidate = map.getSeaTile(x,y);
            if(candidate != null && candidate.getAltitude() < 0 && !fisher.getHomePort().getLocation().equals(candidate))
                return candidate;
        }

        //stay where you are
        return current;

    }

    @Override
    public void step(SimState simState) {
        if(fisher == null)
        {
            assert false : "It seems impossible never to call a chooseDestination in a full year, but it might be possible" +
                    "at some later date. Anyway no way we can optimize a ship that stays at port";
            return;
        }

        SeaTile current = delegate.getFavoriteSpot();
        final double currentCashFlow = fisher.getLatestYearlyObservation(YearlyFisherDataSet.CASH_FLOW_COLUMN);
        assert current != null;
        assert Double.isFinite(currentCashFlow);




        //if you didn't move before (or you are stuck) you don't have a gradient yet so just try a new step
        if(previousLocation == null || Double.isNaN(previousYearCashFlow) || previousLocation== current )
        {
            SeaTile candidate = randomStep();
            //it is a valid candidate because the check is done by randomStep(.)
            delegate.setFavoriteSpot(candidate);

        }

        //not your first step and the step is meaningful
        else {
            //was it (strictly) better before?
            if(currentCashFlow<previousYearCashFlow)
            {
                //go back!
                delegate.setFavoriteSpot(previousLocation);

            }
            //otherwise randomize from here
            SeaTile candidate = randomStep();
            //it is a valid candidate because the check is done by randomStep(.)
            delegate.setFavoriteSpot(candidate);

        }

        //record
        previousLocation = current;
        previousYearCashFlow = currentCashFlow;








    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getMaxStepSize() {
        return maxStepSize;
    }

    public void setMaxStepSize(int maxStepSize) {
        this.maxStepSize = maxStepSize;
    }

    public SeaTile getPreviousLocation() {
        return previousLocation;
    }

    public double getPreviousYearCashFlow() {
        return previousYearCashFlow;
    }
}

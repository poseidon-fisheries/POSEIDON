package uk.ac.ox.oxfish.fisher.strategies.destination;

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
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;
import uk.ac.ox.oxfish.utility.maximization.IterativeMovement;

/**
 * A strategy that every year iteratively tries a new sea-patch to fish on. It uses net cash-flow as a fitness value
 * to decide whether the new sea-patch is better than the one before
 * Created by carrknight on 6/17/15.
 */
public class YearlyIterativeDestinationStrategy implements DestinationStrategy, Steppable
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


    private IterativeMovement algorithm;

    /**
     * this gets set when chooseDestination is called the first time. I am assuming that it's called more than once a year
     */
    private Fisher fisher;


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

    public YearlyIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate, NauticalMap map, MersenneTwisterFast random)
    {
        this(delegate,new HillClimbingMovement(map,random));
    }

    public YearlyIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate, IterativeMovement algorithm)
    {
        this.delegate = delegate;
        this.algorithm = algorithm;
    }

    public YearlyIterativeDestinationStrategy(
            NauticalMap map, MersenneTwisterFast random)
    {
        this(new FavoriteDestinationStrategy(map,random),map,random);

    }

    public YearlyIterativeDestinationStrategy(SeaTile tile, NauticalMap map, MersenneTwisterFast random)
    {
        this(new FavoriteDestinationStrategy(tile),map,random);

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
        assert this.fisher == fisher : "YearlyIterativeDestinationStrategy is a personal strategy and should not be shared";

        return delegate.chooseDestination(fisher,random,model,currentAction);
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

        //adapt!
        delegate.setFavoriteSpot(algorithm.adapt(previousLocation,current,previousYearCashFlow,currentCashFlow));

        //record
        previousLocation = current;
        previousYearCashFlow = currentCashFlow;








    }


    public IterativeMovement getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(IterativeMovement algorithm) {
        this.algorithm = algorithm;
    }

    public SeaTile getPreviousLocation() {
        return previousLocation;
    }

    public double getPreviousYearCashFlow() {
        return previousYearCashFlow;
    }
}

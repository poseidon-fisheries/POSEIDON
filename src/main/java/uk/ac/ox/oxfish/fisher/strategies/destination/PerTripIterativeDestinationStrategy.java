package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.maximization.HillClimbingMovement;
import uk.ac.ox.oxfish.utility.maximization.IterativeMovement;

import java.util.LinkedList;
import java.util.List;

import static com.esotericsoftware.minlog.Log.trace;

/**
 * Like the YearlyIterativeDestinationStrategy except that rather than doing it every
 * year this is done every x trips (x=1 by default). <br>
 *     In terms of code this strategy doesn't actually step but instead listen to the fisher for
 *     new trips
 * Created by carrknight on 6/19/15.
 */
public class PerTripIterativeDestinationStrategy implements DestinationStrategy,TripListener {


    /**
     * how many trips before we make our decision
     */
    private int  tripsPerDecision = 1;

    /**
     * what was the average profit per day for the previous sea-tile?
     */
    private double previousProfits = Double.NaN;

    /**
     * where did you try and fish earlier on?
     */
    private SeaTile previousLocation = null;

    /**
     * should we not study trips that were cut short?
     */
    private boolean ignoreFailedTrips = false;

    /**
     * a record of current trips taken.
     */
    final private List<TripRecord> recordedTrips = new LinkedList<>();

    private IterativeMovement algorithm;

    /**
     * this strategy works by modifying the "favorite" destination of its delegate
     */
    private final FavoriteDestinationStrategy delegate;

    /**
     * fisher I am listening to
     */
    private Fisher fisher;


    public PerTripIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate, IterativeMovement algorithm) {
        this.delegate = delegate;
        this.algorithm = algorithm;
    }

    public PerTripIterativeDestinationStrategy(
            NauticalMap map, MersenneTwisterFast random)
    {
        this(new FavoriteDestinationStrategy(map,random),new HillClimbingMovement(map,random));

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
        if(fisher!=null)
            fisher.removeTripListener(this);
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model)
    {
        delegate.start(model);
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
        {
            this.fisher = fisher;
            //and start listening!
            fisher.addTripListener(this);
        }
        else
        {
            Preconditions.checkArgument(fisher==this.fisher);
        }
        return delegate.chooseDestination(fisher,random,model,currentAction);
    }

    public IterativeMovement getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(IterativeMovement algorithm) {
        this.algorithm = algorithm;
    }


    @Override
    public void reactToFinishedTrip(TripRecord record) {
        assert record.isCompleted();
        //was the trip cancelled?
        if(ignoreFailedTrips && record.isCutShort())
            //if so, ignore it
            return;

        recordedTrips.add(record);
        //if you have enough trips, time to try a new spot!
        if(recordedTrips.size()>=tripsPerDecision)
        {
            assert recordedTrips.size() == tripsPerDecision;
            SeaTile current = delegate.getFavoriteSpot();
            //find average profits per step per trip
            double currentProfits = recordedTrips.stream().
                    mapToDouble(TripRecord::getProfitPerStep).sum();
            currentProfits /= recordedTrips.size();

            //log
            trace(this.toString(),"current profit: " + currentProfits + ", previous profits: " + previousProfits);

            delegate.setFavoriteSpot(
                    algorithm.adapt(previousLocation,current,previousProfits,currentProfits)
            );
            trace(this.toString(),"current location " +current + ", new location, " + delegate.getFavoriteSpot() + ", previous location: " + previousLocation );


            previousLocation = current;
            previousProfits = currentProfits;
            recordedTrips.clear();
        }
    }

    /**
     * forces the strategy to choose a different destination. will throw an exception if the fisher is not at port
     * @param newDestination new destination to go to
     */
    public void forceDestination(SeaTile newDestination)
    {
        Preconditions.checkState(fisher.isAtPort(), "Changing destination out of port might ruin Trip Record");
        Preconditions.checkState(recordedTrips.isEmpty(), "Changing destination while some record trips exist is not something I planned for");
        delegate.setFavoriteSpot(newDestination);
    }

    public int getTripsPerDecision() {
        return tripsPerDecision;
    }

    public void setTripsPerDecision(int tripsPerDecision) {
        this.tripsPerDecision = tripsPerDecision;
    }

    public boolean isIgnoreFailedTrips() {
        return ignoreFailedTrips;
    }

    public void setIgnoreFailedTrips(boolean ignoreFailedTrips) {
        this.ignoreFailedTrips = ignoreFailedTrips;
    }

    public double getPreviousProfits() {
        return previousProfits;
    }

    public SeaTile getPreviousLocation() {
        return previousLocation;
    }

    /**
     * How many trips are recorded for the current location
     */
    public int tripsCurrentlyInMemory() {
        return recordedTrips.size();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fisher", fisher)
                .toString();
    }
}

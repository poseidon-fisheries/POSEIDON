package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.*;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Like the YearlyIterativeDestinationStrategy except that rather than doing it every
 * year this is done every trip <br>
 *     In terms of code this strategy doesn't actually step but instead listen to the fisher for
 *     new trips
 * Created by carrknight on 6/19/15.
 */
public class PerTripIterativeDestinationStrategy implements DestinationStrategy {






    /**
     * should we not study trips that were cut short?
     */
    private boolean ignoreFailedTrips = false;


    private final Adaptation<SeaTile> algorithm;

    /**
     * this strategy works by modifying the "favorite" destination of its delegate
     */
    private final FavoriteDestinationStrategy delegate;

    /**
     * fisher I am listening to
     */
    private Fisher fisher;


    public PerTripIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            AdaptationAlgorithm<SeaTile> algorithm,
            AdaptationProbability probability)
    {
        this.delegate = delegate;
        this.algorithm = new Adaptation<SeaTile>(
                fisher -> !(ignoreFailedTrips && fisher.getLastFinishedTrip().isCutShort()),


                algorithm,
                (fisher, change, model) -> {
                    if (change.getAltitude() < 0) //ignores "go to land" commands
                        delegate.setFavoriteSpot(change);
                },
                fisher1 -> {
                    if (fisher1 == fisher) //if we are sensing ourselves
                        //override to delegate
                        return delegate.getFavoriteSpot();
                    else if (fisher1.getLastFinishedTrip() == null || !fisher1.getLastFinishedTrip().isCompleted() ||
                            fisher1.getLastFinishedTrip().getTilesFished().isEmpty())
                        return null;
                    else
                        return fisher1.getLastFinishedTrip().getTilesFished().iterator().next();
                },
                new HourlyProfitInTripFunction(),
                probability
        );

    }

    public PerTripIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            AdaptationAlgorithm<SeaTile> algorithm,
            double randomizationProbability,
            double imitationProbability) {
        this.delegate = delegate;
        this.algorithm = new Adaptation<SeaTile>(
                fisher -> !(ignoreFailedTrips && fisher.getLastFinishedTrip().isCutShort()),
                algorithm,
                (fisher, change, model) -> {
                    if(change.getAltitude() < 0) //ignores "go to land" commands
                        delegate.setFavoriteSpot(change);
                },
                fisher1 -> {
                    if(fisher1==fisher) //if we are sensing ourselves
                        //override to delegate
                        return delegate.getFavoriteSpot();
                    else
                    if(fisher1.getLastFinishedTrip() == null || !fisher1.getLastFinishedTrip().isCompleted() ||
                            fisher1.getLastFinishedTrip().getTilesFished().isEmpty())
                        return  null;
                    else
                        return fisher1.getLastFinishedTrip().getTilesFished().iterator().next();
                },
                new HourlyProfitInTripFunction(),randomizationProbability, imitationProbability);

    }



    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    /**
     * starts a per-trip adaptation
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);
        this.fisher=fisher;
        fisher.addPerTripAdaptation(algorithm);
    }



    /**
     * decides where to go.
     *
     * @param equipment
     * @param status
     * @param memory
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, MersenneTwisterFast random,
            FishState model,
            Action currentAction) {
        return delegate.chooseDestination(equipment, status, memory, random, model, currentAction);
    }


    public Adaptation<SeaTile> getAlgorithm() {
        return algorithm;
    }
}

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

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
            AdaptationProbability probability,
            final ObjectiveFunction<Fisher> objective)
    {
        this.delegate = delegate;
        this.algorithm = new ExploreImitateAdaptation<SeaTile>(
                new Predicate<Fisher>() {
                    @Override
                    public boolean test(Fisher fisher) {
                        return !(ignoreFailedTrips && fisher.getLastFinishedTrip().isCutShort());
                    }
                },


                algorithm,
                new Actuator<SeaTile>() {
                    @Override
                    public void apply(Fisher fisher, SeaTile change, FishState model) {
                        if (change.getAltitude() < 0) //ignores "go to land" commands
                            delegate.setFavoriteSpot(change);
                    }
                },
                new Sensor<SeaTile>() {
                    @Override
                    public SeaTile scan(Fisher fisher1) {
                        if (fisher1 == fisher) //if we are sensing ourselves
                            //override to delegate
                            return delegate.getFavoriteSpot();
                        else if (fisher1.getLastFinishedTrip() == null || !fisher1.getLastFinishedTrip().isCompleted() ||
                                fisher1.getLastFinishedTrip().getTilesFished().isEmpty())
                            return null;
                        else
                            return fisher1.getLastFinishedTrip().getTilesFished().iterator().next();
                    }
                },
                objective,
                probability
        );

    }


    public PerTripIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            Adaptation<SeaTile> adaptation)
    {
        this.delegate = delegate;
        this.algorithm = adaptation;

    }

    public PerTripIterativeDestinationStrategy(
            final FavoriteDestinationStrategy delegate,
            AdaptationAlgorithm<SeaTile> algorithm,
            double randomizationProbability,
            double imitationProbability, final HourlyProfitInTripObjective objective) {
        this.delegate = delegate;
        this.algorithm = new ExploreImitateAdaptation<SeaTile>(
                new Predicate<Fisher>() {

                    public boolean test(Fisher fisher) {
                        return !(ignoreFailedTrips && fisher.getLastFinishedTrip().isCutShort());
                    }
                },
                algorithm,
                new Actuator<SeaTile>() {
                    @Override
                    public void apply(Fisher fisher, SeaTile change, FishState model) {
                        if (change.getAltitude() < 0) //ignores "go to land" commands
                            delegate.setFavoriteSpot(change);
                    }
                },
                new Sensor<SeaTile>() {
                    @Override
                    public SeaTile scan(Fisher fisher1) {
                        if (fisher1 == fisher) //if we are sensing ourselves
                            //override to delegate
                            return delegate.getFavoriteSpot();
                        else if (fisher1.getLastFinishedTrip() == null || !fisher1.getLastFinishedTrip().isCompleted() ||
                                fisher1.getLastFinishedTrip().getTilesFished().isEmpty())
                            return null;
                        else
                            return fisher1.getLastFinishedTrip().getTilesFished().iterator().next();
                    }
                },
                objective, randomizationProbability, imitationProbability);


    }



    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
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
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random,
            FishState model,
            Action currentAction) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }


    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public FavoriteDestinationStrategy getDelegate() {
        return delegate;
    }

    public Adaptation<SeaTile> getAlgorithm() {
        return algorithm;
    }


    public SeaTile getFavoriteSpot() {
        return delegate.getFavoriteSpot();
    }
}

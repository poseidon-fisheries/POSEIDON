package uk.ac.ox.oxfish.utility.adaptation.probability;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;

import java.util.function.Function;

/**
 * Never imitates, but probability of exploring is 1 if last trip profits are below a threshold
 * Created by carrknight on 10/17/16.
 */
public class ThresholdExplorationProbability implements AdaptationProbability, TripListener {

    private boolean exploring = true;

    private final double multiplier;

    private FishState model;

    final private Function<FishState, Double> threshold;

    public ThresholdExplorationProbability(double multiplier, Function<FishState, Double> threshold) {
        this.multiplier = multiplier;
        this.threshold = threshold;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        fisher.addTripListener(this);
        fisher.getDailyData().registerGatherer("Exploration Probability",
                                               new Gatherer<Fisher>() {
                                                   @Override
                                                   public Double apply(Fisher fisher1) {
                                                       return exploring ? 1d : 0d;
                                                   }
                                               },
                                               Double.NaN);
        this.model = model;
    }

    @Override
    public void turnOff(Fisher fisher) {
        fisher.removeTripListener(this);
        this.model = null;
    }


    @Override
    public void reactToFinishedTrip(TripRecord record) {
        Double ourProfits = record.getProfitPerHour(true);
        Double toBeat = threshold.apply(model);
        double correctMultiplier = toBeat >=0 ? multiplier : 1d/multiplier;
        exploring = !Double.isFinite(toBeat) || ourProfits <= correctMultiplier * toBeat;

    }

    /**
     * get probability of exploring
     */
    @Override
    public double getExplorationProbability() {
        return exploring ? 1 : 0;
    }

    /**
     * never imitates
     */
    @Override
    public double getImitationProbability() {
        return 0;
    }

    /**
     * react to what the result of the exploration was and see if it changes your probabilities.
     *
     * @param previousFitness pre-exploration fitness
     * @param currentFitness  post-exploration fitness
     */
    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {

    }

    /**
     * Getter for property 'multiplier'.
     *
     * @return Value for property 'multiplier'.
     */
    public double getMultiplier() {
        return multiplier;
    }
}

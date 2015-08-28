package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.*;
import uk.ac.ox.oxfish.utility.adaptation.maximization.DefaultBeamHillClimbing;

/**
 * A strategy that every year iteratively tries a new sea-patch to fish on. It uses net cash-flow as a fitness value
 * to decide whether the new sea-patch is better than the one before
 * Created by carrknight on 6/17/15.
 */
public class YearlyIterativeDestinationStrategy implements DestinationStrategy
{


    /**
     * this strategy works by modifying the "favorite" destination of its delegate
     */
    private final FavoriteDestinationStrategy delegate;
    private final DefaultBeamHillClimbing adaptationAlgorithm;

    /**
     * the previous location tried
     */
    private SeaTile previousLocation = null;

    /**
     * the changes in cash when we tried the previous location
     */
    private double previousYearCashFlow =  Double.NaN;






    private final Adaptation<SeaTile> algorithm;


    /**
     * starts a per-trip adaptation
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model,fisher);
        fisher.addYearlyAdaptation(algorithm);
    }


    public YearlyIterativeDestinationStrategy(
            FavoriteDestinationStrategy delegate, int stepSize, int attempts)
    {
        this.delegate = delegate;
        adaptationAlgorithm = new DefaultBeamHillClimbing(stepSize, attempts);
        this.algorithm = new Adaptation<SeaTile>(
                fisher -> fisher.getDailyData().numberOfObservations() > 360,
                adaptationAlgorithm,
                (fisher, change, model) -> delegate.setFavoriteSpot(change),
                fisher -> delegate.getFavoriteSpot(),
                new CashFlowObjective(360),
                1d,0d);
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
     * @param equipment
     * @param status
     * @param memory
     * @param random        the randomizer. It probably comes from the agent but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the agent currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, MersenneTwisterFast random,
            FishState model,
            Action currentAction) {
        return delegate.chooseDestination(equipment, status,memory , random, model, currentAction);
    }


    public Adaptation<SeaTile> getAlgorithm() {
        return algorithm;
    }

    public void setMaxStep(int maxStep) {
        adaptationAlgorithm.setMaxStep(maxStep);
    }

    public int getMaxStep() {
        return adaptationAlgorithm.getMaxStep();
    }

    public int getAttempts() {
        return adaptationAlgorithm.getAttempts();
    }
}

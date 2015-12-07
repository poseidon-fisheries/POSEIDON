package uk.ac.ox.oxfish.utility.adaptation.maximization;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.function.Predicate;

/**
 * Pre-made hillclimber specifically for moving around maps
 * Created by carrknight on 8/7/15.
 */
public class DefaultBeamHillClimbing extends BeamHillClimbing<SeaTile> {


    private int maxStep;

    private final int attempts;

    public DefaultBeamHillClimbing(
            boolean copyAlwaysBest, Predicate<Pair<Double,Double>> unfriendPredicate, int maxStep, int attempts) {
        super(copyAlwaysBest, unfriendPredicate);
        this.maxStep = maxStep;
        this.attempts = attempts;
    }

    public DefaultBeamHillClimbing(int maxStep, int attempts)
    {
        this(DEFAULT_ALWAYS_COPY_BEST, DEFAULT_DYNAMIC_NETWORK, maxStep, attempts);
    }


    static public DefaultBeamHillClimbing  BeamHillClimbingWithUnfriending(boolean alwaysCopyBest,
                                                                           final double unfriendingThreshold,
                                                                           int maxSteps,int attempts)
    {
        Preconditions.checkArgument(unfriendingThreshold>=0, "Unfriending threshold should be above 0!");
        return new DefaultBeamHillClimbing(alwaysCopyBest,
                                           oldFitnessAndNew ->
                                                   unfriendingThreshold * oldFitnessAndNew.getFirst() >
                                                           oldFitnessAndNew.getSecond(),
                                           maxSteps,
                                           attempts);
    }

    @Override
    public SeaTile randomStep(
            FishState state, MersenneTwisterFast random, Fisher fisher, SeaTile current)
    {
        for(int i=0; i<attempts; i++)
        {
            int x = current.getGridX() + (random.nextBoolean() ? random.nextInt(maxStep+1) : -random.nextInt(maxStep+1));
            int y = current.getGridY() + (random.nextBoolean() ? random.nextInt(maxStep+1) : -random.nextInt(maxStep+1));
            SeaTile candidate = state.getMap().getSeaTile(x,y);
            if(candidate!=null && current!= candidate && candidate.getAltitude()<0
                    &&!fisher.getHomePort().getLocation().equals(candidate) )
                return candidate;
        }

        //stay where you are
        return current;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }
}

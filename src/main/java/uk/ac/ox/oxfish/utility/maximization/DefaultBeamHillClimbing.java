package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Pre-made hillclimber specifically for moving around maps
 * Created by carrknight on 8/7/15.
 */
public class DefaultBeamHillClimbing extends BeamHillClimbing<SeaTile> {


    private final int maxStep;

    private final int attempts;

    public DefaultBeamHillClimbing(int maxStep, int attempts)
    {
        this.maxStep = maxStep;
        this.attempts = attempts;
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
}

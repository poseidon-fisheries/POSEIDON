package uk.ac.ox.oxfish.utility.maximization;

import ec.util.MersenneTwisterFast;
import org.metawidget.inspector.annotation.UiHidden;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.function.Predicate;

/**
 * Hill climbing mechanism for general movement
 * Created by carrknight on 6/17/15.
 */
public class HillClimbingMovement implements IterativeMovement {

    private final NauticalMap map;

    @UiHidden
    private final MersenneTwisterFast random;

    /**
     * function defining whether a new candidate sea-tile is valid
     */
    private Predicate<SeaTile> validator = candidate -> candidate != null && candidate.getAltitude() < 0;

    /**
     * number of tries for a random step
     */
    private int attempts = 20;

    /**
     *  maximum step size
     */
    private int maxStepSize = 5;

    public HillClimbingMovement(NauticalMap map, MersenneTwisterFast random)
    {
        this.map = map;
        this.random = random;
    }

    private SeaTile randomStep(SeaTile current)
    {

        for(int i=0; i<attempts; i++)
        {
            int x = current.getGridX() + (random.nextBoolean() ? random.nextInt(maxStepSize+1) : -random.nextInt(maxStepSize+1));
            int y = current.getGridY() + (random.nextBoolean() ? random.nextInt(maxStepSize+1) : -random.nextInt(maxStepSize+1));
            SeaTile candidate = map.getSeaTile(x,y);
            // if(candidate != null && candidate.getAltitude() < 0 && !fisher.getHomePort().getLocation().equals(candidate))
            if(current!= candidate && validator.test(candidate))
                return candidate;
        }

        //stay where you are
        return current;

    }

    @Override
    public SeaTile adapt(
            SeaTile previous, SeaTile current, double previousFitness, double newFitness)
    {

        //if you didn't move before (or you are stuck) you don't have a gradient yet so just try a new step
        if(previous == null || Double.isNaN(previousFitness) || previous== current )


            return randomStep(current);


            //not your first step and the step is meaningful
        else

            //was it (strictly) better before?
            if(newFitness < previousFitness)
                //go back!
                return previous;
            else
                //it's better here
                //randomize from here then
                return randomStep(current);


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

    public Predicate<SeaTile> getValidator() {
        return validator;
    }

    public void setValidator(Predicate<SeaTile> validator) {
        this.validator = validator;
    }
}

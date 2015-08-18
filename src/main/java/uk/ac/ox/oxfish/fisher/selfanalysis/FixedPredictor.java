package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple dummy predictor that always predict the same value
 * Created by carrknight on 8/18/15.
 */
public class FixedPredictor implements Predictor {


    private final double fixedValue;

    public FixedPredictor(double fixedValue) {
        this.fixedValue = fixedValue;
    }

    /**
     * ask the predictor the expected value of the variable it is tracking
     *
     * @return the expected value
     */
    @Override
    public double predict() {

        return fixedValue;
    }

    /**
     * ask the predictor what is the probability the variable it is tracking is below a given level
     *
     * @param level the level
     * @return P(x < level)
     */
    @Override
    public double probabilityBelowThis(double level) {
        if(level >= fixedValue)
            return 1;
        else
            return 0;
    }

    @Override
    public void start(FishState model, Fisher fisher)
    {
        //ignored

    }

    @Override
    public void turnOff() {
        //ignored
    }
}

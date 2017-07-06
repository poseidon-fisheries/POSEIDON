package uk.ac.ox.oxfish.biology.complicated;

/**
 * Created by carrknight on 7/5/17.
 */
public class NoNoiseMaker implements NoiseMaker {
    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public Double get() {
        return 0d;
    }
}

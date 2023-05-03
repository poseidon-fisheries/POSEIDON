package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class FadZapperFactory implements AlgorithmFactory<FadZapper> {
    private DoubleParameter maxFadAge =
        new CalibratedParameter(100, 500, 150);
    private int minGridX = 20;

    public FadZapperFactory() {
    }

    public FadZapperFactory(final DoubleParameter maxFadAge, final int minGridX) {
        this.maxFadAge = maxFadAge;
        this.minGridX = minGridX;
    }

    public DoubleParameter getMaxFadAge() {
        return maxFadAge;
    }

    public void setMaxFadAge(final DoubleParameter maxFadAge) {
        this.maxFadAge = maxFadAge;
    }

    public int getMinGridX() {
        return minGridX;
    }

    public void setMinGridX(final int minGridX) {
        this.minGridX = minGridX;
    }

    @Override
    public FadZapper apply(final FishState fishState) {
        final double maxFadAge = this.maxFadAge.applyAsDouble(fishState.getRandom());
        return new FadZapper(fad ->
            fad.getLocation().getGridX() <= minGridX ||
                fishState.getStep() - fad.getStepDeployed() > maxFadAge
        );
    }
}

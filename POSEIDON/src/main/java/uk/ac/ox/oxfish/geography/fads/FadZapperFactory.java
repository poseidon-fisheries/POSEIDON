package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class FadZapperFactory implements AlgorithmFactory<FadZapper> {
    private DoubleParameter maxFadAge;
    private IntegerParameter minGridX;

    @SuppressWarnings("unused")
    public FadZapperFactory() {
    }

    public FadZapperFactory(final DoubleParameter maxFadAge, final IntegerParameter minGridX) {
        this.maxFadAge = maxFadAge;
        this.minGridX = minGridX;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getMaxFadAge() {
        return maxFadAge;
    }

    public void setMaxFadAge(final DoubleParameter maxFadAge) {
        this.maxFadAge = maxFadAge;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getMinGridX() {
        return minGridX;
    }

    @SuppressWarnings("unused")
    public void setMinGridX(final IntegerParameter minGridX) {
        this.minGridX = minGridX;
    }

    @Override
    public FadZapper apply(final FishState fishState) {
        final double maxFadAge = this.maxFadAge.applyAsDouble(fishState.getRandom());
        return new FadZapper(fad ->
            fad.getLocation().getGridX() <= minGridX.getValue() ||
                fishState.getStep() - fad.getStepDeployed() > maxFadAge
        );
    }
}

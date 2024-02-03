package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class LogNormalErrorOperatorFactory implements AlgorithmFactory<LogNormalErrorOperator> {

    private DoubleParameter mean;
    private DoubleParameter standardDeviation;

    public LogNormalErrorOperatorFactory() {
    }

    public LogNormalErrorOperatorFactory(final DoubleParameter mean, final DoubleParameter standardDeviation) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    public DoubleParameter getMean() {
        return mean;
    }

    public void setMean(final DoubleParameter mean) {
        this.mean = mean;
    }

    public DoubleParameter getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(final DoubleParameter standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Override
    public LogNormalErrorOperator apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new LogNormalErrorOperator(rng, mean.applyAsDouble(rng), standardDeviation.applyAsDouble(rng));
    }
}

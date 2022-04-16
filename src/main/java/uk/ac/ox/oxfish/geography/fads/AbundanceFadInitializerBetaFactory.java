package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.DoubleSupplier;

public class AbundanceFadInitializerBetaFactory extends AbstractAbundanceFadInitializerFactory {

    private DoubleParameter parameterAlpha = new FixedDoubleParameter(.1);
    private DoubleParameter parameterBeta = new FixedDoubleParameter(5);


    public AbundanceFadInitializerBetaFactory() {
//        this("Bigeye tuna", "Yellowfin tuna", "Skipjack tuna");
    }

    public AbundanceFadInitializerBetaFactory(String... speciesNames) {
        super(speciesNames);
    }

    @NotNull
    @Override
    protected DoubleSupplier buildCapacityGenerator(MersenneTwisterFast rng, double maximumCarryingCapacity) {

        BetaDistribution distribution = new BetaDistribution(
                new MTFApache(rng),
                parameterAlpha.apply(rng),
                parameterBeta.apply(rng)
        );

        return () -> distribution.sample()*maximumCarryingCapacity;
    }

    public DoubleParameter getParameterAlpha() {
        return parameterAlpha;
    }

    public void setParameterAlpha(DoubleParameter parameterAlpha) {
        this.parameterAlpha = parameterAlpha;
    }

    public DoubleParameter getParameterBeta() {
        return parameterBeta;
    }

    public void setParameterBeta(DoubleParameter parameterBeta) {
        this.parameterBeta = parameterBeta;
    }
}

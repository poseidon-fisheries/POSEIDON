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

    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);


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
        DoubleSupplier capacityGenerator;
        double probabilityOfFadBeingDud = fadDudRate.apply(rng);
        if(Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud ==0)
            capacityGenerator = () -> distribution.sample() * maximumCarryingCapacity;
        else
            capacityGenerator = () -> {
                if(rng.nextFloat()<= probabilityOfFadBeingDud)
                    return 0;
                else
                    return distribution.sample() * maximumCarryingCapacity;
            };


        return capacityGenerator;
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

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }
}
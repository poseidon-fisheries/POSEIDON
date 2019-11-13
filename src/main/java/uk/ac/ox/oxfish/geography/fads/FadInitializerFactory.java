package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;

public class FadInitializerFactory implements AlgorithmFactory<FadInitializer> {

    private DoubleParameter fadCarryingCapacityInKg = new FixedDoubleParameter(50000d);
    private DoubleParameter attractionRateInPercent = new FixedDoubleParameter(1e-3);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.1);

    @SuppressWarnings("unused")
    public DoubleParameter getFishReleaseProbabilityInPercent() { return fishReleaseProbabilityInPercent; }

    @SuppressWarnings("unused")
    public void setFishReleaseProbabilityInPercent(DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getAttractionRateInPercent() { return attractionRateInPercent; }

    @SuppressWarnings("unused")
    public void setAttractionRateInPercent(DoubleParameter attractionRateInPercent) { this.attractionRateInPercent = attractionRateInPercent; }

    @SuppressWarnings("unused")
    public DoubleParameter getFadCarryingCapacityInKg() { return fadCarryingCapacityInKg; }

    @SuppressWarnings("unused")
    public void setFadCarryingCapacityInKg(
        DoubleParameter fadCarryingCapacityInKg
    ) { this.fadCarryingCapacityInKg = fadCarryingCapacityInKg; }

    @Override public FadInitializer apply(FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();
        return new FadInitializer(
            getQuantity(fadCarryingCapacityInKg.apply(random), KILOGRAM),
            attractionRateInPercent.apply(random) / 100d,
            fishReleaseProbabilityInPercent.apply(random) / 100d
        );
    }

}

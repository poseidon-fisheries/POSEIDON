package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;

public class FadInitializerFactory implements AlgorithmFactory<FadInitializer> {

    private DoubleParameter attractionRateInPercent = new FixedDoubleParameter(1e-3);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.1);
    private Map<String, Double> carryingCapacities = new HashMap<>();

    public Map<String, Double> getCarryingCapacities() {
        return carryingCapacities;
    }

    public void setCarryingCapacities(Map<String, Double> carryingCapacities) {
        this.carryingCapacities = carryingCapacities;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    @SuppressWarnings("unused")
    public void setFishReleaseProbabilityInPercent(DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getAttractionRateInPercent() {
        return attractionRateInPercent;
    }

    @SuppressWarnings("unused")
    public void setAttractionRateInPercent(DoubleParameter attractionRateInPercent) {
        this.attractionRateInPercent = attractionRateInPercent;
    }

    @Override
    public FadInitializer apply(FishState fishState) {
        final MersenneTwisterFast random = fishState.getRandom();
        return new FadInitializer(
            fishState.getBiology(),
            carryingCapacities.entrySet().stream().collect(toImmutableMap(
                entry -> fishState.getBiology().getSpecie(entry.getKey()),
                entry -> getQuantity(entry.getValue(), KILOGRAM)
            )),
            attractionRateInPercent.apply(random) / 100d,
            fishReleaseProbabilityInPercent.apply(random) / 100d
        );
    }

}

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.*;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class WeibullPerSpeciesCarryingCapacitiesFactory
    implements uk.ac.ox.oxfish.geography.fads.CarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> {

    private Map<String, DoubleParameter> shapeParameters;
    private Map<String, DoubleParameter> scaleParameters;
    private Map<String, DoubleParameter> proportionOfZeros;
    private DoubleParameter capacityScalingFactor;
    private DoubleParameter shapeScalingFactor;

    @SuppressWarnings("unused")
    public WeibullPerSpeciesCarryingCapacitiesFactory() {
    }

    public WeibullPerSpeciesCarryingCapacitiesFactory(
        final Map<String, DoubleParameter> shapeParameters,
        final Map<String, DoubleParameter> scaleParameters,
        final Map<String, DoubleParameter> proportionOfZeros,
        final DoubleParameter capacityScalingFactor,
        final DoubleParameter shapeScalingFactor
    ) {
        this.shapeParameters = shapeParameters;
        this.scaleParameters = scaleParameters;
        this.proportionOfZeros = proportionOfZeros;
        this.capacityScalingFactor = capacityScalingFactor;
        this.shapeScalingFactor = shapeScalingFactor;
    }

    @Override
    public CarryingCapacityInitializer<PerSpeciesCarryingCapacity> apply(
        final FishState fishState
    ) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new PerSpeciesCarryingCapacityInitializer(
            fishState
                .getBiology()
                .getSpecies()
                .stream()
                .collect(toImmutableMap(
                    identity(),
                    species -> makeSpeciesCarryingCapacityParameter(species, rng)
                ))
        );
    }

    private DoubleParameter makeSpeciesCarryingCapacityParameter(
        final Species species,
        final MersenneTwisterFast rng
    ) {
        final String speciesName = species.getName();
        if (getScaleParameters().containsKey(speciesName) && getShapeParameters().containsKey(speciesName)) {
            return new ScaledDoubleParameter(
                new ZeroInflatedDoubleParameter(
                    new WeibullDoubleParameter(
                        getShapeParameters().get(speciesName).applyAsDouble(rng) *
                            getShapeScalingFactor().applyAsDouble(rng),
                        getScaleParameters().get(speciesName).applyAsDouble(rng)
                    ),
                    getProportionOfZeros().get(speciesName).applyAsDouble(rng)
                ),
                getCapacityScalingFactor().applyAsDouble(rng)
            );
        } else {
            return new FixedDoubleParameter(-1);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getScaleParameters() {
        return scaleParameters;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getShapeParameters() {
        return shapeParameters;
    }

    @SuppressWarnings("unused")
    public void setShapeParameters(final Map<String, DoubleParameter> shapeParameters) {
        this.shapeParameters = shapeParameters;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getShapeScalingFactor() {
        return shapeScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setShapeScalingFactor(final DoubleParameter shapeScalingFactor) {
        this.shapeScalingFactor = shapeScalingFactor;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getProportionOfZeros() {
        return proportionOfZeros;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getCapacityScalingFactor() {
        return capacityScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setCapacityScalingFactor(final DoubleParameter capacityScalingFactor) {
        this.capacityScalingFactor = capacityScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setProportionOfZeros(final Map<String, DoubleParameter> proportionOfZeros) {
        this.proportionOfZeros = proportionOfZeros;
    }

    @SuppressWarnings("unused")
    public void setScaleParameters(final Map<String, DoubleParameter> scaleParameters) {
        this.scaleParameters = scaleParameters;
    }
}

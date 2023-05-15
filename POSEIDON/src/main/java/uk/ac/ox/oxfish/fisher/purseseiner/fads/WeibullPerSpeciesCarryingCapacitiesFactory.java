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
    private DoubleParameter scalingFactor;

    public WeibullPerSpeciesCarryingCapacitiesFactory() {
    }

    public WeibullPerSpeciesCarryingCapacitiesFactory(
        final Map<String, DoubleParameter> shapeParameters,
        final Map<String, DoubleParameter> scaleParameters,
        final Map<String, DoubleParameter> proportionOfZeros,
        final DoubleParameter scalingFactor
    ) {
        this.shapeParameters = shapeParameters;
        this.scaleParameters = scaleParameters;
        this.proportionOfZeros = proportionOfZeros;
        this.scalingFactor = scalingFactor;
    }

    public Map<String, DoubleParameter> getShapeParameters() {
        return shapeParameters;
    }

    public void setShapeParameters(final Map<String, DoubleParameter> shapeParameters) {
        this.shapeParameters = shapeParameters;
    }

    public Map<String, DoubleParameter> getScaleParameters() {
        return scaleParameters;
    }

    public void setScaleParameters(final Map<String, DoubleParameter> scaleParameters) {
        this.scaleParameters = scaleParameters;
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
        if (scaleParameters.containsKey(speciesName) && shapeParameters.containsKey(speciesName)) {
            return new ScaledDoubleParameter(
                new ZeroInflatedDoubleParameter(
                    new WeibullDoubleParameter(
                        shapeParameters.get(speciesName).applyAsDouble(rng),
                        scaleParameters.get(speciesName).applyAsDouble(rng)
                    ),
                    getProportionOfZeros().get(speciesName).applyAsDouble(rng)
                ),
                getScalingFactor().applyAsDouble(rng)
            );
        } else {
            return new FixedDoubleParameter(-1);
        }
    }

    public Map<String, DoubleParameter> getProportionOfZeros() {
        return proportionOfZeros;
    }

    public DoubleParameter getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(final DoubleParameter scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public void setProportionOfZeros(final Map<String, DoubleParameter> proportionOfZeros) {
        this.proportionOfZeros = proportionOfZeros;
    }
}

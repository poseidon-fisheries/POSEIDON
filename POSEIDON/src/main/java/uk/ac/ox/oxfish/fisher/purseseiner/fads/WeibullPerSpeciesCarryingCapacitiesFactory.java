package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class WeibullPerSpeciesCarryingCapacitiesFactory
    extends AbstractCarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> {

    private Map<String, DoubleParameter> shapeParameters;
    private Map<String, DoubleParameter> scaleParameters;

    public WeibullPerSpeciesCarryingCapacitiesFactory() {
    }

    public WeibullPerSpeciesCarryingCapacitiesFactory(
        final Map<String, DoubleParameter> shapeParameters,
        final Map<String, DoubleParameter> scaleParameters
    ) {
        this.shapeParameters = shapeParameters;
        this.scaleParameters = scaleParameters;
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
            getProbabilityOfFadBeingDud().applyAsDouble(rng),
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
            return new WeibullDoubleParameter(
                shapeParameters.get(speciesName).applyAsDouble(rng),
                scaleParameters.get(speciesName).applyAsDouble(rng)
            );
        } else {
            return new FixedDoubleParameter(-1);
        }
    }
}

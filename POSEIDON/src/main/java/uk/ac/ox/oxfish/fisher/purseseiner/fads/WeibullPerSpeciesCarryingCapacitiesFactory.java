package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.Map;

public class WeibullPerSpeciesCarryingCapacitiesFactory extends PerSpeciesCarryingCapacitiesFactory {

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
    DoubleParameter makeSpeciesCarryingCapacityParameter(
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

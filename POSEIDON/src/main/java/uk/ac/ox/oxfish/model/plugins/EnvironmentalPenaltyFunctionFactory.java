package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.function.Function;

import static java.lang.Math.*;

public class EnvironmentalPenaltyFunctionFactory extends EnvironmentalFunctionFactory<Double> {

    public EnvironmentalPenaltyFunctionFactory() {
        super();
    }

    public EnvironmentalPenaltyFunctionFactory(
        final Map<String, EnvironmentalMapFactory> environmentalMapFactories
    ) {
        super(environmentalMapFactories);
    }

    @Override
    Double identity() {
        return 1.0;
    }

    @Override
    Function<SeaTile, Double> makeFunction(
        final FishState fishState,
        final EnvironmentalMapFactory environmentalMapFactory
    ) {
        final String mapName = environmentalMapFactory.getMapVariableName();
        final double margin = environmentalMapFactory.getMargin().applyAsDouble(fishState.getRandom());
        final double penalty = environmentalMapFactory.getPenalty().applyAsDouble(fishState.getRandom());
        final double target = environmentalMapFactory.getTarget().applyAsDouble(fishState.getRandom());
        return seaTile -> {
            final double valueHere =
                fishState.getMap()
                    .getAdditionalMaps()
                    .get(mapName)
                    .get()
                    .get(seaTile.getGridX(), seaTile.getGridY());
            final double valueDifference = abs(valueHere - target) - margin;
            return (valueDifference > 0) ? 1 / pow(1 + (-valueDifference * log(1 - penalty)), 4) : 1;
        };
    }

    @Override
    Double accumulator(
        final Double a,
        final Double b
    ) {
        return a * b;
    }

}

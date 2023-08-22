package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

public class EnvironmentalPredicateFunctionFactory extends EnvironmentalFunctionFactory<Boolean> {

    @Override
    Boolean identity() {
        return true;
    }

    @Override
    Function<SeaTile, Boolean> makeFunction(
        final FishState fishState,
        final EnvironmentalMapFactory environmentalMapFactory
    ) {
        final String mapName = environmentalMapFactory.getMapVariableName();
        final double target = environmentalMapFactory.getTarget().applyAsDouble(fishState.getRandom());
        final double margin = environmentalMapFactory.getMargin().applyAsDouble(fishState.getRandom());
        return seaTile ->
            Math.abs(fishState
                .getMap()
                .getAdditionalMaps()
                .get(mapName)
                .get()
                .get(
                    seaTile.getGridX(),
                    seaTile.getGridY()
                )-target) >= margin;
    }

    @Override
    Boolean accumulator(final Boolean a, final Boolean b) {
        return a && b;
    }
}

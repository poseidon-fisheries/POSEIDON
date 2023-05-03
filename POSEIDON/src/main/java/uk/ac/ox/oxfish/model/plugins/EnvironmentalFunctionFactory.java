package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;
import java.util.function.Function;

public abstract class EnvironmentalFunctionFactory<T> implements AlgorithmFactory<Function<SeaTile, T>> {
    private Map<String, EnvironmentalMapFactory> environmentalMapFactories;

    EnvironmentalFunctionFactory(final Map<String, EnvironmentalMapFactory> environmentalMapFactories) {
        this.environmentalMapFactories = environmentalMapFactories;
    }

    EnvironmentalFunctionFactory() {
    }

    @Override
    public Function<SeaTile, T> apply(final FishState fishState) {
        registerMaps(fishState);
        final T identity = identity();
        return getEnvironmentalMapFactories()
            .values()
            .stream()
            .map(factory -> makeFunction(fishState, factory))
            .reduce(
                seaTile -> identity,
                (f1, f2) -> seaTile -> accumulator(f1.apply(seaTile), f2.apply(seaTile))
            );
    }

    private void registerMaps(final FishState fishState) {
        environmentalMapFactories
            .values()
            .stream()
            .map(x -> x.apply(fishState))
            .forEach(fishState::registerStartable);
    }

    abstract T identity();

    public Map<String, EnvironmentalMapFactory> getEnvironmentalMapFactories() {
        return environmentalMapFactories;
    }

    public void setEnvironmentalMapFactories(final Map<String, EnvironmentalMapFactory> environmentalMapFactories) {
        this.environmentalMapFactories = environmentalMapFactories;
    }

    abstract Function<SeaTile, T> makeFunction(
        FishState fishState,
        EnvironmentalMapFactory environmentalMapFactory
    );

    abstract T accumulator(T a, T b);
}

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

public abstract class FadInitializerFactory<
    B extends LocalBiology,
    F extends Fad>
    implements AlgorithmFactory<FadInitializer<B, F>> {
    private final CacheByFishState<FadInitializer<B, F>> cache =
        new CacheByFishState<>(this::makeFadInitializer);
    private Map<String, DoubleParameter> catchabilities;
    private CarryingCapacityInitializerFactory<?> carryingCapacityInitializer;
    private DoubleParameter daysInWaterBeforeAttraction;
    private Map<String, DoubleParameter> fishReleaseProbabilities;

    FadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacityInitializer,
        final Map<String, DoubleParameter> catchabilities,
        final Map<String, DoubleParameter> fishReleaseProbabilities,
        final DoubleParameter daysInWaterBeforeAttraction
    ) {
        this.carryingCapacityInitializer = carryingCapacityInitializer;
        this.catchabilities = catchabilities;
        this.fishReleaseProbabilities = fishReleaseProbabilities;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    FadInitializerFactory() {
    }

    public Map<String, DoubleParameter> getFishReleaseProbabilities() {
        return fishReleaseProbabilities;
    }

    public void setFishReleaseProbabilities(final Map<String, DoubleParameter> fishReleaseProbabilities) {
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    public Map<String, DoubleParameter> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(final Map<String, DoubleParameter> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        invalidateCache();
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    void invalidateCache() {
        cache.invalidateAll();
    }

    @Override
    public FadInitializer<B, F> apply(final FishState fishState) {
        return cache.get(fishState);
    }

    protected abstract FadInitializer<B, F> makeFadInitializer(FishState fishState);

    public CarryingCapacityInitializerFactory<?> getCarryingCapacityInitializer() {
        return carryingCapacityInitializer;
    }

    public void setCarryingCapacityInitializer(final CarryingCapacityInitializerFactory<?> carryingCapacityInitializer) {
        this.carryingCapacityInitializer = carryingCapacityInitializer;
    }
}

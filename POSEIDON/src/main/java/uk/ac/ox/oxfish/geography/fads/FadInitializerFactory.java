package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Map;

public abstract class FadInitializerFactory<
    B extends LocalBiology,
    F extends Fad>
    implements AlgorithmFactory<FadInitializer<B, F>> {
    private final CacheByFishState<FadInitializer<B, F>> cache =
        new CacheByFishState<>(this::makeFadInitializer);
    private Map<String, DoubleParameter> catchabilities;
    private CarryingCapacityInitializerFactory<?> carryingCapacityInitializerFactory;
    private DoubleParameter fishValueCalculatorStandardDeviation =
        new CalibratedParameter(0, 0.5, 0, 1, 0);
    private DoubleParameter fadDudRate =
        new CalibratedParameter(0, 0.35, 0, 1, 0.001);
    private DoubleParameter daysInWaterBeforeAttraction =
        new CalibratedParameter(13, 30, 5, 60, 14);
    private DoubleParameter maximumDaysAttractions =
        new FixedDoubleParameter(Integer.MAX_VALUE);
    private DoubleParameter fishReleaseProbabilityInPercent =
        new CalibratedParameter(0.0, 3.5, 0, 10, 3.3);

    FadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacityInitializerFactory,
        final Map<String, DoubleParameter> catchabilities,
        final DoubleParameter fishValueCalculatorStandardDeviation,
        final DoubleParameter fadDudRate,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter maximumDaysAttractions,
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        this.carryingCapacityInitializerFactory = carryingCapacityInitializerFactory;
        this.catchabilities = catchabilities;
        this.fishValueCalculatorStandardDeviation = fishValueCalculatorStandardDeviation;
        this.fadDudRate = fadDudRate;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maximumDaysAttractions = maximumDaysAttractions;
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    FadInitializerFactory() {
    }

    public FadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacityInitializerFactory,
        final Map<String, DoubleParameter> catchabilities
    ) {
        this.carryingCapacityInitializerFactory = carryingCapacityInitializerFactory;
        this.catchabilities = catchabilities;
    }

    public Map<String, DoubleParameter> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(final Map<String, DoubleParameter> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getFishValueCalculatorStandardDeviation() {
        return fishValueCalculatorStandardDeviation;
    }

    public void setFishValueCalculatorStandardDeviation(final DoubleParameter fishValueCalculatorStandardDeviation) {
        invalidateCache();
        this.fishValueCalculatorStandardDeviation = fishValueCalculatorStandardDeviation;
    }

    void invalidateCache() {
        cache.invalidateAll();
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(final DoubleParameter fadDudRate) {
        invalidateCache();
        this.fadDudRate = fadDudRate;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        invalidateCache();
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getMaximumDaysAttractions() {
        return maximumDaysAttractions;
    }

    public void setMaximumDaysAttractions(final DoubleParameter maximumDaysAttractions) {
        invalidateCache();
        this.maximumDaysAttractions = maximumDaysAttractions;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(final DoubleParameter fishReleaseProbabilityInPercent) {
        invalidateCache();
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    @Override
    public FadInitializer<B, F> apply(final FishState fishState) {
        return cache.get(fishState);
    }

    protected abstract FadInitializer<B, F> makeFadInitializer(FishState fishState);

    public CarryingCapacityInitializerFactory<?> getCarryingCapacityInitializerFactory() {
        return carryingCapacityInitializerFactory;
    }

    public void setCarryingCapacityInitializerFactory(final CarryingCapacityInitializerFactory<?> carryingCapacityInitializerFactory) {
        this.carryingCapacityInitializerFactory = carryingCapacityInitializerFactory;
    }
}
